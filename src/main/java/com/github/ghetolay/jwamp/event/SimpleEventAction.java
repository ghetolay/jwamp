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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.WampResult;

public class SimpleEventAction implements EventAction {

	protected EventSender sender; 
	protected Set<String> subscriber = new HashSet<String>();
	
	private String eventId;
	
	public void setEventId(String eventId){
		this.eventId = eventId;
	}
	
	public void setEventSender(EventSender sender) {
		this.sender = sender;
	}
	
	public void subscribe(String sessionId, WampArguments args) {
		subscriber.add(sessionId);
	}

	public void unsubscribe(String sessionId) {
		subscriber.remove(sessionId);
	}

	public List<String> publish(String sessionId, WampPublishMessage wampPublishMessage, WampEventMessage msg) {
		if(wampPublishMessage.getEligible() != null)
			return wampPublishMessage.getEligible();
		
		List<String> res;
		if(wampPublishMessage.getExclude() != null){
			res = new ArrayList<String>(subscriber);
			for(String s : wampPublishMessage.getExclude())
				res.remove(s);
		}
		else{ 
			if(wampPublishMessage.isExcludeMe()){
				res = new ArrayList<String>(subscriber);
				res.remove(sessionId);
			}else 
				res = new ArrayList<String>(subscriber);
		}
		
		return res;	
	}
	
	public void eventAll(WampResult event){
		for(String s : subscriber)
			if(sender != null)
				sender.sendEvent(s, eventId, event);
	}
}
