/**
 * 
 */
package com.github.ghetolay.jwamp.session;

import javax.websocket.Session;

import com.github.ghetolay.jwamp.endpoint.WampEndpointParameters;

/**
 * @author Kevin
 *
 */
public interface WampSessionContextFactory {

	public WampSessionContext createContext(Session session, WampEndpointParameters endpointParameters, String wampSessionId);

}