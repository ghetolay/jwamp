package com.github.ghetolay.jwamp;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.Encoder;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpointConfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.github.ghetolay.jwamp.actions.ActionRegistration;
import com.github.ghetolay.jwamp.endpoint.WampClientEndPoint;
import com.github.ghetolay.jwamp.endpoint.WampEndpointParameters;
import com.github.ghetolay.jwamp.endpoint.WampServerEndpoint;
import com.github.ghetolay.jwamp.event.EventAction;
import com.github.ghetolay.jwamp.message.WampMessageDecoder;
import com.github.ghetolay.jwamp.message.WampMessageEncoder;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.CallIdTimeoutKey;
import com.github.ghetolay.jwamp.rpc.CallResultListener;
import com.github.ghetolay.jwamp.session.DefaultWampSessionConfigFactory;
import com.github.ghetolay.jwamp.session.SessionRegistry;
import com.github.ghetolay.jwamp.session.WampLifeCycleListener;
import com.github.ghetolay.jwamp.session.WampSession;
import com.github.ghetolay.jwamp.session.WampSessionConfigFactory;
import com.github.ghetolay.jwamp.utils.ResultListener;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap;
import com.github.ghetolay.jwamp.utils.WaitResponse;

public class WampBuilder {
	
	private static final String protocolName = "wamp";
	private static final int protocolVersion = 1;
	private static final String implementation = "jwamp/0.1";//"jwamp/${project.version}";

	
	private SessionRegistry sessionRegistry = new SessionRegistry();
	private JsonFactory jsonFactory = new MappingJsonFactory();
	private final List<ActionRegistration<CallAction>> initialCallActionRegistrations = new ArrayList<ActionRegistration<CallAction>>();
	private final List<ActionRegistration<EventAction>> initialEventActionRegistrations = new ArrayList<ActionRegistration<EventAction>>();
	private final List<WampLifeCycleListener> lifecycleListeners = new ArrayList<WampLifeCycleListener>();
	private final List<Class<? extends Encoder>> encoders = createEncoderClassList(WampMessageEncoder.Text.class);
	private final List<Class<? extends Decoder>> decoders = createDecoderClassList(WampMessageDecoder.Text.class);
	private final TimeoutHashMap<CallIdTimeoutKey, CallResultListener> rpcTimeoutManager = new TimeoutHashMap<CallIdTimeoutKey, CallResultListener>();

	/**
	 * Return the name of the WebSocket subprotocol.
	 * 
	 * @return The subprotocol name i.e. WAMP 
	 */
	public static String getProtocolName() {
		return protocolName;
	}

	/**
	 * 
	 * @return The protocol version.
	 */
	public static int getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * The WAMP implementation name in the form of "jwamp/VERSION".
	 * 
	 * @return The WAMP implementation name.
	 */
	public static String getImplementation() {
		return implementation;
	}
	
	
	public static WampBuilder create(){
		return new WampBuilder();
	}

	public Server forServer(String path){
		return new Server(this, path);
	}
	
	public Client forClient(){
		return new Client(this);
	}
	
	public WampBuilder withCallAction(URI actionUri, CallAction action){
		initialCallActionRegistrations.add(new ActionRegistration<CallAction>(actionUri, action));
		return this;
	}

	public WampBuilder withEventSubscription(URI topicUri, EventAction action){
		initialEventActionRegistrations.add(new ActionRegistration<EventAction>(topicUri, action));
		return this;
	}
	
	public WampBuilder withLifeCycleListener(WampLifeCycleListener listener){
		lifecycleListeners.add(listener);
		return this;
	}
	
	// utility methods to help with constructor
	
	private static List<Class<? extends Encoder>> createEncoderClassList(Class<? extends WampMessageEncoder> clazz){
		List<Class<? extends Encoder>> lst = new ArrayList<Class<? extends Encoder>>(1);
		lst.add(clazz);
		return Collections.unmodifiableList(lst);
	}

	private static List<Class<? extends Decoder>> createDecoderClassList(Class<? extends WampMessageDecoder> clazz){
		List<Class<? extends Decoder>> lst = new ArrayList<Class<? extends Decoder>>(1);
		lst.add(clazz);
		return Collections.unmodifiableList(lst);
	}

	
	
