/**
 * 
 */
package com.github.ghetolay.jwamp.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

/**
 * @author Kevin
 *
 */
public class SessionRegistry {

	private final ConcurrentHashMap<String, WampSessionConfig> sessionConfigs = new ConcurrentHashMap<String, WampSessionConfig>();
	private final static String USER_PROPERTY = WampSessionConfig.class.getName();
	
	public SessionRegistry() {
	}

	public void register(WampSessionConfig sessionConfig){
		WampSessionConfig existing = sessionConfigs.putIfAbsent(sessionConfig.getSessionId(), sessionConfig);
		if (existing != null)
			throw new IllegalStateException(sessionConfig.getSessionId() + " is already registered");

		// we do the following registration so we can easily retrieve it in getWampSessionConfigForWebSocketSession
		sessionConfig.getWebSocketSession().getUserProperties().put(USER_PROPERTY, sessionConfig);
	}
	
	public void deregister(WampSessionConfig sessionConfig){
		if (!sessionConfigs.remove(sessionConfig.getSessionId(), sessionConfig))
			throw new IllegalStateException(sessionConfig.getSessionId() + " is not registered");
		
		WampSessionConfig removed = (WampSessionConfig)sessionConfig.getWebSocketSession().getUserProperties().remove(USER_PROPERTY);
		
		if (removed != sessionConfig){
			sessionConfig.getWebSocketSession().getUserProperties().put(USER_PROPERTY, removed);
			throw new IllegalStateException("Someone messed with the " + USER_PROPERTY + " user property");
		}
	}
	
	public Collection<WampSessionConfig> getSessionConfigs(){
		return Collections.unmodifiableCollection(sessionConfigs.values());
	}
	
	public Collection<WampSessionConfig> getSessionConfigs(Collection<String> sessionIds){
		List<WampSessionConfig> rslt = new ArrayList<WampSessionConfig>(sessionIds.size());
		for (String sessionId : sessionIds) {
			rslt.add(sessionConfigs.get(sessionId));
		}
		return rslt;
	}
	
	public WampSessionConfig getWampSessionConfigForWebSocketSession(Session session){
		// the only problem with using user properties like this is that a handler with access to the user properties could muck with the value and cause problems
		// if we are concerned about that, we can just look the values of the sessions map and check for equality...
		return (WampSessionConfig)session.getUserProperties().get(USER_PROPERTY);
	}
}
