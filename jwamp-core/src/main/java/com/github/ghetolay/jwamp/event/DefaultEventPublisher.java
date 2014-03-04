/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import com.github.ghetolay.jwamp.message.MessageSender;
import com.github.ghetolay.jwamp.message.WampPublishMessage;

/**
 * @author Kevin
 *
 */
public class DefaultEventPublisher extends AbstractEventPublisher {

	private final MessageSender remoteMessageSender;
	
	public DefaultEventPublisher(MessageSender remoteMessageSender) {
		this.remoteMessageSender = remoteMessageSender;
	}

	@Override
	protected void sendMessage(WampPublishMessage message) {
		remoteMessageSender.sendToRemote(message);
	}


}
