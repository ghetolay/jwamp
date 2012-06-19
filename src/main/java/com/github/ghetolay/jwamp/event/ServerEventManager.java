package com.github.ghetolay.jwamp.event;


import java.util.HashMap;
import java.util.Map;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.utils.ActionMapping;

public class ServerEventManager extends AbstractEventManager{
	
	private Map<String,WampConnection> connections = new HashMap<String,WampConnection>();
	
	
	public ServerEventManager(ActionMapping<EventAction> am){
		super(am);
	}
	
	public void onConnected(WampConnection connection) {
		connections.put(connection.getSessionId(),connection);
	}

	@Override
	protected WampConnection getConnection(String sessionId) {
		return connections.get(sessionId);
	}
	
	@Override
	public void onClose(String sessionId, int closeCode){
		super.onClose(sessionId, closeCode);
		
		connections.remove(sessionId);
	}
}
