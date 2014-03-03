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
package com.github.ghetolay.jwamp.message;

import java.net.URI;

import com.github.ghetolay.jwamp.utils.JsonBackedObject;


public class WampEventMessage extends WampMessage{
	private final URI topicURI;
	private final JsonBackedObject event;
	
	public static WampEventMessage createFromPublishMessage(WampPublishMessage publishMsg){
		WampEventMessage evtMsg = new WampEventMessage(publishMsg.getTopicURI(), publishMsg.getEvent());
		return evtMsg;
	}
	
	public static WampEventMessage create(URI topicURI, JsonBackedObject event){
		return new WampEventMessage(topicURI, event);
	}
	
	private WampEventMessage(URI topicURI, JsonBackedObject event){
		super(MessageType.EVENT);
		this.topicURI = topicURI;
		this.event = event;
	}

	public URI getTopicURI() {
		return topicURI;
	}

	public JsonBackedObject getEvent() {
		return event;
	}
	
	@Override
	public String toString(){
		return " WampEventMessage { "+ topicURI+ " , " + event  + " } ";
	}

}
