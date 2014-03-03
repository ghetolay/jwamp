/**
 * 
 */
package com.github.ghetolay.jwamp.session;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import com.github.ghetolay.jwamp.endpoint.WampEndpointParameters;

/**
 * @author Kevin
 *
 */
public interface WampLifeCycleListener {

	public boolean beforeOpen(WampEndpointParameters wampEndpointParameters, Session session);
	public void onOpen(WampSession session);
	public void onClose(WampSession session, CloseReason closeReason);

}
