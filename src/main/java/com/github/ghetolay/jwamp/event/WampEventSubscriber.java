package com.github.ghetolay.jwamp.event;


import java.io.IOException;

import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.event.DefaultEventSubscriber.EventResult;
import com.github.ghetolay.jwamp.utils.ResultListener;

public interface WampEventSubscriber extends WampMessageHandler{
	
	public void subscribe(String topicId) throws IOException;
	public void subscribe(String topicId, ResultListener<Object> resultListener) throws IOException;
	public void unsubscribe(String topicId) throws IOException;
	
	public void publish(String topicId, Object event) throws IOException;
	public void publish(String topicId, Object event, boolean excludeMe) throws IOException;
	public void publish(String topicId, Object event, boolean excludeMe, String[] eligible) throws IOException;
	public void publish(String topicId, Object event, String[] exclude, String[] eligible) throws IOException;
	
	public ResultListener<EventResult> getGlobalListener();
	public void setGlobalListener(ResultListener<EventResult> listener);
	
}
