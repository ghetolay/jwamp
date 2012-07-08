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
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;

public class WampCallMessage extends WampMessage{

	private String callId;
	private String procId;
	private List<Object> args;
	
	private JsonParser parser; 
	
	public WampCallMessage(){
		messageType = CALL;
	}
	
	public WampCallMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 3)
			throw BadMessageFormException.notEnoughParameter("Call", JSONArray.length, 3);
		
		try{
			setCallId((String) JSONArray[1]);
			setProcId((String) JSONArray[2]);
			
			if(JSONArray.length > 3){
				args = new ArrayList<Object>(JSONArray.length - 3);
				for(int i = 3 ; i < JSONArray.length; i++)
					args.add(JSONArray[i]);
			}
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	public WampCallMessage(JsonParser parser) throws BadMessageFormException{
		this();
		
		try {
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("CallId is required and must be a string");
			setCallId(parser.getText());
			
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("ProcUri is required and must be a string");
			setProcId(parser.getText());

			this.parser = parser;
		} catch (JsonParseException e) {
			throw new BadMessageFormException(e);
		} catch (IOException e) {
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public Object[] toJSONArray() {
		int argsLength = 0;
		if(args != null)
			argsLength = args.size();
			
		Object[] result = new Object[argsLength + 3];
		
		result[0] = messageType;
		result[1] = callId;
		result[2] = procId;
		
		for(int i = argsLength - 1; i >= 0; i--)
			result[i + 3] = args .get(i);
		
		return result;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String getProcId() {
		return procId;
	}

	public void setProcId(String procId) {
		this.procId = procId;
	}

	//TODO: pas super le logging
	public List<Object> getArguments(){
		if(parser==null)
			return args;
		
		try{
			args = new ArrayList<Object>();
			
			while(parser.nextToken() != JsonToken.END_ARRAY)
				args.add(parser.readValueAs(Object.class));
		}catch(Exception e){
			log.error("ParseException",e);
		}
		
		parser = null;
		return args;
	}

	public Object nextArgument() throws JsonProcessingException, IOException{
		return nextArgument(Object.class);
	}
	
	public <T> T nextArgument(Class<T> c) throws JsonProcessingException, IOException{
		if(parser == null)
			return null;
		
		if(parser.nextToken() == JsonToken.END_ARRAY){
			parser = null;
			return null;
		}
		
		return parser.readValueAs(c);
	}
	
	public void setArguments(List<Object> args){
		this.args = args;
	}
	
	@SuppressWarnings("unchecked")
	public void setArgument(Object arg) {
		if(arg instanceof List)
			this.args = (List<Object>) arg;
		
		this.args = Arrays.asList(arg);
	}

}
