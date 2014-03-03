/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import com.github.ghetolay.jwamp.actions.ActionRegistry;
import com.github.ghetolay.jwamp.message.RemoteMessageSender;
import com.github.ghetolay.jwamp.message.WampMessage;

/**
 * @author Kevin
 *
 */
public class RemoteEventSubscriptionManager extends AbstractEventSubscriptionManager {

	private final RemoteMessageSender messageSender;

	public RemoteEventSubscriptionManager(RemoteMessageSender messageSender, ActionRegistry<EventAction> actionRegistry) {
		super(actionRegistry);
		this.messageSender = messageSender;
	}

	@Override
	public void sendMessage(WampMessage msg) {
		messageSender.sendToRemote(msg);
	}

}
