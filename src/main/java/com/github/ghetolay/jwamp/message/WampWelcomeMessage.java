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

public class WampWelcomeMessage extends WampMessage{

	private String sessionId;
	private int protocolVersion;
	private String implementation;
	
	public WampWelcomeMessage(){
		messageType = WELCOME;
	}
	
	public WampWelcomeMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 4)
			throw BadMessageFormException.notEnoughParameter("Welcome", JSONArray.length, 4);
		
		try{
			setSessionId((String) JSONArray[1]);
			setProtocolVersion((Integer) JSONArray[2]);
			setImplementation((String) JSONArray[3]);
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	public WampWelcomeMessage(JsonParser parser) throws BadMessageFormException{
		this();
		
		try {
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("SessionId is required and must be a string");
			setSessionId(parser.getText());
			
			if(parser.nextToken() != JsonToken.VALUE_NUMBER_INT)
				throw new BadMessageFormException("ProtocolVersion is required and must be a int");
			setProtocolVersion(parser.getIntValue());
			
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("Implementation is required and must be a string");
			setImplementation(parser.getText());
			
		} catch (JsonParseException e) {
			throw new BadMessageFormException(e);
		} catch (IOException e) {
			throw new BadMessageFormException(e);
		}
		
		
	}
	
	@Override
	public String toJSONMessage(ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		
		StringBuffer result = startMsg();
		
		appendString(result, sessionId);
		result.append(',');
		result.append(protocolVersion);
		result.append(',');
		appendString(result, implementation);
		
		return endMsg(result);
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public String getImplementation() {
		return implementation;
	}

	public void setImplementation(String implementation) {
		this.implementation = implementation;
	}

}
