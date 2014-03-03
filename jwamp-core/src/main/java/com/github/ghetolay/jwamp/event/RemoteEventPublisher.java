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
public class RemoteEventPublisher extends AbstractEventPublisher {

	private final RemoteMessageSender remoteMessageSender;
	
	public RemoteEventPublisher(RemoteMessageSender remoteMessageSender) {
		this.remoteMessageSender = remoteMessageSender;
	}

	@Override
	protected void sendMessage(WampPublishMessage message) {
		remoteMessageSender.sendToRemote(message);
	}


}
