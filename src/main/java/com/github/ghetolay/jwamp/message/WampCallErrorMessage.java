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


public class WampCallErrorMessage extends WampCallResultMessage{

	private String errorUri;
	private String errorDesc;
	private String errorDetails;
	
	
	public WampCallErrorMessage(){
		messageType = CALLERROR;
	}
	
	public WampCallErrorMessage(String... args){
	
		try{
			setCallId(args[0]);
			setErrorUri(args[1]);
			setErrorDesc(args[2]);
			setErrorDetails(args[3]);
		}catch(IndexOutOfBoundsException e){}
	}
	
	public WampCallErrorMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 4)
			throw BadMessageFormException.notEnoughParameter("CallError", JSONArray.length, 4);
		
		try{
			
			setCallId((String) JSONArray[1]);
			setErrorUri((String) JSONArray[2]);
			setErrorDesc((String) JSONArray[3]);
			
			if(JSONArray.length > 4)
				setErrorDetails((String) JSONArray[4]);
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	public WampCallErrorMessage(JsonParser parser) throws BadMessageFormException{
		this();
		
		try {
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("CallId is required and must be a string");
			setCallId(parser.getText());
			
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("ErrorUri is required and must be a string");
			setErrorUri(parser.getText());

			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("ErrorDescription is required and must be a string");
			setErrorDesc(parser.getText());
			
			if(parser.nextToken() != JsonToken.END_ARRAY){
				if(parser.nextToken() != JsonToken.VALUE_STRING)
					throw new BadMessageFormException("ErrorDetails must be a string");
				setErrorDetails(parser.getText());
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
		
		result.append(',');
		appendString(result, getCallId());
		result.append(',');
		appendString(result, errorUri);
		result.append(',');
		appendString(result, errorDesc);
		
		if(errorDetails != null && !errorDetails.isEmpty()){
			result.append(',');
			appendString(result, errorUri);
		}
		
		return endMsg(result);
	}
	
	public String getErrorUri() {
		return errorUri;
	}

	public void setErrorUri(String errorUri) {
		this.errorUri = errorUri;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}

}
