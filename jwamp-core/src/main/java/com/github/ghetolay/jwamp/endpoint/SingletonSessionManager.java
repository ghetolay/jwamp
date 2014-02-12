package com.github.ghetolay.jwamp.endpoint;

import java.util.Iterator;

import javax.websocket.Session;

public class SingletonSessionManager extends AbstractSessionManager{

	private WampSession session;
	
	@Override
	public void newSession(WampSession session) {
		this.session = session;
	}

	@Override
	public void closeSession(Session session) {
		//if(!session.getId().equals( this.session.getSession().getId() ))
		if(!session.equals( this.session.getSession() ))
			throw new IllegalStateException("Wrong session !");
		
		this.session = null;
	}

	@Override
	public WampSession getWampSession(String id) {
		if(session == null)
			throw new IllegalStateException("No active session");
		
		if(session.getWampSessionId().equals(id))
			return session;
		
		return null;
	}

	@Override
	public Iterator<WampSession> getActiveWampSessions() {
		return new Iterator<WampSession>(){

			boolean hasNext = true;
			
			@Override
			public boolean hasNext() {
				return hasNext;
			}

			@Override
			public WampSession next() {
				if(hasNext){
					hasNext = false;
					return session;
				}
				
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
