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

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class WampEventMessage extends WampMessage{

	private String topicId;
	
	private WampObjectArray event = new WampObjectArray();
	
	public WampEventMessage(){
		messageType = EVENT;
	}
	
	public WampEventMessage(JsonParser parser) throws BadMessageFormException{
		try {
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("TopicUri is required and must be a string");
			setTopicId(parser.getText());
			
			if(parser.nextToken() == JsonToken.END_ARRAY)
				throw new BadMessageFormException("Missing event element");
			
			event.setParser(parser, false);
		} catch (JsonParseException e) {
			throw new BadMessageFormException(e);
		} catch (IOException e) {
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public String toJSONMessage(ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		
		StringBuffer result = startMsg();
		
		appendString(result, topicId);
		
		if(event != null)
			event.toJSONMessage(result, objectMapper, false);
		else
			result.append(",null");
		
		return endMsg(result);
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	public WampObjectArray getEvent(){
		return event;
	}
	
	public void setEvent(WampObjectArray event){
		this.event = event;
	}
	
	public void addEventObject(Object obj){
		event.addObject(obj);
	}
}
