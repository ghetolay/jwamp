package com.github.ghetolay.jwamp.endpoint;

import javax.websocket.Session;

public class DifferWampSession implements WampSession{
	
	private Session session;
	private String wampSessionId;
	
	public DifferWampSession(Session session, String wampSessionId){
		this.session = session;
		this.wampSessionId = wampSessionId;
	}
	
	public Session getSession(){
		return session;
	}
	
	public String getWampSessionId(){
		return wampSessionId;
	}
}
