/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import com.github.ghetolay.jwamp.actions.ActionRegistry;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.session.WampSession;

/**
 * @author Kevin
 *
 */
public class LocalEventSubscriptionManager extends AbstractEventSubscriptionManager {

	private final WampSession session;
	private final PubSubMessageHandler pubSubMessageHandler;

	public LocalEventSubscriptionManager(WampSession session, PubSubMessageHandler pubSubMessageHandler, ActionRegistry<EventAction> actionRegistry) {
		super(actionRegistry);
		this.session = session;
		this.pubSubMessageHandler = pubSubMessageHandler;
	}

	@Override
	public void sendMessage(WampMessage msg) {
		pubSubMessageHandler.onMessage(session, msg);
	}

}
