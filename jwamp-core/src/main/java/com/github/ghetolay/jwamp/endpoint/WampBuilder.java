package com.github.ghetolay.jwamp.endpoint;

import java.net.URI;

import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.event.DefaultEventManager;
import com.github.ghetolay.jwamp.event.DefaultEventSubscriber;
import com.github.ghetolay.jwamp.event.EventAction;
import com.github.ghetolay.jwamp.event.EventSubscription;
import com.github.ghetolay.jwamp.rpc.Action;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.CompleteCallAction;
import com.github.ghetolay.jwamp.rpc.DefaultRPCManager;
import com.github.ghetolay.jwamp.rpc.DefaultRPCSender;
import com.github.ghetolay.jwamp.utils.ActionMapping;
import com.github.ghetolay.jwamp.utils.MapActionMapping;

public class WampBuilder {
	
	public static class Dispatcher{
	
		WampDispatcher dispatcher;
		
		public static Dispatcher newDefaultDispatcher(SessionManager sessions){
			Dispatcher builder = new Dispatcher();
			builder.dispatcher = new DefaultWampDispatcher(sessions);
			
			return builder;
		}
		
		public Dispatcher addMessageHandler(WampMessageHandler handler){
			dispatcher.addMessageHandler(handler);
			return this;
		}
		
		public Dispatcher addRPCSender(){
			dispatcher.addMessageHandler(new DefaultRPCSender());
			return this;
		}
		
		public RPCManager newRPCManager(){
			RPCManager builder= new RPCManager(this);
			return builder;
		}
		
		public Dispatcher addEventsubscriber(){
			dispatcher.addMessageHandler(new DefaultEventSubscriber());
			return this;
		}
		
		public EventSubscriber newEventsubscriber(){
			EventSubscriber builder= new EventSubscriber(this);
			return builder;
		}
		
		public EventManager newEventManager(){
			EventManager builder= new EventManager(this);
			return builder;
		}
		
		public WampDispatcher build(){
			return dispatcher;
		}
	}
	
	public static class RPCManager{
		
		Dispatcher mainBuilder;
		DefaultRPCManager rpcManager;
		
		RPCManager(Dispatcher mainBuilder) {
			this.mainBuilder = mainBuilder;
			rpcManager = new DefaultRPCManager();
			
			rpcManager.setActionMapping(new MapActionMapping<Action>());
		}
		
		/**
		 * Must be set before adding any action cause this will reset all action previously added
		 * 
		 * @param actionMapping
		 * @return
		 */
		public RPCManager setActionMapping(ActionMapping<Action> actionMapping){
			rpcManager.setActionMapping(actionMapping);
			return this;
		}
		
		public RPCManager addAction(URI procURI, CallAction action){
			rpcManager.addAction(procURI, action);
			return this;
		}
		
		public RPCManager addCompleteAction(URI procURI, CompleteCallAction action){
			rpcManager.addCompleteAction(procURI, action);
			return this;
		}
		
		public Dispatcher build(){
			mainBuilder.dispatcher.addMessageHandler(rpcManager);
			return mainBuilder;
		}
		
	}
	
	public static class EventSubscriber{
		
		Dispatcher mainBuilder;
		DefaultEventSubscriber eventSub;
		
		EventSubscriber(Dispatcher mainBuilder) {
			this.mainBuilder = mainBuilder;
			eventSub = new com.github.ghetolay.jwamp.event.DefaultEventSubscriber();
		}
		
		public EventSubscriber addTopic(URI topicURI){
			eventSub.addSubscription(new EventSubscription.Impl(topicURI));
			return this;
		}
		
		public EventSubscriber addTopic(EventSubscription subscription){
			eventSub.addSubscription(subscription);
			return this;
		}
		
		public Dispatcher build(){
			mainBuilder.dispatcher.addMessageHandler(eventSub);
			return mainBuilder;
		}
		
	}
	
	public static class EventManager{
		
		Dispatcher mainBuilder;
		DefaultEventManager eventManager;
		
		EventManager(Dispatcher mainBuilder) {
			this.mainBuilder = mainBuilder;
			eventManager = new DefaultEventManager();
		}
		
		public EventManager addEvent(URI eventURI, EventAction eventAction){
			eventManager.addEventAction(eventURI, eventAction);
			return this;
		}
		
		public Dispatcher build(){
			mainBuilder.dispatcher.addMessageHandler(eventManager);
			return mainBuilder;
		}
		
	}
}
