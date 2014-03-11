package com.github.ghetolay.jwamp.session;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import com.github.ghetolay.jwamp.actions.ActionRegistry;
import com.github.ghetolay.jwamp.event.DefaultEventPublisher;
import com.github.ghetolay.jwamp.event.DefaultEventSubscriptionManager;
import com.github.ghetolay.jwamp.event.EventPublisher;
import com.github.ghetolay.jwamp.event.EventSubscriptionManager;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.RPCSender;

public class WampSession {
	
	private final Session session;
	private final String wampSessionId;
	private final EventPublisher eventPublisher;
	private final EventSubscriptionManager eventSubscriptionManager;
	private final RPCSender rpcSender;
	private final ActionRegistry<CallAction> callActionRegistry;
	
	public WampSession(Session session, String wampSessionId, DefaultEventPublisher eventPublisher, DefaultEventSubscriptionManager eventSubscriptionRegistry, RPCSender rpcSender, ActionRegistry<CallAction> callActionRegistry){
		this.session = session;
		this.wampSessionId = wampSessionId;
		this.eventPublisher = eventPublisher;
		this.eventSubscriptionManager = eventSubscriptionRegistry;
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
