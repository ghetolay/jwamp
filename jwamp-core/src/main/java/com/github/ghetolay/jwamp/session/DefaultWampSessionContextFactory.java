/**
 * 
 */
package com.github.ghetolay.jwamp.session;

import javax.websocket.Session;

import com.github.ghetolay.jwamp.actions.ActionRegistry;
import com.github.ghetolay.jwamp.endpoint.WampEndpointParameters;
import com.github.ghetolay.jwamp.event.DefaultEventPublisher;
import com.github.ghetolay.jwamp.event.DefaultEventSubscriptionManager;
import com.github.ghetolay.jwamp.event.EventAction;
import com.github.ghetolay.jwamp.event.EventMessageHandler;
import com.github.ghetolay.jwamp.event.EventSubscriptionRegistry;
import com.github.ghetolay.jwamp.event.PubSubMessageHandler;
import com.github.ghetolay.jwamp.message.LoopbackMessageSender;
import com.github.ghetolay.jwamp.message.MessageHandlerRegistry;
import com.github.ghetolay.jwamp.message.MessageSender;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampMessageHandler;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.DefaultRPCSender;
import com.github.ghetolay.jwamp.rpc.RPCMessageHandler;

public class DefaultWampSessionContextFactory implements WampSessionContextFactory{
	
	private final boolean createAsPubSubBroker;
	
	public static WampSessionContextFactory createDefaultForServer(){
		return new DefaultWampSessionContextFactory(true);
	}
	
	public static WampSessionContextFactory createDefaultForClient(){
		return new DefaultWampSessionContextFactory(false);
	}
	
	private DefaultWampSessionContextFactory(boolean createAsPubSubBroker) {
		this.createAsPubSubBroker = createAsPubSubBroker;
	}
	
	@Override
	public WampSessionContext createContext(final Session session, WampEndpointParameters endpointParameters, String wampSessionId){

		MessageHandlerRegistry.Builder dispatcherBuilder = new MessageHandlerRegistry.Builder();

		final MessageSender remoteMessageSender = new WebsocketMessageSender(session);
		
		// Pub/Sub stuff for clients AND servers
		final EventSubscriptionRegistry eventSubscriptionRegistry = new EventSubscriptionRegistry(); 

		final ActionRegistry<EventAction> eventActionRegistry = new ActionRegistry<EventAction>();
		
		final EventMessageHandler eventMessageHandler = new EventMessageHandler(eventActionRegistry);
		dispatcherBuilder.register(eventMessageHandler);
		
		
		// RPC stuff for clients AND servers
		final ActionRegistry<CallAction> callActionRegistry = new ActionRegistry<CallAction>(); 
		
		final RPCMessageHandler rpcMessageHandler = new RPCMessageHandler(remoteMessageSender, callActionRegistry);
		dispatcherBuilder.register(rpcMessageHandler);
		
		final DefaultRPCSender rpcSender = new DefaultRPCSender(endpointParameters.getRpcTimeoutManager(), remoteMessageSender, wampSessionId);
		dispatcherBuilder.register(rpcSender);
		
		// behavior of the WampSession is slightly different depending on whether we are a client connection or a server connection
		
		WampSessionHolder wampSessionHolder = new WampSessionHolder();
		
		final MessageSender pubSubMessageSender;
		if (createAsPubSubBroker){
			
			// Pub/Sub stuff for servers only
			final PubSubMessageHandler pubSubMessageHandler = new PubSubMessageHandler(eventSubscriptionRegistry, endpointParameters.getSessionRegistry());
			dispatcherBuilder.register(pubSubMessageHandler);

			pubSubMessageSender = new LoopbackMessageSender(wampSessionHolder, pubSubMessageHandler);
		} else {
			pubSubMessageSender = remoteMessageSender;
		}
		
		DefaultEventPublisher eventPublisher = new DefaultEventPublisher(pubSubMessageSender);
		DefaultEventSubscriptionManager eventSubscriptionManager = new DefaultEventSubscriptionManager(pubSubMessageSender, eventActionRegistry);
		
		final WampSession wampSession = new WampSession(session, wampSessionId, eventPublisher, eventSubscriptionManager, rpcSender, callActionRegistry);

		wampSessionHolder.setWampSession(wampSession);
		
		// create the main dispatcher
		final WampMessageHandler messageHandler = dispatcherBuilder.build();

		
			
		DefaultWampSessionConfig wampSessionConfig = new DefaultWampSessionConfig(wampSessionId, session, eventSubscriptionRegistry, messageHandler, remoteMessageSender, wampSession);
		
		return wampSessionConfig;		
	}
	
	private final static class WampSessionHolder implements WampSessionProvider{
		private WampSession wampSession;

		private void setWampSession(WampSession wampSession){
			if (this.wampSession != null) throw new IllegalStateException("WampSession can only be set one time");
			this.wampSession = wampSession;
		}
		
		@Override
		public WampSession getWampSession() {
			return wampSession;
		}
	}
	
	/**
	 * @author Kevin
	 *
	 */
	private final class WebsocketMessageSender implements MessageSender {
		/**
		 * 
		 */
		private final Session session;

		/**
		 * @param session
		 */
		private WebsocketMessageSender(Session session) {
			this.session = session;
		}

		@Override
		public void sendToRemote(WampMessage msg) {
			session.getAsyncRemote().sendObject(msg);
		}
	}

	private static class DefaultWampSessionConfig implements WampSessionContext {

		private final String sessionId;
		private final Session webSocketSession;
		private final EventSubscriptionRegistry eventSubscriptionRegistry;
		private final WampMessageHandler messageHandler;
		private final MessageSender remoteMessageSender;
		private final WampSession wampSession;

		
		
		private DefaultWampSessionConfig(String sessionId, Session webSocketSession,
				EventSubscriptionRegistry eventSubscriptionRegistry, WampMessageHandler messageHandler,
				MessageSender remoteMessageSender, WampSession wampSession) {

			this.sessionId = sessionId;
			this.webSocketSession = webSocketSession;
			this.eventSubscriptionRegistry = eventSubscriptionRegistry;
			this.messageHandler = messageHandler;
			this.remoteMessageSender = remoteMessageSender;
			this.wampSession = wampSession;
		}

		@Override
		public WampSession getWampSession() {
			return wampSession;
		}
		
		@Override
		public String getSessionId() {
			return sessionId;
		}
		
		@Override
		public Session getWebSocketSession() {
			return webSocketSession;
		}

		@Override
		public EventSubscriptionRegistry getEventSubscriptionRegistry() {
			return eventSubscriptionRegistry;
		}
		
		@Override
		public WampMessageHandler getMessageHandler() {
			return messageHandler;
		}
		
		@Override
		public MessageSender getRemoteMessageSender() {
			return remoteMessageSender;
		}
		

	}
}