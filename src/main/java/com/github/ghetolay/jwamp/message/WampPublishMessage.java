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
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class WampPublishMessage extends WampMessage{

	private String topicId;
	private Object event;
	private List<String> exclude;
	private List<String> eligible;
	private boolean excludeMe;
	
	public WampPublishMessage(){
		messageType = PUBLISH;
	}
	
	@SuppressWarnings("unchecked")
	public WampPublishMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 3)
			throw BadMessageFormException.notEnoughParameter("Publish", JSONArray.length, 3);
		
		try{
			
			setTopicId((String) JSONArray[1]);
			setEvent(JSONArray[2]);
			
			if(JSONArray.length > 4)
				if(JSONArray[4] instanceof Boolean)
					setExcludeMe((Boolean) JSONArray[4]);
				else
					setExclude((List<String>) JSONArray[4]);
			
			if(JSONArray.length > 5)
				setEligible((List<String>) JSONArray[5]);
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	public WampPublishMessage(JsonParser parser) throws BadMessageFormException{
		this();
		
		try {
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("TopicUri is required and must be a string");
			setTopicId(parser.getText());
			
			event = parser.readValueAs(Object.class);
			
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
	public Object[] toJSONArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	public Object getEvent() {
		return event;
	}

	public <T> T getEvent(Class<T> c) throws Exception{
		if(c.isInstance(event))
			return c.cast(event);
		
		if(event instanceof Map<?, ?>){
			T result = c.newInstance();
			@SuppressWarnings("unchecked")
			Map<String,Object> eventMap = (Map<String, Object>) event;
			for(Entry<String,Object> entry : eventMap.entrySet())
				c.getMethod("set"+entry.getKey(), entry.getValue().getClass()).invoke(result, entry.getValue());
			
			event = result;
			return result;
		}
		
		return null;
	}
	
	public void setEvent(Object event) {
		this.event = event;
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
