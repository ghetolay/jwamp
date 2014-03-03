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
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.github.ghetolay.jwamp.utils.JsonBackedObject;
import com.github.ghetolay.jwamp.utils.JsonBackedObjectFactory;


/**
 * @author ghetolay
 *
 */
public abstract class WampMessageDecoder implements Decoder{
	
	private static final Logger log = LoggerFactory.getLogger(WampMessageDecoder.class);
	
	/**
	 * Singleton JsonFactory used by all instances of MessageEncoder.  If sub-classes wish to use a different JsonFactory, they can do so
	 * by overriding {@link WampMessageEncoder#getJsonFactory()}
	 */
	private static final JsonFactory jsonFactory = new MappingJsonFactory();
	
	private WampMessageDecoder(){
		log.debug("Message decoder created");
	}
	
	@Override
	public void init(EndpointConfig config) {
		log.debug("init");
	}

	protected JsonFactory getJsonFactory() {
		return jsonFactory;
	}
	
	@Override
	public void destroy() {
	}

	public static class Text extends WampMessageDecoder implements Decoder.Text<WampMessage>{
		@Override
		public boolean willDecode(String s) {
			return true;
		}
	
		@Override
		public WampMessage decode(String reader) throws DecodeException{
	
			try {
				JsonParser parser = getJsonFactory().createParser(reader);
				return decode(parser);
			} catch (IOException e) {
				throw new DecodeException(reader, "Unparsable message", e);
			}
			
		}
	}
	
	public static class TextStream extends WampMessageDecoder implements Decoder.TextStream<WampMessage>{
		
		@Override
		public WampMessage decode(Reader reader) throws DecodeException, IOException{

			JsonParser parser = getJsonFactory().createParser(reader);

			return WampMessageDecoder.decode(parser);

		}
	}
	
	// TODO: refactor the decoders into objects and use an EnumMap to retrieve the appropriate one
	protected static WampMessage decode(JsonParser parser) throws DecodeException, IOException{
		try{
			if(parser.nextToken() != JsonToken.START_ARRAY)
				throw new DecodeException(parser.getValueAsString(), "WampMessage must be a not null JSON array");
	
			if(parser.nextToken() != JsonToken.VALUE_NUMBER_INT)
				throw new DecodeException(parser.getValueAsString(), "The first array element must be a int");
	
			MessageType messageType;
			try{
				messageType = MessageType.forId(parser.getIntValue());
			} catch (IllegalArgumentException e){
				throw new DecodeException(parser.getValueAsString(), "Unknown message type : " + parser.getIntValue());
			}
			
			switch(messageType){
				case CALL :
					return callMsg(parser);
	
				case CALLERROR :
					return callErrorMsg(parser);
	
				case CALLRESULT :
					return callResultMsg(parser);
	
				case EVENT :
					return eventMsg(parser);
	
				case PUBLISH :
					return publishMsg(parser);
	
				case SUBSCRIBE :
					return subscribeMsg(parser);
	
				case UNSUBSCRIBE :	
					return unsubscribeMsg(parser);
	
				case WELCOME :
					return welcomeMsg(parser);
	
				case PREFIX :
					return prefixMsg(parser);
					
				default:
					throw new DecodeException(parser.getValueAsString(), "Unknown message type : " + parser.getIntValue());
			}
		} catch (JsonParseException e){
			throw new DecodeException(parser.getValueAsString(), "Unknown message type : " + parser.getIntValue());
		}
	}
	


	private static URI parseURI(JsonParser parser) throws JsonParseException, IOException, DecodeException{
		String uriText = parser.getText();
		
		URI uri;
		try {
			uri = new URI(uriText);
		} catch (URISyntaxException e) {
			throw new DecodeException(uriText, "Invalid URI");
		}

		return uri;
	}
	
	private static void assertLastArgument(JsonParser parser) throws DecodeException, JsonParseException, IOException{
		
		if (parser.nextToken() != JsonToken.END_ARRAY)
			throw new DecodeException(parser.getValueAsString(), "To many arguments");
		
	}
	
