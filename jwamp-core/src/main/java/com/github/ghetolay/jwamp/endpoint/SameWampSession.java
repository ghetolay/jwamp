package com.github.ghetolay.jwamp.endpoint;

import javax.websocket.Session;

public class SameWampSession implements WampSession{
	
	private Session session;
	
	public SameWampSession(Session session){
		this.session = session;
	}
	
	public Session getSession(){
		return session;
	}
	
	public String getWampSessionId(){
		return session.getId();
	}
}
