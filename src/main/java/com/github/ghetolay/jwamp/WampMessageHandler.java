package com.github.ghetolay.jwamp;

import com.github.ghetolay.jwamp.message.BadMessageFormException;

public interface WampMessageHandler {
	
	public void onConnected(WampConnection connection);
	
	public boolean onMessage(String sessionId, int messageType, Object[] msg) throws BadMessageFormException;
	public void onClose(String sessionId, int closeCode);
}
