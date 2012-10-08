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
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.unpacker.BufferUnpacker;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
			case WampMessage.CALLMORERESULT :
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

	public static WampMessage deserialize(byte[] data, int offset, int length, MessagePack msgPack) throws SerializationException{

		try{
			BufferUnpacker unpacker = msgPack.createBufferUnpacker(data, offset, length);
	
			int size;
			Integer msgType = -1;

			try{
				size = unpacker.readArrayBegin();
			}catch(IOException e){
				throw new BadMessageFormException("Message should be an array with a int as first element.", e);
			}
			
			msgType = getIntProperty(unpacker, "MessageType");

			switch(msgType){
			case WampMessage.CALL :
				return callMsg(unpacker, size);

			case WampMessage.CALLERROR :
				return  callErrorMsg(unpacker, size);

			case WampMessage.CALLRESULT :
			case WampMessage.CALLMORERESULT :
				return  callResultMsg(unpacker, size);

			case WampMessage.EVENT :
				return  eventMsg(unpacker, size);

			case WampMessage.PUBLISH :
				return  publishMsg(unpacker, size);

			case WampMessage.SUBSCRIBE :
				return  subscribeMsg(unpacker, size);

			case WampMessage.UNSUBSCRIBE :	
				return  unsubscribeMsg(unpacker, size);

			case WampMessage.WELCOME :
				return welcomeMsg(unpacker, size);

			default :
				throw new SerializationException("Unknown message type : " + msgType);
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

			if(parser.nextToken() != JsonToken.END_ARRAY){
				if(parser.nextToken() != JsonToken.VALUE_STRING)
					throw new BadMessageFormException("ErrorDetails must be a string");
				result.errorDetails = parser.getText();
			}

			return result;
	}

	public static WampCallErrorMessage callErrorMsg(Unpacker unpacker, int size) throws BadMessageFormException{
		
		if( size  < 4 )
			throw BadMessageFormException.notEnoughParameter("CALLERROR", size, 4);
		
		WampCallErrorMessage result = new WampCallErrorMessage();
		
		result.callId = getStringProperty(unpacker, "CallID");
		result.errorUri = getStringProperty(unpacker, "errorUri");
		result.errorDetails = getStringProperty(unpacker, "errorDesc");
		
		if(size > 4)
			result.errorDetails = getStringProperty(unpacker, "errorDetails", false);
		
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

			JsonToken token = parser.nextToken();
			if(token != null && token != JsonToken.END_ARRAY)
				result.args = new JSONWampArrayObject(parser,true);

			return result;
	
	}

	public static WampCallMessage callMsg(Unpacker unpacker, int size) throws BadMessageFormException{
		if( size  < 3 )
			throw BadMessageFormException.notEnoughParameter("CALL", size, 3);
		
		WampCallMessage result = new WampCallMessage();
		
		result.callId = getStringProperty(unpacker, "CallID");
		result.procId = getStringProperty(unpacker, "procId");
		if( size > 3)
			result.args = new MsgPackWampArrayObject(unpacker, size - 3);
		
		return result;
	}
	
	public static WampCallResultMessage callResultMsg(JsonParser parser) throws BadMessageFormException, IOException{
		return callResultMsg(parser, true);
	}

	public static WampCallResultMessage callMoreResultMsg(JsonParser parser) throws BadMessageFormException, IOException{
		return callResultMsg(parser,false);
	}


	private static WampCallResultMessage callResultMsg(JsonParser parser, boolean last) throws BadMessageFormException, IOException{
		WampCallResultMessage result = new WampCallResultMessage(true);


			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("CallId is required and must be a string");
			result.callId = parser.getText();

			if(parser.nextToken() == JsonToken.END_ARRAY)
				throw new BadMessageFormException("Missing event element");

			result.result = new JSONWampArrayObject(parser,false);

			return result;
	}

	public static WampCallResultMessage callResultMsg(Unpacker unpacker, int size) throws BadMessageFormException{
		if( size  < 3 )
			throw BadMessageFormException.notEnoughParameter("CALLRESULT", size, 3);
		
		WampCallResultMessage result = new WampCallResultMessage();
		
		result.callId = getStringProperty(unpacker, "CallID");
		result.result = new MsgPackWampArrayObject(unpacker);
		
		return result;
	}
	
	public static WampEventMessage eventMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampEventMessage result = new WampEventMessage();
		
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("TopicUri is required and must be a string");
			result.topicId = parser.getText();

			if(parser.nextToken() == JsonToken.END_ARRAY)
				throw new BadMessageFormException("Missing event element");

			result.event = new JSONWampArrayObject(parser, false);

			return result;
	

	}

	public static WampEventMessage eventMsg(Unpacker unpacker, int size) throws BadMessageFormException{
		if( size  < 3 )
			throw BadMessageFormException.notEnoughParameter("EVENT", size, 3);
		
		WampEventMessage result = new WampEventMessage();
		
		result.topicId = getStringProperty(unpacker, "topicURI");
		result.event = new MsgPackWampArrayObject(unpacker);
		
		return result;
	}
	
	public static WampPublishMessage publishMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampPublishMessage result = new WampPublishMessage();			
		
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("TopicUri is required and must be a string");
			result.topicId  = parser.getText();

			result.event = parser.readValueAs(Object.class);

			//excludeme or exclude list
			if(parser.nextToken() != JsonToken.END_ARRAY){
				if(parser.getCurrentToken() == JsonToken.VALUE_TRUE)
					result.excludeMe = true;
				else if(parser.getCurrentToken() == JsonToken.START_ARRAY){
					result.exclude = new ArrayList<String>();
					while(parser.nextToken() != JsonToken.END_ARRAY){
						if(parser.getCurrentToken() != JsonToken.VALUE_STRING)
							throw new BadMessageFormException("Fourth element must be boolean or array of string");
						result.exclude.add(parser.getText());
					}
				}else
					throw new BadMessageFormException("Fourth element must be boolean or array of string");

				//eligible list
				if(parser.nextToken() != JsonToken.END_ARRAY){
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

	public static WampPublishMessage publishMsg(Unpacker unpacker, int size) throws BadMessageFormException{
		if( size  < 3 )
			throw BadMessageFormException.notEnoughParameter("PUBLISH", size, 3);
		
		WampPublishMessage result = new WampPublishMessage();
		
		result.topicId = getStringProperty(unpacker, "topicURI");
		result.event = getValueProperty(unpacker, "event");
		
		if( size > 3 && size < 5)
			result.excludeMe = getBooleanProperty(unpacker, "excludeMe", false);
		else{
			result.exclude = new ArrayList<String>();
			result.eligible = new ArrayList<String>();
			result.exclude = setProperty(unpacker, result.exclude, "exclude");
			result.eligible = setProperty(unpacker, result.eligible, "eligible");
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

	public static WampUnsubscribeMessage unsubscribeMsg(Unpacker unpacker, int size) throws BadMessageFormException{
		if( size  < 2 )
			throw BadMessageFormException.notEnoughParameter("UNSUBSCRIBE", size, 2);
		
		WampUnsubscribeMessage result = new WampUnsubscribeMessage();
		
		result.topicId = getStringProperty(unpacker, "topicURI");
		
		return result;
	}
	
	public static WampSubscribeMessage subscribeMsg(JsonParser parser) throws BadMessageFormException, IOException{
		WampSubscribeMessage result = new WampSubscribeMessage();

			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("TopicUri is required and must be a string");
			result.topicId = parser.getText();

			if(parser.nextToken() != JsonToken.END_ARRAY)
				result.args = new JSONWampArrayObject(parser, true);

			return result;
		
	}

	public static WampSubscribeMessage subscribeMsg(Unpacker unpacker, int size) throws BadMessageFormException{
		if( size  < 2 )
			throw BadMessageFormException.notEnoughParameter("SUBSCRIBE", size, 2);
		
		WampSubscribeMessage result = new WampSubscribeMessage();
		
		result.topicId = getStringProperty(unpacker, "topicURI");
		if(size > 2)
			result.args = new MsgPackWampArrayObject(unpacker);
		
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
	
	public static WampWelcomeMessage welcomeMsg(Unpacker unpacker, int size) throws BadMessageFormException{
		if( size  < 4 )
			throw BadMessageFormException.notEnoughParameter("WELCOME", size, 4);
		
		WampWelcomeMessage result = new WampWelcomeMessage();
		
		result.sessionId = getStringProperty(unpacker, "sessionId");
		result.protocolVersion = getIntProperty(unpacker, "protocolVersion", false);
		result.implementation = getStringProperty(unpacker, "serverIdentification", false);

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

	public static WampPrefixMessage prefixMsg(Unpacker unpacker, int size) throws BadMessageFormException{
		if( size  < 3 )
			throw BadMessageFormException.notEnoughParameter("PREFIX", size, 3);
		
		WampPrefixMessage result = new WampPrefixMessage();
		
		result.prefix = getStringProperty(unpacker, "prefix");
		result.uri = getStringProperty(unpacker, "URI");
		
		return result;
	}
	
	//TODO create equivalent setProperty for JsonParser
	//TODO add messagetype to the exception message
	
	
	private static int getIntProperty(Unpacker unpacker, String propertyName) throws BadMessageFormException{
		return getIntProperty(unpacker, propertyName, true);
	}
	
	private static int getIntProperty(Unpacker unpacker, String propertyName, boolean fatal) throws BadMessageFormException{
		try{
			return unpacker.readInt();
		}catch(Exception e){
			if(fatal)
				throw new BadMessageFormException("Error reading " + propertyName,e);
			else{
				Logger log = LoggerFactory.getLogger(WampMessageDeserializer.class);
				if(log.isWarnEnabled())
					log.warn("Error reading " + propertyName +". Set to 0",e);
				return 0;
			}
		}
	}
	
	private static boolean getBooleanProperty(Unpacker unpacker, String propertyName, boolean fatal) throws BadMessageFormException{
		try{
			return unpacker.readBoolean();
		}catch(Exception e){
			if(fatal)
				throw new BadMessageFormException("Error reading " + propertyName,e);
			else{
				Logger log = LoggerFactory.getLogger(WampMessageDeserializer.class);
				if(log.isWarnEnabled())
					log.warn("Error reading " + propertyName +". Set to false",e);
				return false;
			}
		}
		
	}
	
	private static String getStringProperty(Unpacker unpacker, String propertyName) throws BadMessageFormException{
		return getStringProperty(unpacker, propertyName, true);
	}
	
	private static String getStringProperty(Unpacker unpacker, String propertyName, boolean fatal) throws BadMessageFormException{
		try{
			return unpacker.readString();
		}catch(Exception e){
			if(fatal)
				throw new BadMessageFormException("Error reading " + propertyName,e);
			else{
				Logger log = LoggerFactory.getLogger(WampMessageDeserializer.class);
				if(log.isWarnEnabled())
					log.warn("Error reading " + propertyName +". Set as an empty string",e);
				return "";
			}
		}
		
	}
	
	private static <T> T setProperty(Unpacker unpacker, T property, String propertyName) throws BadMessageFormException{
		try{
			return unpacker.read(property);
		}catch(Exception e){
			throw new BadMessageFormException("Error reading " + propertyName,e);
		}
	}

	//should disappear, exist because of publish messages
	private static Value getValueProperty(Unpacker unpacker, String propertyName) throws BadMessageFormException{
		try{
			return unpacker.readValue();
		}catch(Exception e){
			throw new BadMessageFormException("Error reading " + propertyName,e);
		}
	}
}
