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

public class WampCallMessage extends WampMessage{

	private String callId;
	private String procId;
	
	private WampArguments args = new WampArguments();
	
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
			
			if(JSONArray.length > 3)
				for(int i = 3 ; i < JSONArray.length; i++)
					args.addArgument(JSONArray[i]);
			
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
			
			JsonToken token = parser.nextToken();
			if(token != null && token != JsonToken.END_ARRAY)
				args.setParser(parser);
			
		} catch (JsonParseException e) {
			throw new BadMessageFormException(e);
		} catch (IOException e) {
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public String toJSONMessage(ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		
		StringBuffer result = startMsg(); 
		
		appendString(result, callId);
		result.append(',');
		appendString(result, procId);
		
		if(args.getArguments() != null)
			args.toJSONMessage(result, objectMapper);
		
		result.append(']');
		
		return endMsg(result);
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
	
	public WampArguments getArguments(){
		return args;
	}
	
	public void addArgument(Object arg) {
		args.addArgument(arg);
	}
}
