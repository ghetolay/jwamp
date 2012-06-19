package com.github.ghetolay.jwamp.event;

import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampPublishMessage;

public interface EventAction {
	
	public void addEventListener(EventSender listener);
	public void removeEventListener(EventSender listener);
	
	public void subscribe(String sessionId);
	public void unsubscribe(String sessionId);

	public String[] publish(String sessionId, WampPublishMessage wampPublishMessage, WampEventMessage msg);
}
