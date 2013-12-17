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

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;


/**
 * @author ghetolay
 *
 */
public class WampMessageDeserializer {

	public static WampMessage deserialize(JsonParser parser) throws SerializationException{

		try{
			if(parser.nextToken() != JsonToken.START_ARRAY)
				throw new BadMessageFormException("WampMessage must be a not null JSON array");

			if(parser.nextToken() != JsonToken.VALUE_NUMBER_INT)
				throw new BadMessageFormException("The first array element must be a int");

			int messageType = parser.getIntValue();

			switch(messageType){
			case WampMessage.CALL :
				return callMsg(parser);

			case WampMessage.CALLERROR :
				return callErrorMsg(parser);

			case WampMessage.CALLRESULT :
				return callResultMsg(parser);

			case WampMessage.EVENT :
				return eventMsg(parser);

			case WampMessage.PUBLISH :
				return publishMsg(parser);

			case WampMessage.SUBSCRIBE :
				return subscribeMsg(parser);

			case WampMessage.UNSUBSCRIBE :	
				return unsubscribeMsg(parser);

			case WampMessage.WELCOME :
				return welcomeMsg(parser);

			default :
				throw new SerializationException("Unknown message type : " + messageType);
			}
		} catch(SerializationException e){
			throw e;
		} catch(Exception e){
			throw new SerializationException(e);
		}
	}

	public static WampCallErrorMessage callErrorMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampCallErrorMessage result = new WampCallErrorMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("CallId is required and must be a string");
		result.callId = parser.getText();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("ErrorUri is required and must be a string");
		result.errorUri = parser.getText();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("ErrorDescription is required and must be a string");
		result.errorDesc = parser.getText();

		JsonToken nextToken = parser.nextToken();
		if(nextToken != JsonToken.END_ARRAY){
			if(nextToken != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("ErrorDetails must be a string");
			result.errorDetails = parser.getText();
		}

		return result;
	}

	public static WampCallMessage callMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampCallMessage result = new WampCallMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("CallId is required and must be a string");
		result.callId = parser.getText();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("ProcUri is required and must be a string");
		result.procId = parser.getText();

		parser.nextToken();
		result.args = new JSONArguments(parser);

		return result;
	}

	public static WampCallResultMessage callResultMsg(JsonParser parser) throws BadMessageFormException, IOException{
		return callResultMsg(parser, true);
	}

	public static WampCallResultMessage callMoreResultMsg(JsonParser parser) throws BadMessageFormException, IOException{
		return callResultMsg(parser,false);
	}


	private static WampCallResultMessage callResultMsg(JsonParser parser, boolean last) throws BadMessageFormException, IOException{
		WampCallResultMessage result = new WampCallResultMessage();


		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("CallId is required and must be a string");
		result.callId = parser.getText();

		parser.nextToken();
		result.result = new JSONArguments(parser);

		return result;
	}

	public static WampEventMessage eventMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampEventMessage result = new WampEventMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("TopicUri is required and must be a string");
		result.topicId = parser.getText();

		parser.nextToken();
		result.event = new JSONArguments(parser);

		return result;
	}

	public static WampPublishMessage publishMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampPublishMessage result = new WampPublishMessage();			

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("TopicUri is required and must be a string");
		result.topicId  = parser.getText();

		parser.nextToken();
		result.event = parser.readValueAs(Object.class);

		//excludeme or exclude list
		if(parser.nextToken() == JsonToken.VALUE_TRUE)
			result.excludeMe = true;
		else if(parser.getCurrentToken() == JsonToken.START_ARRAY){
			result.exclude = new ArrayList<String>();
			while(parser.nextToken() != JsonToken.END_ARRAY){
				if(parser.getCurrentToken() != JsonToken.VALUE_STRING)
					throw new BadMessageFormException("Fourth element must be boolean or array of string");
				result.exclude.add(parser.getText());
			}

			//eligible list
			if(parser.nextToken() == JsonToken.START_ARRAY){
				result.eligible = new ArrayList<String>();
				while(parser.nextToken() != JsonToken.END_ARRAY){
					if(parser.getCurrentToken() != JsonToken.VALUE_STRING)
						throw new BadMessageFormException("Fifth element must be an array of string");
					result.eligible.add(parser.getText());
				}
			}
		}

		return result;
	}

	public static WampUnsubscribeMessage unsubscribeMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampUnsubscribeMessage result = new WampUnsubscribeMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("TopicUri is required and must be a string");
		result.topicId = parser.getText();

		return result;

	}

	public static WampSubscribeMessage subscribeMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampSubscribeMessage result = new WampSubscribeMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("TopicUri is required and must be a string");
		result.topicId = parser.getText();

		return result;
	}

	public static WampWelcomeMessage welcomeMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampWelcomeMessage result = new WampWelcomeMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("SessionId is required and must be a string");
		result.setSessionId(parser.getText());

		if(parser.nextToken() != JsonToken.VALUE_NUMBER_INT)
			throw new BadMessageFormException("ProtocolVersion is required and must be a int");
		result.setProtocolVersion(parser.getIntValue());

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("Implementation is required and must be a string");
		result.setImplementation(parser.getText());

		return result;

	}

	public static WampPrefixMessage prefixMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampPrefixMessage result = new WampPrefixMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("prefix is required and must be a string");
		result.setPrefix(parser.getText());

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new BadMessageFormException("URI is required and must be a int");
		result.setUri(parser.getText());

		return result;
	}
}
