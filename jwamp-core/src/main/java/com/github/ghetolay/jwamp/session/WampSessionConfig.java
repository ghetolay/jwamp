/**
 * 
 */
package com.github.ghetolay.jwamp.session;

import javax.websocket.Session;

import com.github.ghetolay.jwamp.event.EventSubscriptionRegistry;
import com.github.ghetolay.jwamp.message.RemoteMessageSender;
import com.github.ghetolay.jwamp.message.WampMessageHandler;

/**
 * @author Kevin
 *
 */
public interface WampSessionConfig {

	public WampSession getWampSession();

	public String getSessionId();

	public Session getWebSocketSession();

	public EventSubscriptionRegistry getEventSubscriptionRegistry();

	public WampMessageHandler getMessageHandler();

	public RemoteMessageSender getRemoteMessageSender();

}