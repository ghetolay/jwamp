package com.github.ghetolay.jwamp.event;


import java.io.IOException;

import com.github.ghetolay.jwamp.WampMessageHandler;

public interface WampEventSubscriber extends WampMessageHandler{
	
	public void subscribe(String topicId) throws IOException;
	public void unsubscribe(String topicId) throws IOException;
	
	public void publish(String topicId, Object event) throws IOException;
	public void publish(String topicId, Object event, boolean excludeMe) throws IOException;
	public void publish(String topicId, Object event, boolean excludeMe, String[] eligible) throws IOException;
	public void publish(String topicId, Object event, String[] exclude, String[] eligible) throws IOException;
	
	public EventListener getListener();
	public void setListener(EventListener listener);
	
}
