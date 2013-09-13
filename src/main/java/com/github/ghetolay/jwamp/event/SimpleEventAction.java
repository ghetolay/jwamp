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


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleEventAction implements EventAction {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	protected EventSender sender; 
	protected Set<String> subscriber = new HashSet<String>();
	
	private String eventId;
	
	public void setEventId(String eventId){
		this.eventId = eventId;
	}
	
	public void setEventSender(EventSender sender) {
		this.sender = sender;
	}
	
	public void subscribe(String sessionId) {
		subscriber.add(sessionId);
	}

	public void unsubscribe(String sessionId) {
		subscriber.remove(sessionId);
	}

	public Set<String> getSubscriber(){
		return Collections.unmodifiableSet(subscriber);
	}
	
	public void eventAll(Object event){
		for(String s : subscriber)
			if(sender != null)
				try{
					sender.sendEvent(s, eventId, event);
				}catch(Exception e){
					//TODO log
					log.trace("eventall ",e);
				}
	}
}
