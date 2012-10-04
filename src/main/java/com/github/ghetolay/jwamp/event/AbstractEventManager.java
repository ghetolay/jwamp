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


import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.message.BadMessageFormException;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampObjectArray;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.utils.ActionMapping;

public abstract class AbstractEventManager implements WampMessageHandler, EventSender {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	//TODO changer action mapping
	private ActionMapping<EventAction> eventMapping;
	
	//TODO ability to change eventMapping dynamically 
	public AbstractEventManager(ActionMapping<EventAction> eventMapping){
		if(eventMapping == null)
			throw new IllegalArgumentException("EventMapping can't be null");
		
		this.eventMapping = eventMapping;
		for(Iterator<EventAction> it = eventMapping.getActionsIterator(); it.hasNext();)
			it.next().setEventSender(this);
	}
	
	public boolean onMessage(String sessionId, WampMessage message) throws BadMessageFormException {
		
		switch(message.getMessageType()){
			case WampMessage.SUBSCRIBE :
				onSubscribe(sessionId, (WampSubscribeMessage)message);
				return true;
			case WampMessage.UNSUBSCRIBE :
				onUnsubscribe(sessionId, (WampSubscribeMessage)message);
				return true;
			case WampMessage.PUBLISH :
				onPublish(sessionId, (WampPublishMessage)message);
				return true;
			default : 
				return false;
		}
	}

	public void onSubscribe(String sessionId, WampSubscribeMessage wampSubscribeMessage) {
		EventAction e = eventMapping.getAction(wampSubscribeMessage.getTopicId());
		if(e != null)
			e.subscribe(sessionId, wampSubscribeMessage.getArguments());
		else if(log.isDebugEnabled())
			log.debug("unable to subscribe : action name doesn't not exist " + wampSubscribeMessage.getTopicId());
	}

	public void onUnsubscribe(String sessionId, WampSubscribeMessage wampSubscribeMessage) {
		EventAction e = eventMapping.getAction(wampSubscribeMessage.getTopicId());
		if(e != null)
			e.unsubscribe(sessionId);
		else if(log.isDebugEnabled())
			log.debug("unable to unsubscribe : action name doesn't not exist " + wampSubscribeMessage.getTopicId());
	}

	public void onPublish(String sessionId, WampPublishMessage wampPublishMessage) {
		EventAction e = eventMapping.getAction(wampPublishMessage.getTopicId());
		if(e != null){
			WampEventMessage msg = new WampEventMessage();
			msg.setTopicId(wampPublishMessage.getTopicId());
			msg.setEvent(wampPublishMessage.getEvent());
			
			List<String> publishTo = e.publish(sessionId, wampPublishMessage, msg);
			if(publishTo != null)
				for(String s : publishTo)
					sendEvent(s, msg);
		}else if(log.isDebugEnabled())
			log.debug("unable to publish : action name doesn't not exist " + wampPublishMessage.getTopicId());
	}
	
	public void sendEvent(String sessionId, String eventId, WampObjectArray event){		
		WampEventMessage msg = new WampEventMessage();
		msg.setTopicId(eventId);
		msg.setEvent(event);
			
		sendEvent(sessionId, msg);
	}
	
	private void sendEvent(String sessionId, WampEventMessage msg){
		WampConnection con = getConnection(sessionId);
		if(con != null)
			try {
				con.sendMessage(msg);
			} catch (IOException e) {
				if(log.isErrorEnabled())
					log.error("Unable to send event message : " + e.getMessage());
			}
		else if(log.isWarnEnabled())
			log.warn("Unable to find connection : " + sessionId);
	}
	
	public void onClose(String sessionId, int closeCode) {
		for(Iterator<EventAction> it = eventMapping.getActionsIterator(); it.hasNext();)
			it.next().unsubscribe(sessionId);
	}
	
	protected abstract WampConnection getConnection(String sessionId);
}
