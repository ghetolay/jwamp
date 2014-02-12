package com.github.ghetolay.jwamp.endpoint;

import javax.websocket.Session;

public interface WampSession {
	
	public Session getSession();
	
	public String getWampSessionId();
}
