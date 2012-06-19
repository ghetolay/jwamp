package com.github.ghetolay.jwamp.event;

public interface EventSender {
	public void sendEvent(String sessionId, EventAction action, Object event);
}
