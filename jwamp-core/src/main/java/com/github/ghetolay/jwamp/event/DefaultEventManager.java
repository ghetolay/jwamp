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
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.endpoint.SessionManager;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.message.WampUnsubscribeMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampEventMessage;
import com.github.ghetolay.jwamp.utils.ActionMapping;
import com.github.ghetolay.jwamp.utils.MapActionMapping;

public class DefaultEventManager 
	implements WampMessageHandler, WampMessageHandler.Subscribe, WampMessageHandler.Unsubscribe, WampMessageHandler.Publish, EventSender {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private SessionManager sessionManager;
	
	//TODO changer action mapping
	private ActionMapping<EventAction> eventMapping;
	
	public DefaultEventManager(){
		eventMapping = new MapActionMapping<EventAction>();
	}
	
	public DefaultEventManager(ActionMapping<EventAction> eventMapping){
		if(eventMapping == null)
			throw new IllegalArgumentException("EventMapping can't be null");
		
		this.eventMapping = eventMapping;
		for(Iterator<EventAction> it = eventMapping.getActionsIterator(); it.hasNext();)
			it.next().setEventSender(this);
	}
	
	public void addEventAction(URI eventURI, EventAction eventAction){
		eventMapping.addAction(eventURI, eventAction);
		eventAction.setEventSender(this);
	}
	
	@Override
	public void init(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	@Override
	public void onOpen(String sessionId) {}

	public void onMessage(String sessionId, WampSubscribeMessage wampSubscribeMessage) {
		EventAction e = eventMapping.getAction(wampSubscribeMessage.getTopicURI());
		if(e != null)
			e.subscribe(sessionId);
		else if(log.isDebugEnabled())
			log.debug("unable to subscribe : action name doesn't not exist " + wampSubscribeMessage.getTopicURI());
	}

	public void onMessage(String sessionId, WampUnsubscribeMessage wampUnsubscribeMessage) {
		EventAction e = eventMapping.getAction(wampUnsubscribeMessage.getTopicURI());
		if(e != null)
			e.unsubscribe(sessionId);
		else if(log.isDebugEnabled())
			log.debug("unable to unsubscribe : action name doesn't not exist " + wampUnsubscribeMessage.getTopicURI());
	}

	//TODO new publish
	public void onMessage(String sessionId, WampPublishMessage wampPublishMessage) {
		EventAction e = eventMapping.getAction(wampPublishMessage.getTopicURI());
		if(e != null){
			OutputWampEventMessage msg = new OutputWampEventMessage();
			msg.setTopicURI(wampPublishMessage.getTopicURI());
			msg.setEvent( wampPublishMessage.getEvent() );
			
			//e.publishTo think about it
			List<String> publishList = e.publishTo( getPublishList(e, sessionId, wampPublishMessage), wampPublishMessage, msg);
			if(publishList != null)
				for(String s : publishList)
						sendEvent(s, msg);
			
		}else if(log.isDebugEnabled())
			log.debug("unable to publish : action name doesn't not exist " + wampPublishMessage.getTopicURI());
	}
	
	private List<String> getPublishList(EventAction e, String sessionId,WampPublishMessage wampPublishMessage){
		if(wampPublishMessage.getEligible() != null)
			return wampPublishMessage.getEligible();
		
		List<String> res;
		if(wampPublishMessage.getExclude() != null){
			res = new ArrayList<String>(e.getSubscriber());
			for(String s : wampPublishMessage.getExclude())
				res.remove(s);
		}
		else{ 
			if(wampPublishMessage.isExcludeMe()){
				res = new ArrayList<String>(e.getSubscriber());
				res.remove(sessionId);
			}else 
				res = new ArrayList<String>(e.getSubscriber());
		}
		
		return res;	
	}
	 
	public boolean sendEvent(String sessionId, URI eventURI, Object event) {		
		OutputWampEventMessage msg = new OutputWampEventMessage();
		msg.setTopicURI(eventURI);
		msg.setEvent(event);
			
		return sendEvent(sessionId, msg);
	}
	
	private boolean sendEvent(String sessionId, OutputWampEventMessage msg) {
		try {
			sessionManager.sendMessageTo(sessionId, msg);
			return true;
		} catch (IOException | EncodeException e) {
			if(log.isErrorEnabled())
				log.error("Unable to send event message : " + e.getMessage());
		}
		
		return false;
	}
	
	public void onClose(String sessionId, CloseReason closeReason) {
		for(Iterator<EventAction> it = eventMapping.getActionsIterator(); it.hasNext();)
			it.next().unsubscribe(sessionId);
	}
}
