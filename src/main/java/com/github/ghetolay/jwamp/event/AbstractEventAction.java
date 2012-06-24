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


import java.util.HashSet;
import java.util.Set;

import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampPublishMessage;

public class AbstractEventAction implements EventAction {

	private EventSender listener; 
	private Set<String> subscriber = new HashSet<String>();
	
	public void addEventListener(EventSender listener) {
		this.listener = listener;
	}

	public void removeEventListener(EventSender listener) {
		this.listener = null;
	}
	
	public void subscribe(String sessionId) {
		subscriber.add(sessionId);
	}

	public void unsubscribe(String sessionId) {
		subscriber.remove(sessionId);
	}

	public String[] publish(String sessionId, WampPublishMessage wampPublishMessage, WampEventMessage msg) {
		if(wampPublishMessage.getEligible() != null)
			return wampPublishMessage.getEligible();
		
		Set<String> res;
		if(wampPublishMessage.getExclude() != null){
			res = new HashSet<String>(subscriber);
			for(String s : wampPublishMessage.getExclude())
				res.remove(s);
		}
		
		else{ 
			if(wampPublishMessage.isExcludeMe()){
				res = new HashSet<String>(subscriber);
				res.remove(sessionId);
			}else 
				res = subscriber;
		}
		
		return res.toArray(new String[0]);	
	}
	
	public void eventAll(Object event){
		for(String s : subscriber)
			if(listener != null)
				listener.sendEvent(s, this, event);
	}
}
