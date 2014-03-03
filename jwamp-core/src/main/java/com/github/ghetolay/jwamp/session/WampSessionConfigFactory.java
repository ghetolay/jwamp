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
public interface WampSessionConfigFactory {

	public WampSessionConfig createConfig(Session session, WampEndpointParameters endpointParameters, String wampSessionId);

}