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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.github.ghetolay.jwamp.WampFactory;


/**
 * @author ghetolay
 *
 */
public class WampMessageDecoder implements Decoder.Text<WampMessage>{

	private JsonFactory jsonFactory;

	@Override
	public void init(EndpointConfig config) {
		this.jsonFactory = new MappingJsonFactory();//(JsonFactory) config.getUserProperties().get("jwamp.jsonfactory");
	}

	@Override
	public void destroy() {		
	}

	@Override
	public WampMessage decode(String reader) throws DecodeException{


		try {
			//JsonParser parser = jsonFactory.createParser(reader);
			JsonParser parser = WampFactory.getInstance().getWampEncoders().getJsonFactory().createParser(reader);

			if(parser.nextToken() != JsonToken.START_ARRAY)
				throw new DecodeException(parser.getValueAsString(), "WampMessage must be a not null JSON array");

			if(parser.nextToken() != JsonToken.VALUE_NUMBER_INT)
				throw new DecodeException(parser.getValueAsString(), "The first array element must be a int");

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

			case WampMessage.PREFIX :
				return prefixMsg(parser);

			default :
				throw new DecodeException(parser.getValueAsString(), "Unknown message type : " + messageType);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static WampCallErrorMessage callErrorMsg(JsonParser parser) throws JsonParseException, IOException, DecodeException{
		WampCallErrorMessage result = new WampCallErrorMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "CallId is required and must be a string");
		result.callId = parser.getText();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "ErrorUri is required and must be a string");
		try {
			result.errorUri = new URI(parser.getText());
		} catch (URISyntaxException e) {
			throw new DecodeException(parser.getText(), "Invalid URI");
		}

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "ErrorDescription is required and must be a string");
		result.errorDesc = parser.getText();

		parser.nextToken();
		result.errorDetails = new JSONArguments(parser);

		return result;
	}

	public static WampCallMessage callMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{
		WampCallMessage result = new WampCallMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "CallId is required and must be a string");
		result.callId = parser.getText();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "ProcUri is required and must be a string");
		try {
			result.procURI = new URI(parser.getText());
		} catch (URISyntaxException e) {
			throw new DecodeException(parser.getText(), "Invalid URI");
		}

		parser.nextToken();
		result.args = new JSONArguments(parser);

		return result;
	}


	private static WampCallResultMessage callResultMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{
		WampCallResultMessage result = new WampCallResultMessage();


		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "CallId is required and must be a string");
		result.callId = parser.getText();

		parser.nextToken();
		result.result = new JSONArguments(parser);

		return result;
	}

	public static WampEventMessage eventMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{
		WampEventMessage result = new WampEventMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "TopicUri is required and must be a string");
		try{
			result.topicURI = new URI(parser.getText());
		} catch (URISyntaxException e) {
			throw new DecodeException(parser.getText(), "Invalid URI");
		}

		parser.nextToken();
		result.event = new JSONArguments(parser);

		return result;
	}

	public static WampPublishMessage publishMsg(JsonParser parser) throws JsonParseException, IOException, DecodeException {
		WampPublishMessage result = new WampPublishMessage();			

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "TopicUri is required and must be a string");
		try{
			result.topicURI = new URI(parser.getText());
		} catch (URISyntaxException e) {
			throw new DecodeException(parser.getText(), "Invalid URI");
		}

		parser.nextToken();
		result.event = parser.readValueAs(Object.class);

		//excludeme or exclude list
		if(parser.nextToken() == JsonToken.VALUE_TRUE)
			result.excludeMe = true;
		else if(parser.getCurrentToken() == JsonToken.START_ARRAY){
			result.exclude = new ArrayList<String>();
			while(parser.nextToken() != JsonToken.END_ARRAY){
				if(parser.getCurrentToken() != JsonToken.VALUE_STRING)
					throw new DecodeException(parser.getValueAsString(), "Fourth element must be boolean or array of string");
				result.exclude.add(parser.getText());
			}

			//eligible list
			if(parser.nextToken() == JsonToken.START_ARRAY){
				result.eligible = new ArrayList<String>();
				while(parser.nextToken() != JsonToken.END_ARRAY){
					if(parser.getCurrentToken() != JsonToken.VALUE_STRING)
						throw new DecodeException(parser.getValueAsString(), "Fifth element must be an array of string");
					result.eligible.add(parser.getText());
				}
			}
		}

		return result;
	}

	public static WampUnsubscribeMessage unsubscribeMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{
		WampUnsubscribeMessage result = new WampUnsubscribeMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "TopicUri is required and must be a string");
		try{
			result.topicURI = new URI(parser.getText());
		} catch (URISyntaxException e) {
			throw new DecodeException(parser.getText(), "Invalid URI");
		}

		return result;

	}

	public static WampSubscribeMessage subscribeMsg(JsonParser parser) throws JsonParseException, IOException, DecodeException{
		WampSubscribeMessage result = new WampSubscribeMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "TopicUri is required and must be a string");
		try{
			result.topicURI = new URI(parser.getText());
		} catch (URISyntaxException e) {
			throw new DecodeException(parser.getText(), "Invalid URI");
		}

		return result;
	}

	public static WampWelcomeMessage welcomeMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{
		WampWelcomeMessage result = new WampWelcomeMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(),"SessionId is required and must be a string");
		result.setSessionId(parser.getText());

		if(parser.nextToken() != JsonToken.VALUE_NUMBER_INT)
			throw new DecodeException(parser.getValueAsString(), "ProtocolVersion is required and must be a int");
		result.setProtocolVersion(parser.getIntValue());

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "Implementation is required and must be a string");
		result.setImplementation(parser.getText());

		return result;

	}

	public static WampPrefixMessage prefixMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{
		WampPrefixMessage result = new WampPrefixMessage();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "prefix is required and must be a string");
		result.setPrefix(parser.getText());

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "URI is required and must be a int");
		result.setUri(parser.getText());

		return result;
	}

	@Override
	public boolean willDecode(String s) {
		// TODO Auto-generated method stub
		return true;
	}
}