	public static class Client {
		private final WampBuilder wampBuilder;
		private WampSessionConfigFactory wampSessionConfigFactory;
		private long connectionTimeout = 1000;

		private Client(WampBuilder wampBuilder) {
			this.wampBuilder = wampBuilder;
			this.wampSessionConfigFactory = DefaultWampSessionConfigFactory.createDefaultClientFactory();
		}

		public Client withConnectionTimeout(long connectionTimeout){
			this.connectionTimeout = connectionTimeout;
			return this;
		}
		
		public Client withWampSessionConfigFactory(WampSessionConfigFactory wampSessionConfigFactory) {
			this.wampSessionConfigFactory = wampSessionConfigFactory;
			return this;
		}
		
		public WampEndpointParameters createEndpointParameters(){
			return new WampEndpointParameters(wampBuilder.sessionRegistry, 
					wampBuilder.jsonFactory, 
					wampBuilder.initialCallActionRegistrations, 
					wampBuilder.initialEventActionRegistrations, 
					wampBuilder.lifecycleListeners, 
					wampSessionConfigFactory,
					wampBuilder.rpcTimeoutManager);
		}
		
		public ClientEndpointConfig createEndpointConfig(){
			return ClientEndpointConfig.Builder.create()
				.decoders(wampBuilder.decoders)
				.encoders(wampBuilder.encoders)
				.preferredSubprotocols(Collections.singletonList(getProtocolName()))
				.build();
		}
		
		public WampClientEndPoint createEndPoint(ResultListener<WampSession> connectionListener){
			return new WampClientEndPoint(createEndpointParameters(), connectionListener);
		}
		
		public WampSession connectToServer(URI serverUri) throws InterruptedException, DeploymentException, IOException{
			
			WaitResponse<WampSession> openListener = new WaitResponse<>(connectionTimeout);
			
			WampClientEndPoint endPoint = createEndPoint(openListener);
			
			Session webSocketSession = ContainerProvider.getWebSocketContainer().connectToServer(endPoint, createEndpointConfig(), serverUri);

			WampSession session = openListener.call();
			if (session == null){
				webSocketSession.close();
				throw new IOException("Unable to connect to " + serverUri);
			}
			
			return session;
		}

	}
	
	
	public static class Server {
		private final WampBuilder wampBuilder;
		private final String path;
		private WampSessionConfigFactory wampSessionConfigFactory;

		public Server(WampBuilder wampBuilder, String path) {
			this.wampBuilder = wampBuilder;
			this.path = path;
			this.wampSessionConfigFactory = DefaultWampSessionConfigFactory.createDefaultServerFactory();

		}
		
		public Server withWampSessionConfigFactory(WampSessionConfigFactory wampSessionConfigFactory) {
			this.wampSessionConfigFactory = wampSessionConfigFactory;
			return this;
		}
		
		private WampEndpointParameters createEndpointParameters(){
			return new WampEndpointParameters(
					wampBuilder.sessionRegistry, 
					wampBuilder.jsonFactory, 
					wampBuilder.initialCallActionRegistrations, 
					wampBuilder.initialEventActionRegistrations, 
					wampBuilder.lifecycleListeners, 
					wampSessionConfigFactory,
					wampBuilder.rpcTimeoutManager);
		}
		
		// May eventually expose this in the API if someone needs finer grained control and wants to construct their endpoint config themselves
		private ServerEndpointConfig.Configurator createServerEndpointConfigurator(){
			
			return new ServerEndpointConfig.Configurator(){
				@Override
				public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
					return endpointClass.cast(new WampServerEndpoint(createEndpointParameters()));
				}
			};
		}
		
		public ServerEndpointConfig createEndpointConfig(){
			
			return ServerEndpointConfig.Builder.create(WampServerEndpoint.class, path)
			.subprotocols(Collections.singletonList(getProtocolName()))
			.decoders(wampBuilder.decoders)
			.encoders(wampBuilder.encoders)
			.configurator(createServerEndpointConfigurator())
			.build();
			
		}

	}

	
	
	

}
