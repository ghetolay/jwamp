package com.github.ghetolay.jwamp.event;

public interface EventListener {
	public void event(String sessionId, String topicId, Object event);
}
