/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.ghetolay.jwamp.message.RemoteMessageSender;
import com.github.ghetolay.jwamp.message.WampEventMessage;

/**
 * @author Kevin
 *
 */
public class EventSubscriptionRegistry {
	private final Set<URI> subscriptions = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
	
	private final RemoteMessageSender remoteMessageSender;
	
	public EventSubscriptionRegistry(RemoteMessageSender remoteMessageSender) {
		this.remoteMessageSender = remoteMessageSender;
	}
	
	public void register(URI topicUri){
		subscriptions.add(topicUri);
	}
	
	public void deregister(URI topicUri){
		subscriptions.remove(topicUri);
	}
	
	public void publish(String sourceSessionId, WampEventMessage eventMessage){
		if (!subscriptions.contains(eventMessage.getTopicURI()))
			return;
		
		remoteMessageSender.sendToRemote(eventMessage);
	}

}
