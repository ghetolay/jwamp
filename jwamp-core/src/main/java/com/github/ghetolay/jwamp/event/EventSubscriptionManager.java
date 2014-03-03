/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import java.net.URI;

/**
 * @author Kevin
 *
 */
public interface EventSubscriptionManager {
	public void subscribe(URI topic, EventAction action);
	public void unsubscribe(URI topic);
}
