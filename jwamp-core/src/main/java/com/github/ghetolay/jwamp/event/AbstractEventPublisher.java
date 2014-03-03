/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import java.net.URI;
import java.util.Collection;

import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.utils.JsonBackedObject;
import com.github.ghetolay.jwamp.utils.JsonBackedObjectFactory;

/**
 * @author Kevin
 *
 */
public abstract class AbstractEventPublisher implements EventPublisher {

	/**
	 * 
	 */
	public AbstractEventPublisher() {
		// TODO Auto-generated constructor stub
	}

	abstract protected void sendMessage(WampPublishMessage message);
	
	@Override
	public void publishEvent(URI topicURI, Object event) {
		JsonBackedObject jsonEvent = JsonBackedObjectFactory.createForObject(event);
		WampPublishMessage message = WampPublishMessage.createSimple(topicURI, jsonEvent); 
		
		sendMessage(message);
		
	}

	@Override
	public void publishEvent(URI topicURI, Object event, boolean excludeMe){
		JsonBackedObject jsonEvent = JsonBackedObjectFactory.createForObject(event);
		WampPublishMessage message = WampPublishMessage.createExcludeMe(topicURI, jsonEvent, excludeMe);
		
		sendMessage(message);
	}
	
	@Override
	public void publishEvent(URI topicURI, Object event, Collection<String> exclude){
		JsonBackedObject jsonEvent = JsonBackedObjectFactory.createForObject(event);
		WampPublishMessage message = WampPublishMessage.createWithExclude(topicURI, jsonEvent, exclude);
		
		sendMessage(message);
	}
	
	@Override
	public void publishEvent(URI topicURI, Object event, Collection<String> exclude, Collection<String> eligible){
		JsonBackedObject jsonEvent = JsonBackedObjectFactory.createForObject(event);
		WampPublishMessage message = WampPublishMessage.createWithExcludeAndEligible(topicURI, jsonEvent, exclude, eligible);
		
		sendMessage(message);
	}

}