	public static WampCallErrorMessage callErrorMsg(JsonParser parser) throws JsonParseException, IOException, DecodeException{
		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "CallId is required and must be a string");
		String callId = parser.getText();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "ErrorUri is required and must be a string");
		URI errorURI = parseURI(parser);

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "ErrorDescription is required and must be a string");
		String errorDesc = parser.getText();

		if (parser.nextToken() == JsonToken.END_ARRAY)
			return WampCallErrorMessage.create(callId, errorURI, errorDesc);
		
		JsonBackedObject errorDetails = JsonBackedObjectFactory.readNextObject(parser);
		
		assertLastArgument(parser);
		
		return WampCallErrorMessage.create(callId, errorURI, errorDesc, errorDetails);
	}

	public static WampCallMessage callMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "CallId is required and must be a string");
		String callId = parser.getText();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "ProcUri is required and must be a string");
		URI procURI = parseURI(parser);

		List<JsonBackedObject> args = new ArrayList<JsonBackedObject>();
		while (parser.nextToken() != JsonToken.END_ARRAY){
			JsonBackedObject arg = JsonBackedObjectFactory.readNextObject(parser);
			args.add(arg);
		}
		
		// by definition, we are at the last argument now, so no need to call assertLastArgument(parser);
		
		return WampCallMessage.create(callId, procURI, args);
	}


	private static WampCallResultMessage callResultMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "CallId is required and must be a string");
		String callId = parser.getText();

		if (parser.nextToken() == JsonToken.END_ARRAY)
			throw new DecodeException(parser.getValueAsString(), "Result is required");
		
		JsonBackedObject result = JsonBackedObjectFactory.readNextObject(parser);

		assertLastArgument(parser);
		
		return WampCallResultMessage.create(callId, result);
		
	}

	public static WampEventMessage eventMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{
		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "TopicUri is required and must be a string");
		URI topicURI = parseURI(parser);

		if (parser.nextToken() == JsonToken.END_ARRAY)
			throw new DecodeException(parser.getValueAsString(), "Event is required");

		JsonBackedObject event = JsonBackedObjectFactory.readNextObject(parser);
		
		assertLastArgument(parser);
		
		return WampEventMessage.create(topicURI, event);
	}

	public static WampPublishMessage publishMsg(JsonParser parser) throws JsonParseException, IOException, DecodeException {

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "TopicUri is required and must be a string");
		URI topicURI = parseURI(parser);

		if (parser.nextToken() == JsonToken.END_ARRAY)
			throw new DecodeException(parser.getValueAsString(), "Event is required");
		
		JsonBackedObject event = JsonBackedObjectFactory.readNextObject(parser);

		//excludeme or exclude list
		if(parser.nextToken() == JsonToken.VALUE_TRUE)
			return WampPublishMessage.createExcludeMe(topicURI, event);
		else if(parser.getCurrentToken() == JsonToken.START_ARRAY){
			List<String> exclude = new ArrayList<String>();
			while(parser.nextToken() != JsonToken.END_ARRAY){
				if(parser.getCurrentToken() != JsonToken.VALUE_STRING)
					throw new DecodeException(parser.getValueAsString(), "Fourth element must be boolean or array of string");
				exclude.add(parser.getText());
			}

			//eligible list
			if(parser.nextToken() == JsonToken.START_ARRAY){
				List<String> eligible = new ArrayList<String>();
				while(parser.nextToken() != JsonToken.END_ARRAY){
					if(parser.getCurrentToken() != JsonToken.VALUE_STRING)
						throw new DecodeException(parser.getValueAsString(), "Fifth element must be an array of string");
					eligible.add(parser.getText());
				}
				return WampPublishMessage.createWithExcludeAndEligible(topicURI, event, exclude, eligible);
			} else {
				return WampPublishMessage.createWithExclude(topicURI, event, exclude);
			}
		} else {
			return WampPublishMessage.createSimple(topicURI, event);
		}

	}

	public static WampUnsubscribeMessage unsubscribeMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "TopicUri is required and must be a string");
		URI topicURI = parseURI(parser);

		assertLastArgument(parser);
		
		return WampUnsubscribeMessage.create(topicURI);

	}

	public static WampSubscribeMessage subscribeMsg(JsonParser parser) throws JsonParseException, IOException, DecodeException{

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "TopicUri is required and must be a string");
		URI topicURI = parseURI(parser);
		
		assertLastArgument(parser);

		return WampSubscribeMessage.create(topicURI);
	}

	public static WampWelcomeMessage welcomeMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{
		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(),"SessionId is required and must be a string");
		String sessionId = parser.getText();

		if(parser.nextToken() != JsonToken.VALUE_NUMBER_INT)
			throw new DecodeException(parser.getValueAsString(), "ProtocolVersion is required and must be a int");
		int protocolVersion = parser.getIntValue();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "Implementation is required and must be a string");
		String implementation = parser.getText();

		assertLastArgument(parser);

		return new WampWelcomeMessage(sessionId, protocolVersion, implementation);

	}

	public static WampPrefixMessage prefixMsg(JsonParser parser) throws JsonParseException, DecodeException, IOException{
		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "prefix is required and must be a string");
		String prefix = parser.getText();

		if(parser.nextToken() != JsonToken.VALUE_STRING)
			throw new DecodeException(parser.getValueAsString(), "URI is required and must be a string");
		URI uri = parseURI(parser);

		assertLastArgument(parser);

		return new WampPrefixMessage(prefix, uri);
	}

}
