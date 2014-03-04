/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import com.github.ghetolay.jwamp.message.RemoteMessageSender;
import com.github.ghetolay.jwamp.message.WampPublishMessage;

/**
 * @author Kevin
 *
 */
public class DefaultEventPublisher extends AbstractEventPublisher {

	private final RemoteMessageSender remoteMessageSender;
	
	public DefaultEventPublisher(RemoteMessageSender remoteMessageSender) {
		this.remoteMessageSender = remoteMessageSender;
	}

	@Override
	protected void sendMessage(WampPublishMessage message) {
		remoteMessageSender.sendToRemote(message);
	}


}
