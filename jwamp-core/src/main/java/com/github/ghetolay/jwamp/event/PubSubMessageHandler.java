/**
 *Copyright [2012] [Ghetolay]
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
package com.github.ghetolay.jwamp.event;

import java.util.Collection;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.message.MessageType;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampMessageHandler;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.message.WampUnsubscribeMessage;
import com.github.ghetolay.jwamp.session.SessionRegistry;
import com.github.ghetolay.jwamp.session.WampSession;
import com.github.ghetolay.jwamp.session.WampSessionConfig;

/**
 * @author ghetolay
 *
 */
public class PubSubMessageHandler implements WampMessageHandler {

	private final static Logger log = LoggerFactory.getLogger(PubSubMessageHandler.class);

	private final EventSubscriptionRegistry eventSubscriptionRegistry;
	private final SessionRegistry sessionRegistry;
	
	public PubSubMessageHandler(EventSubscriptionRegistry eventSubscriptionRegistry, SessionRegistry sessionRegistry) {
		this.eventSubscriptionRegistry = eventSubscriptionRegistry;
		this.sessionRegistry = sessionRegistry;
	}
	
	@Override
	public Collection<MessageType> getMessageTypes() {
		return EnumSet.of(MessageType.SUBSCRIBE, MessageType.UNSUBSCRIBE, MessageType.PUBLISH);
	}
	
	@Override
	public void onMessage(WampSession session, WampMessage msg) {
		
		switch(msg.getMessageType()){
			case SUBSCRIBE:
				onMessage(session, (WampSubscribeMessage)msg);
				break;
			case UNSUBSCRIBE:
				onMessage(session, (WampUnsubscribeMessage)msg);
				break;
			case PUBLISH:
				onMessage(session, (WampPublishMessage)msg);
				break;
			default:
				log.warn(this + " received unexpected message " + msg);
				return;
		}
		
	}

	private void onMessage(WampSession session, WampSubscribeMessage msg){
		eventSubscriptionRegistry.register(msg.getTopicURI());
	}
	
	private void onMessage(WampSession session, WampUnsubscribeMessage msg){
		eventSubscriptionRegistry.deregister(msg.getTopicURI());
	}

	private void onMessage(WampSession session, WampPublishMessage msg){
		// TODO: we can do better than this if we want - instead of looping through all sessionConfigs (if eligile isn't specified), somehow make a global event subscription manager that tracks which sessions are registered for which event URLs and query that for the list of sessions to notify
		Collection<WampSessionConfig> configsToNotify = msg.isEligibleSpecified() ? sessionRegistry.getSessionConfigs(msg.getEligible()) : sessionRegistry.getSessionConfigs();

		WampEventMessage eventMessage = WampEventMessage.createFromPublishMessage(msg);
		
		for(WampSessionConfig sessionConfig : configsToNotify){
			String targetSessionId = sessionConfig.getSessionId();
			String sourceSessionId = session.getWampSessionId();
			
			if (targetSessionId.equals(sourceSessionId) && msg.isExcludeMe())
				return;
			if (msg.isExcludeSpecified() && msg.getExclude().contains(targetSessionId))
				return;

			if (sessionConfig.getEventSubscriptionRegistry().isSubscribed(eventMessage.getTopicURI())){
				sessionConfig.getRemoteMessageSender().sendToRemote(eventMessage);
			}			
			
		}
	}


}


