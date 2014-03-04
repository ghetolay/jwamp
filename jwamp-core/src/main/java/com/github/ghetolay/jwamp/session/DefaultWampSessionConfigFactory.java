/**
 * 
 */
package com.github.ghetolay.jwamp.session;

import javax.websocket.Session;

import com.github.ghetolay.jwamp.actions.ActionRegistry;
import com.github.ghetolay.jwamp.endpoint.WampEndpointParameters;
import com.github.ghetolay.jwamp.event.EventAction;
import com.github.ghetolay.jwamp.event.EventMessageHandler;
import com.github.ghetolay.jwamp.event.EventSubscriptionRegistry;
import com.github.ghetolay.jwamp.event.PubSubMessageHandler;
import com.github.ghetolay.jwamp.message.MessageHandlerRegistry;
import com.github.ghetolay.jwamp.message.RemoteMessageSender;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampMessageHandler;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.DefaultRPCSender;
import com.github.ghetolay.jwamp.rpc.RPCMessageHandler;

public class DefaultWampSessionConfigFactory implements WampSessionConfigFactory{
	
	private final boolean serverMode;
	
	public static WampSessionConfigFactory createDefaultServerFactory(){
		return new DefaultWampSessionConfigFactory(true);
	}
	
	public static WampSessionConfigFactory createDefaultClientFactory(){
		return new DefaultWampSessionConfigFactory(false);
	}
	
	private DefaultWampSessionConfigFactory(boolean serverMode) {
		this.serverMode = serverMode;
	}
	
	@Override
	public WampSessionConfig createConfig(final Session session, WampEndpointParameters endpointParameters, String wampSessionId){

		MessageHandlerRegistry.Builder dispatcherBuilder = new MessageHandlerRegistry.Builder();

		final RemoteMessageSender remoteMessageSender = new RemoteMessageSender() {
			@Override
			public void sendToRemote(WampMessage msg) {
				session.getAsyncRemote().sendObject(msg);
			}
		};
		
		final SessionRegistry sessionRegistry = endpointParameters.getSessionRegistry();
		
		// Pub/Sub stuff for clients AND servers
		final EventSubscriptionRegistry eventSubscriptionRegistry = new EventSubscriptionRegistry(remoteMessageSender); 

		final ActionRegistry<EventAction> eventActionRegistry = new ActionRegistry<EventAction>();
		
		final EventMessageHandler eventMessageHandler = new EventMessageHandler(eventActionRegistry);
		dispatcherBuilder.register(eventMessageHandler);
		
		
		// RPC stuff for clients AND servers
		final ActionRegistry<CallAction> callActionRegistry = new ActionRegistry<CallAction>(); 
		
		final RPCMessageHandler rpcMessageHandler = new RPCMessageHandler(remoteMessageSender, callActionRegistry);
		dispatcherBuilder.register(rpcMessageHandler);
		
		final DefaultRPCSender rpcSender = new DefaultRPCSender(remoteMessageSender, wampSessionId);
		dispatcherBuilder.register(rpcSender);
		
		// behavior of the WampSession is slightly different depending on whether we are a client connection or a server connection
		
		final WampSession wampSession;
		if (serverMode){
			// Pub/Sub stuff for servers only
			final PubSubMessageHandler pubSubMessageHandler = new PubSubMessageHandler(eventSubscriptionRegistry, sessionRegistry);
			dispatcherBuilder.register(pubSubMessageHandler);

			wampSession = WampSession.createForServer(session, wampSessionId, pubSubMessageHandler, eventActionRegistry, rpcSender, callActionRegistry);
		} else {
			wampSession = WampSession.createForClient(session, wampSessionId, remoteMessageSender, eventActionRegistry, rpcSender, callActionRegistry);
		}
		
		
		// create the main dispatcher
		final WampMessageHandler messageHandler = dispatcherBuilder.build();

		
			
		DefaultWampSessionConfig wampSessionConfig = new DefaultWampSessionConfig(wampSessionId, session, eventSubscriptionRegistry, messageHandler, remoteMessageSender, wampSession);
		
		return wampSessionConfig;		
	}
	
	private static class DefaultWampSessionConfig implements WampSessionConfig {

		private final String sessionId;
		private final Session webSocketSession;
		private final EventSubscriptionRegistry eventSubscriptionRegistry;
		private final WampMessageHandler messageHandler;
		private final RemoteMessageSender remoteMessageSender;
		private final WampSession wampSession;

		
		
		private DefaultWampSessionConfig(String sessionId, Session webSocketSession,
				EventSubscriptionRegistry eventSubscriptionRegistry, WampMessageHandler messageHandler,
				RemoteMessageSender remoteMessageSender, WampSession wampSession) {

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
		public RemoteMessageSender getRemoteMessageSender() {
			return remoteMessageSender;
		}
		

	}
}