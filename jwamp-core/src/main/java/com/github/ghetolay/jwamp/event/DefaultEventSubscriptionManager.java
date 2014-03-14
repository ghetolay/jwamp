/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import java.net.URI;

import com.github.ghetolay.jwamp.actions.ActionRegistry;
import com.github.ghetolay.jwamp.message.MessageSender;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.message.WampUnsubscribeMessage;

/**
 * @author Kevin
 *
 */
public class DefaultEventSubscriptionManager implements EventSubscriptionManager {

	private final ActionRegistry<EventAction> actionRegistry;
	private final MessageSender messageSender;

	public DefaultEventSubscriptionManager(MessageSender messageSender, ActionRegistry<EventAction> actionRegistry) {
		this.actionRegistry = actionRegistry;
		this.messageSender = messageSender;
	}

	private void sendMessage(WampMessage msg) {
		messageSender.sendToRemote(msg);
	}

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
