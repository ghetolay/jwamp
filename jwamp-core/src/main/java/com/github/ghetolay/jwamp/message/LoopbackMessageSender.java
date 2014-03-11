/**
 * 
 */
package com.github.ghetolay.jwamp.message;

import com.github.ghetolay.jwamp.session.WampSessionProvider;

public class LoopbackMessageSender implements MessageSender{
	private final WampSessionProvider sessionProvider;
	private final WampMessageHandler targetHandler;
	
	public LoopbackMessageSender(WampSessionProvider sessionProvider, WampMessageHandler targetHandler) {
		this.sessionProvider = sessionProvider;
		this.targetHandler = targetHandler;
	}
	
	@Override
	public void sendToRemote(WampMessage msg) {
		targetHandler.onMessage(sessionProvider.getWampSession(), msg);
	}
}