package com.github.ghetolay.jwamp.endpoint;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

public class DefaultSessionManager extends AbstractSessionManager {

	//TODO build new data structure
	Map<String, WampSession> map = new ConcurrentHashMap<String, WampSession>();
	
	@Override
	public void newSession(WampSession session) {
		map.put(session.getWampSessionId(), session);
	}

	@Override
	public void closeSession(Session session) {
		if(map.remove(session.getId()) == null)
			for(Entry<String, WampSession> s : map.entrySet())
				//which one is better ??
				//if(s.getValue().getSession().getId().equals(session.getId()))
				if(s.getValue().getSession().equals(session)){
					map.remove(s.getKey());
					return;
				}
	}

	@Override
	public WampSession getWampSession(String sessionId) {
		return map.get(sessionId);
	}

	@Override
	public Iterator<WampSession> getActiveWampSessions() {
		return map.values().iterator();
	}
}
