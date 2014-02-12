package com.github.ghetolay.jwamp.endpoint;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.message.WampMessage;

public abstract class AbstractSessionManager implements SessionManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private boolean asyncMode;

	@Override
	public void sendMessageTo(String sessionId, WampMessage message) throws IOException, EncodeException {
		sendMessageTo(sessionId, message, asyncMode);
	}

	@Override
	public void sendMessageTo(String sessionId, WampMessage message, boolean async) throws IOException, EncodeException {
		Session session = getWampSession(sessionId).getSession();
		
		if(session != null){
			if(async)
				session.getAsyncRemote().sendObject(message);
			else
				session.getBasicRemote().sendObject(message);
				
			if(log.isDebugEnabled())
				log.debug("Send Text Message " + message);
			
		}else if(log.isDebugEnabled())
			log.debug("Session : " + sessionId + " not found.");
	}


	@Override
	public void setAsyncMode(boolean async) {
		asyncMode = async;
	}

	@Override
	public boolean getAsyncMode() {
		return asyncMode;
	}

}
