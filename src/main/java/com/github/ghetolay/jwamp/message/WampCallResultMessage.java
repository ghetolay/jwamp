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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.type.TypeReference;


public class WampCallResultMessage extends WampMessage{

	private String callId;
	private Object result;
	
	private JsonParser parser;
	
	public WampCallResultMessage(){
		messageType = CALLRESULT;
	}
	
	public WampCallResultMessage(boolean last){
		messageType = last?CALLRESULT:CALLMORERESULT;
	}
	
	public WampCallResultMessage(Object[] JSONArray) throws BadMessageFormException{
		if(JSONArray.length < 3)
			throw BadMessageFormException.notEnoughParameter("CallResult", JSONArray.length, 3);
		
		try{
			messageType = (Integer)JSONArray[0];
			setCallId((String) JSONArray[1]);
			setResult(JSONArray[2]);
			
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	public WampCallResultMessage(JsonParser parser) throws BadMessageFormException{
		this(CALLRESULT, parser);
	}
	
	public WampCallResultMessage(int messageType, JsonParser parser) throws BadMessageFormException{
		this.messageType = messageType;
		
		try {
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("CallId is required and must be a string");
			setCallId(parser.getText());
			
			this.parser = parser;
		} catch (JsonParseException e) {
			throw new BadMessageFormException(e);
		} catch (IOException e) {
			throw new BadMessageFormException(e);
		}
	}
	
	
	@Override
	public Object[] toJSONArray() {
		return new Object[]{messageType, callId, result};
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	//TODO logging
	public Object getResult(){
		try {
			return getResult(Object.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public <T> T getResult(Class<T> c) throws JsonProcessingException, IOException{
		if(parser == null)
			if(c.isInstance(result))
				return c.cast(result);
			else
				return null;
		
		if(parser.nextToken() == JsonToken.END_ARRAY){
			parser = null;
			return null;
		}
			
		T res = parser.readValueAs(c);
		
		result = res;
		return res;
	}
	
	public <T> T getNextResult(TypeReference<T> t) throws JsonParseException, IOException{
		if(parser == null)
			return null;
		
		JsonToken token = parser.nextToken();
		
		if(token == JsonToken.START_ARRAY)
			token = parser.nextToken();
		
		if(token == JsonToken.END_ARRAY){
			parser = null;
			return null;
		}
			
		return parser.readValueAs(t);
	}
	
	public <T> T getNextResult(Class<T> c) throws JsonParseException, IOException{
		if(parser == null)
			return null;
		
		JsonToken token = parser.nextToken();
		
		if(token == JsonToken.START_ARRAY)
			token = parser.nextToken();
		
		if(token == JsonToken.END_ARRAY){
			parser = null;
			return null;
		}
			
		return parser.readValueAs(c);
	}
	
	public void setResult(Object result) {
		this.result = result;
	}

	public boolean isLast(){
		return messageType != CALLMORERESULT;
	}
}
