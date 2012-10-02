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


public class WampCallResultMessage extends WampMessage{

	private String callId;
	private WampResult result = new WampResult();
	
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
			
			List<Object> resultList = new ArrayList<Object>(1);
			resultList.add(JSONArray[2]);
			this.result.setArguments(resultList);
			
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
			
			result.setParser(parser);
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
		
		this.result.toJSONMessage(result, objectMapper);
		
		return endMsg(result);
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public WampResult getResult(){
		return result;
	}
	
	public void setResult(WampResult args){
		this.result = args;
	}
	
	public void addResult(Object args){
		this.result.addArgument(args);
	}

	public boolean isLast(){
		return messageType != CALLMORERESULT;
	}
}
