package com.github.ghetolay.jwamp.endpoint;

import java.util.Iterator;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampMessageHandler;

public abstract class AbstractWampDispatcher implements WampDispatcher {

	protected final static Logger log = LoggerFactory.getLogger(AbstractWampDispatcher.class);
	
	protected SessionManager sessions;
	
	private boolean exclusiveHandler = true;
	
	protected AbstractWampDispatcher(SessionManager sessionManager){
		sessions = sessionManager;
	}
	
	public SessionManager getSessionManager() {
		return sessions;
	}

	public void newConnection(WampSession session){
		sessions.newSession(session);
		
		for(Iterator<WampMessageHandler> it = getHandlerIterator(); it.hasNext();)
			it.next().onOpen(session.getWampSessionId());
	}
	
	public void closeConnection(Session session, CloseReason reason){
		sessions.closeSession(session);
		
		for(Iterator<WampMessageHandler> it = getHandlerIterator(); it.hasNext();)
			it.next().onClose(session.getId(), reason);
	}
	
	protected abstract Iterator<WampMessageHandler> getHandlerIterator();
	
	@Override
	public void setExclusiveHandler(boolean exclusive){
		this.exclusiveHandler = exclusive;
	}

	@Override
	public boolean isExclusiveHandler(){
		return exclusiveHandler;
	}

	@Override
	public void addMessageHandler(WampMessageHandler handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsMessageHandler(WampMessageHandler handler) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeMessageHandler(WampMessageHandler handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends WampMessageHandler> T getMessageHandler(
			Class<T> handlerClass) {
		// TODO Auto-generated method stub
		return null;
	}

}
