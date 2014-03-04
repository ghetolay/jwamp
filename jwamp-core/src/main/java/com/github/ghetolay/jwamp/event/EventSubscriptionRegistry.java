/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Kevin
 *
 */
public class EventSubscriptionRegistry {
	private final Set<URI> subscriptions = new ConcurrentSkipListSet<URI>();
	
	public EventSubscriptionRegistry() {
	}
	
	public void register(URI topicUri){
		subscriptions.add(topicUri);
	}
	
	public void deregister(URI topicUri){
		subscriptions.remove(topicUri);
	}
	
	public boolean isSubscribed(URI topicUri){
		return subscriptions.contains(topicUri);
	}
	


}
