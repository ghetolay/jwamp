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
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class WampPublishMessage extends WampMessage{

	//TODO use WampArguments for event field/ problem: event is in the middle of the message not at the end
	
	private String topicId;
	private WampObjectArray event = new WampObjectArray();
	private List<String> exclude;
	private List<String> eligible;
	private boolean excludeMe;
	
	public WampPublishMessage(){
		messageType = PUBLISH;
	}
	
	public WampPublishMessage(JsonParser parser) throws BadMessageFormException{
		this();
		
		try {
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("TopicUri is required and must be a string");
			setTopicId(parser.getText());
			
			event.setParser(parser,false);
			
			//excludeme or exclude list
			if(parser.nextToken() != JsonToken.END_ARRAY){
				if(parser.getCurrentToken() == JsonToken.VALUE_TRUE)
					excludeMe = true;
				else if(parser.getCurrentToken() == JsonToken.START_ARRAY){
					exclude = new ArrayList<String>();
					while(parser.nextToken() != JsonToken.END_ARRAY){
						if(parser.getCurrentToken() != JsonToken.VALUE_STRING)
							throw new BadMessageFormException("Fourth element must be boolean or array of string");
						exclude.add(parser.getText());
					}
				}else
					throw new BadMessageFormException("Fourth element must be boolean or array of string");
				
				//eligible list
				if(parser.nextToken() != JsonToken.END_ARRAY){
					eligible = new ArrayList<String>();
					while(parser.nextToken() != JsonToken.END_ARRAY){
						if(parser.getCurrentToken() != JsonToken.VALUE_STRING)
							throw new BadMessageFormException("Fifth element must be an array of string");
						eligible.add(parser.getText());
					}
				}
			}
			
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
		
		if(excludeMe)
			result.append(",true");
		else
			if( eligible != null || exclude != null ){
				result.append(',');
				result.append(exclude==null?"[]":objectMapper.writeValueAsString(exclude));
				
				result.append(',');
				result.append(eligible==null?"[]":objectMapper.writeValueAsString(eligible));
			}
		
		return endMsg(result);
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	public WampObjectArray getEvent() {
		return event;
	}
	
	public void setEvent(WampObjectArray event) {
		this.event = event;
	}
	
	public void addEvent(Object event) {
		this.event.addObject(event);
	}

	public List<String> getExclude() {
		return exclude;
	}

	public void setExclude(List<String> exclude) {
		this.exclude = exclude;
	}

	public List<String> getEligible() {
		return eligible;
	}

	public void setEligible(List<String> eligible) {
		this.eligible = eligible;
	}

	public boolean isExcludeMe() {
		return excludeMe;
	}

	public void setExcludeMe(boolean excludeMe) {
		this.excludeMe = excludeMe;
	}

}
