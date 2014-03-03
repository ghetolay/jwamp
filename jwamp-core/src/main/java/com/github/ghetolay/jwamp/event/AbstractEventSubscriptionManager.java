/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import java.net.URI;

import com.github.ghetolay.jwamp.actions.ActionRegistry;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.message.WampUnsubscribeMessage;

/**
 * Provides a single interface to allow for user-side subscribe/unsubscribe and publish requests.  The actual publish mechanism is deferred to sub-classes (servers need to send the publish message internally, clients need to send the publich message to the remote server)
 * This object keeps the remote registration information for each topicURI in sync with the action registration that should handle events for topicURI
 */
public abstract class AbstractEventSubscriptionManager implements EventSubscriptionManager {

	private final ActionRegistry<EventAction> actionRegistry;
	
	public AbstractEventSubscriptionManager(ActionRegistry<EventAction> actionRegistry) {
		this.actionRegistry = actionRegistry;
	}

	abstract public void sendMessage(WampMessage msg);

	@Override
	public void subscribe(URI topicUri, EventAction action) {
		actionRegistry.registerAction(topicUri, action);
		WampSubscribeMessage msg = WampSubscribeMessage.create(topicUri);
		sendMessage(msg);
	}

	@Override
	public void unsubscribe(URI topicUri) {
		WampUnsubscribeMessage msg = WampUnsubscribeMessage.create(topicUri);
		sendMessage(msg);
		actionRegistry.unregisterAction(topicUri);
	}

}
