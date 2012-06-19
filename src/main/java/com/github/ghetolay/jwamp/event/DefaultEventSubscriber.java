package com.github.ghetolay.jwamp.event;


import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.message.BadMessageFormException;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class DefaultEventSubscriber implements WampEventSubscriber {

	private WampConnection conn;
	
	private Set<String> topics;
	private EventListener listener;
	
	private Map<String, ResultListener<WampEventMessage>> resultListeners = new HashMap<String, ResultListener<WampEventMessage>>();
	
	public DefaultEventSubscriber(){
		topics = new CopyOnWriteArraySet<String>();
	}
	
	public DefaultEventSubscriber(Collection<String> topics){
		this.topics = new CopyOnWriteArraySet<String>(topics);
	}
	
	public void onConnected(WampConnection connection) {
		conn = connection;
		if(!topics.isEmpty()){
			
			Set<String> initTopics = new HashSet<String>(topics);
			topics.clear();
			
			for(String s : initTopics)
				try {
					subscribe(s);
				} catch (IOException e) {
					// TODO log
					e.printStackTrace();
				}
		}
	}

	public void onClose(String sessionId, int closeCode) {}
	
	public boolean onMessage(String sessionId, int messageType, Object[] array)
			throws BadMessageFormException {
		if(messageType == WampMessage.EVENT){
			onEvent(new WampEventMessage(array));
			return true;
		}
		return false;
	}

	private void onEvent(WampEventMessage msg){
		if(listener != null)
			listener.event(conn.getSessionId(), msg.getTopicId(), msg.getEvent());
		
		if(resultListeners.containsKey(msg.getTopicId()))
			resultListeners.get(msg.getTopicId()).onResult(msg);
	}
	
	public void subscribe(String topicId) throws IOException {
		if(!topics.contains(topicId)){
			conn.sendMessage(new WampSubscribeMessage(WampMessage.SUBSCRIBE, topicId));
			
			topics.add(topicId);
		}
	}

	public void unsubscribe(String topicId) throws IOException {
		if(topics.contains(topicId)){
			conn.sendMessage(new WampSubscribeMessage(WampMessage.UNSUBSCRIBE, topicId));
			
			topics.remove(topicId);
		}
		if(resultListeners.containsKey(topicId))
			resultListeners.remove(topicId);
	}
	
	public void publish(String topicId, Object event) throws IOException {
		publish(topicId, event, true);
	}

	public void publish(String topicId, Object event, boolean excludeMe) throws IOException {
		publish(topicId, event, true, null, null );
	}

	public void publish(String topicId, Object event, boolean excludeMe, String[] eligible) throws IOException {
		if(eligible != null){
			String [] excludes;
			if(excludeMe)
				excludes = new String[] { conn.getSessionId() };
			else
				excludes = new String[0];
			publish(topicId, event, false, excludes, eligible);
		}else
			publish(topicId, event, excludeMe, null , null);
	}
	
	public void publish(String topicId, Object event, String[] exclude, String[] eligible) throws IOException {
		if(exclude.length == 1 && exclude[0].equals(conn.getSessionId()) && eligible == null)
			publish(topicId, event, true, null, null);
		else
			publish(topicId, event, false, exclude, eligible);
	}

	private void publish(String topicId, Object event, boolean excludeMe, String[] exclude, String[] eligible) throws IOException{
		
		if(!topics.contains(topicId))
			subscribe(topicId);
		
		WampPublishMessage msg = new WampPublishMessage();
		msg.setTopicUri(topicId);
		msg.setEvent(event);
		
		if(excludeMe)
			msg.setExcludeMe(true);
		else if(exclude != null || eligible != null){
			msg.setExclude(exclude != null ? exclude : new String[0]);
			msg.setEligible(eligible != null ? exclude : new String[0]);
		}
		
		conn.sendMessage(msg);
	}

	public EventListener getListener() {
		return listener;
	}

	public void setListener(EventListener listener) {
		this.listener = listener;
	}
}
