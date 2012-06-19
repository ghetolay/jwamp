package com.github.ghetolay.jwamp.event;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.utils.ActionMapping;


public class ClientEventManager extends AbstractEventManager{
	
	private WampConnection connection;
	
	public ClientEventManager(ActionMapping<EventAction> am){
		super(am);
	}
	
	public void onConnected(WampConnection connection) {
		this.connection = connection;
	}

	@Override
	protected WampConnection getConnection(String sessionId) {
		if(connection.getSessionId().equals(sessionId))
			return connection;
		else
			return null;
	}
	
	@Override
	public void onClose(String sessionId, int closeCode){
		super.onClose(sessionId, closeCode);
		
		connection = null;
	}
}
