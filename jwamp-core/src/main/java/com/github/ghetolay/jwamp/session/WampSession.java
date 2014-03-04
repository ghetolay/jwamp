package com.github.ghetolay.jwamp.session;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import com.github.ghetolay.jwamp.actions.ActionRegistry;
import com.github.ghetolay.jwamp.event.DefaultEventPublisher;
import com.github.ghetolay.jwamp.event.DefaultEventSubscriptionManager;
import com.github.ghetolay.jwamp.event.EventAction;
import com.github.ghetolay.jwamp.event.EventPublisher;
import com.github.ghetolay.jwamp.event.EventSubscriptionManager;
import com.github.ghetolay.jwamp.event.PubSubMessageHandler;
import com.github.ghetolay.jwamp.message.LoopbackMessageSender;
import com.github.ghetolay.jwamp.message.MessageSender;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.RPCSender;

public class WampSession {
	
	private final Session session;
	private final String wampSessionId;
	private final EventPublisher eventPublisher;
	private final EventSubscriptionManager eventSubscriptionManager;
	private final RPCSender rpcSender;
	private final ActionRegistry<CallAction> callActionRegistry;
	
	public static WampSession createForClient(Session session, String wampSessionId, MessageSender remoteMessageSender, ActionRegistry<EventAction> eventActionRegistry, RPCSender rpcSender, ActionRegistry<CallAction> callActionRegistry){
		return new WampSession(session, wampSessionId, remoteMessageSender, eventActionRegistry, rpcSender, callActionRegistry);
	}
	
	public static WampSession createForServer(Session session, String wampSessionId, PubSubMessageHandler pubSubMessageHandler, ActionRegistry<EventAction> eventActionRegistry, RPCSender rpcSender, ActionRegistry<CallAction> callActionRegistry){
		return new WampSession(session, wampSessionId, pubSubMessageHandler, eventActionRegistry, rpcSender, callActionRegistry);
	}
	
	// client session constructor
	private WampSession(Session session, String wampSessionId, MessageSender remoteMessageSender, ActionRegistry<EventAction> eventActionRegistry, RPCSender rpcSender, ActionRegistry<CallAction> callActionRegistry){
		this.session = session;
		this.wampSessionId = wampSessionId;
		this.eventPublisher = new DefaultEventPublisher(remoteMessageSender);
		this.eventSubscriptionManager = new DefaultEventSubscriptionManager(remoteMessageSender, eventActionRegistry);
		this.rpcSender = rpcSender;
		this.callActionRegistry = callActionRegistry;
	}
	
	// server session constructor
	private WampSession(Session session, String wampSessionId, PubSubMessageHandler pubSubMessageHandler, ActionRegistry<EventAction> eventActionRegistry, RPCSender rpcSender, ActionRegistry<CallAction> callActionRegistry){
		MessageSender loopbackSender = new LoopbackMessageSender(this, pubSubMessageHandler);
		
		this.session = session;
		this.wampSessionId = wampSessionId;
		this.eventPublisher = new DefaultEventPublisher(loopbackSender);
		this.eventSubscriptionManager = new DefaultEventSubscriptionManager(loopbackSender, eventActionRegistry);
		this.rpcSender = rpcSender;
		this.callActionRegistry = callActionRegistry;
	}
	
	public Session getWebSocketSession(){
		return session;
	}
	
	public void close(CloseReason closeReason) throws IOException{
		session.close(closeReason);
	}
	
	public String getWampSessionId(){
		return wampSessionId;
	}
	
	public EventPublisher getEventPublisher() {
		return eventPublisher;
	}
	
	public EventSubscriptionManager getEventSubscriptionManager() {
		return eventSubscriptionManager;
	}
	
	public RPCSender getRpcSender() {
		return rpcSender;
	}
	
	public ActionRegistry<CallAction> getCallActionRegistry() {
		return callActionRegistry;
	}
}
