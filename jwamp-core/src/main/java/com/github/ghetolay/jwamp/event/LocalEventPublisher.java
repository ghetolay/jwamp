/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.session.WampSession;

/**
 * @author Kevin
 *
 */
public class LocalEventPublisher extends AbstractEventPublisher {
	private final WampSession session;
	private final PubSubMessageHandler pubSubMessageHandler;
	
	public LocalEventPublisher(WampSession session, PubSubMessageHandler pubSubMessageHandler) {
		this.session = session;
		this.pubSubMessageHandler = pubSubMessageHandler;
	}

	@Override
	protected void sendMessage(WampPublishMessage message) {
		pubSubMessageHandler.onMessage(session, message);
	}


}
