package com.github.ghetolay.jwamp.endpoint;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author ghetolay
 *
 */
public abstract class ExtendedWampEndpoint extends Endpoint{

	protected static final Logger log = LoggerFactory.getLogger(ExtendedWampEndpoint.class);
	
	protected WampDispatcher dispatcher;
	
	public ExtendedWampEndpoint(){
	}
	
	public ExtendedWampEndpoint(WampDispatcher dispatcher){
		this.dispatcher = dispatcher;
	}
	
	public void onClose(Session session, CloseReason closeReason) {
		dispatcher.closeConnection(session, closeReason);
	}

}
