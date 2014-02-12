package com.github.ghetolay.jwamp.endpoint;

import java.io.IOException;
import java.util.Iterator;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.github.ghetolay.jwamp.message.WampMessage;

public interface SessionManager {

	void newSession(WampSession session);
	
	void closeSession(Session session);
	
	/**
	 * Use it to access UserProperties.
	 * To send message use one of sendMessageTo() methods.
	 * 
	 * @param sessionId
	 * @return
	 * @see #sendMessageTo(String, WampMessage)
	 * @see #sendMessageTo(String, WampMessage, boolean)
	 * @see Session
	 */
	public WampSession getWampSession(String sessionId);
	
	public Iterator<WampSession> getActiveWampSessions();
	
	public void sendMessageTo(String sessionId, WampMessage message) throws IOException, EncodeException;
	
	public void sendMessageTo(String sessionId, WampMessage message, boolean async) throws IOException, EncodeException;
	
	public void setAsyncMode(boolean async);
	public boolean getAsyncMode();
}
