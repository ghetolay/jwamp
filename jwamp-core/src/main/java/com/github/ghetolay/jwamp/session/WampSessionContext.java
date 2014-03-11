/**
 * 
 */
package com.github.ghetolay.jwamp.session;

import javax.websocket.Session;

import com.github.ghetolay.jwamp.event.EventSubscriptionRegistry;
import com.github.ghetolay.jwamp.message.MessageSender;
import com.github.ghetolay.jwamp.message.WampMessageHandler;

/**
 * @author Kevin
 *
 */
public interface WampSessionContext {

	public WampSession getWampSession();

	public String getSessionId();

	public Session getWebSocketSession();

	public EventSubscriptionRegistry getEventSubscriptionRegistry();

	public WampMessageHandler getMessageHandler();

	public MessageSender getRemoteMessageSender();

}