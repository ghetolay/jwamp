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


public class WampCallResultMessage extends WampMessage{

	private String callId;
	private WampObjectArray result;
	
	private WampCallResultMessage(int messageType){
		this.messageType = messageType;
		
		result = new WampObjectArray();
	}
	
	public WampCallResultMessage(){
		this(CALLRESULT);
	}
	
	public WampCallResultMessage(boolean last){
		this(last?CALLRESULT:CALLMORERESULT);	
	}
	
	public WampCallResultMessage(JsonParser parser, boolean last) throws BadMessageFormException{
		this(last);
		
		try {
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("CallId is required and must be a string");
			setCallId(parser.getText());
			if(parser.nextToken() == JsonToken.END_ARRAY)
				throw new BadMessageFormException("Missing event element");
			
			result.setParser(parser,false);
			
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
		if(this.result != null)
			this.result.toJSONMessage(result, objectMapper, false);
		else	
			result.append(",null");
		
		return endMsg(result);
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public WampObjectArray getResult(){
		return result;
	}
	
	public void setResult(WampObjectArray args){		
		this.result = args;
	}
	
	public void addResult(Object args){
		this.result.addObject(args);
	}

	public boolean isLast(){
		return messageType != CALLMORERESULT;
	}
}
