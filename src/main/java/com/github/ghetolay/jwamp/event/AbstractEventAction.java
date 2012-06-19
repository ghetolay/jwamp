package com.github.ghetolay.jwamp.event;


import java.util.HashSet;
import java.util.Set;

import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampPublishMessage;

public class AbstractEventAction implements EventAction {

	private EventSender listener; 
	private Set<String> subscriber = new HashSet<String>();
	
	public void addEventListener(EventSender listener) {
		this.listener = listener;
	}

	public void removeEventListener(EventSender listener) {
		this.listener = null;
	}
	
	public void subscribe(String sessionId) {
		subscriber.add(sessionId);
	}

	public void unsubscribe(String sessionId) {
		subscriber.remove(sessionId);
	}

	public String[] publish(String sessionId, WampPublishMessage wampPublishMessage, WampEventMessage msg) {
		if(wampPublishMessage.getEligible() != null)
			return wampPublishMessage.getEligible();
		
		Set<String> res;
		if(wampPublishMessage.getExclude() != null){
			res = new HashSet<String>(subscriber);
			for(String s : wampPublishMessage.getExclude())
				res.remove(s);
		}
		
		else{ 
			if(wampPublishMessage.isExcludeMe()){
				res = new HashSet<String>(subscriber);
				res.remove(sessionId);
			}else 
				res = subscriber;
		}
		
		return res.toArray(new String[0]);	
	}
	
	public void eventAll(Object event){
		for(String s : subscriber)
			if(listener != null)
				listener.sendEvent(s, this, event);
	}
}
