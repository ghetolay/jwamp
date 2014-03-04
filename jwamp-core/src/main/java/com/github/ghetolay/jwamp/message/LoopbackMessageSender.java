/**
 * 
 */
package com.github.ghetolay.jwamp.message;

import com.github.ghetolay.jwamp.session.WampSession;

public class LoopbackMessageSender implements MessageSender{
	private final WampSession session;
	private final WampMessageHandler targetHandler;
	
	public LoopbackMessageSender(WampSession session, WampMessageHandler targetHandler) {
		this.session = session;
		this.targetHandler = targetHandler;
	}
	
	@Override
	public void sendToRemote(WampMessage msg) {
		targetHandler.onMessage(session, msg);
	}
}