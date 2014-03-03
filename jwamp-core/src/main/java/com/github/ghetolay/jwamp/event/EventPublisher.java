/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import java.net.URI;
import java.util.Collection;

/**
 * @author Kevin
 *
 */
public interface EventPublisher {

	public void publishEvent(URI topicURI, Object event);

	public void publishEvent(URI topicURI, Object event, boolean excludeMe);

	public void publishEvent(URI topicURI, Object event, Collection<String> exclude);

	public void publishEvent(URI topicURI, Object event, Collection<String> exclude, Collection<String> eligible);

}