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
package com.github.ghetolay.jwamp.message.output;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.Packer;

import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampPrefixMessage;
import com.github.ghetolay.jwamp.message.WampWelcomeMessage;

/**
 * @author ghetolay
 *
 */
public class WampMessageSerializer {

	protected static StringBuffer startMsg(int messageType){
		StringBuffer result = new StringBuffer("[");
		result.append(messageType);
		result.append(',');
		return result;
	}

	protected static void appendString(StringBuffer sb, String s){
		sb.append('\"');
		sb.append(s);
		sb.append('\"');
	}

	protected static String endMsg(StringBuffer sb){
		sb.append(']');
		return sb.toString();
	}

	public static String serialize(WampMessage msg, ObjectMapper objectMapper) throws SerializationException{
		try{
			switch(msg.getMessageType()){
			case WampMessage.CALL :
				return callMsg((OutputWampCallMessage) msg, objectMapper);

			case WampMessage.CALLERROR :
				return callErrorMsg((OutputWampCallErrorMessage) msg);

			case WampMessage.CALLRESULT :
			case WampMessage.CALLMORERESULT :
				return callResultMsg((OutputWampCallResultMessage) msg, objectMapper);

			case WampMessage.EVENT :
				return eventMsg((OutputWampEventMessage) msg, objectMapper);

			case WampMessage.PUBLISH :
				return publishMsg((OutputWampPublishMessage) msg, objectMapper);

			case WampMessage.SUBSCRIBE :
				return subscribeMsg((OutputWampSubscribeMessage) msg, objectMapper);

			case WampMessage.UNSUBSCRIBE :	
				return unsubscribeMsg((OutputWampUnsubscribeMessage) msg);

			case WampMessage.WELCOME :
				return welcomeMsg((WampWelcomeMessage) msg);

			default :
				throw new SerializationException("Unknown message type : " + msg.getMessageType());
			}
		} catch(SerializationException e){
			throw e;
		} catch(Exception e){
			throw new SerializationException(e);
		}
	}

	public static byte[] serialize(WampMessage msg, MessagePack msgPack) throws SerializationException{

		try{
			BufferPacker packer = msgPack.createBufferPacker();

			switch(msg.getMessageType()){
			case WampMessage.CALL :
				callMsg((OutputWampCallMessage) msg, packer);
				break;

			case WampMessage.CALLERROR :
				callErrorMsg((OutputWampCallErrorMessage) msg, packer);
				break;

			case WampMessage.CALLRESULT :
			case WampMessage.CALLMORERESULT :
				callResultMsg((OutputWampCallResultMessage) msg, packer);
				break;

			case WampMessage.EVENT :
				eventMsg((OutputWampEventMessage) msg, packer);
				break;

			case WampMessage.PUBLISH :
				publishMsg((OutputWampPublishMessage) msg, packer);
				break;

			case WampMessage.SUBSCRIBE :
				subscribeMsg((OutputWampSubscribeMessage) msg, packer);
				break;

			case WampMessage.UNSUBSCRIBE :	
				unsubscribeMsg((OutputWampUnsubscribeMessage) msg, packer);
				break;

			case WampMessage.WELCOME :
				welcomeMsg((WampWelcomeMessage) msg, packer);
				break;

			default :
				throw new SerializationException("Unknown message type : " + msg.getMessageType());
			}

			return packer.toByteArray();
		} catch(SerializationException e){
			throw e;
		} catch(Exception e){
			throw new SerializationException(e);
		}
	}

	public static String callErrorMsg(OutputWampCallErrorMessage msg){

		StringBuffer result = startMsg(msg.getMessageType());

		result.append(',');
		appendString(result, msg.getCallId());
		result.append(',');
		appendString(result, msg.getErrorUri());
		result.append(',');
		appendString(result, msg.getErrorDesc());

		if(msg.getErrorDetails() != null && !msg.getErrorDetails().isEmpty()){
			result.append(',');
			appendString(result, msg.getErrorUri());
		}

		return endMsg(result);
	}

	public static void callErrorMsg(OutputWampCallErrorMessage msg, Packer pk) throws IOException {

		int size;
		if(msg.getErrorDetails() != null && !msg.getErrorDetails().isEmpty())
			size = 5;
		else
			size = 4;

		pk.writeArrayBegin(size);

		pk.write(msg.getMessageType());
		pk.write(msg.getCallId());
		pk.write(msg.getErrorUri());
		pk.write(msg.getErrorDesc());

		if(size == 5)
			pk.write(msg.getErrorDetails());

		pk.writeArrayEnd(true);
	}

	public static String callMsg(OutputWampCallMessage msg, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{

		StringBuffer result = startMsg(msg.getMessageType()); 

		appendString(result, msg.getCallId());
		result.append(',');
		appendString(result, msg.getProcId());
		serializeArgs(result, msg.getArguments(), objectMapper, true);

		return endMsg(result);
	}

	public static void callMsg(OutputWampCallMessage msg, Packer pk) throws IOException {

		pk.writeArrayBegin(3 + (msg.getArguments() == null ? 0 : msg.getArguments().size()) );

		pk.write(msg.getMessageType());
		pk.write(msg.getCallId());
		pk.write(msg.getProcId());
		serializeArgs(msg.getArguments(), pk, true);

		pk.writeArrayEnd(true);
	}

	public static String callResultMsg(OutputWampCallResultMessage msg, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		StringBuffer result = startMsg(msg.getMessageType());
		appendString(result, msg.getCallId());
		serializeArgs(result, msg.getResult(), objectMapper, false);

		return endMsg(result);
	}

	public static void callResultMsg(OutputWampCallResultMessage msg, Packer pk) throws IOException {

		pk.writeArrayBegin(3);

		pk.write(msg.getMessageType());
		pk.write(msg.getCallId());
		serializeArgs(msg.getResult(), pk, false);

		pk.writeArrayEnd(true);
	}

	public static String eventMsg(OutputWampEventMessage msg, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		StringBuffer result = startMsg(msg.getMessageType());
		appendString(result, msg.getTopicId());
		serializeArgs(result, msg.getEvent(), objectMapper, false);

		return endMsg(result);
	}

	public static void eventMsg(OutputWampEventMessage msg, Packer pk) throws IOException {

		pk.writeArrayBegin(3);

		pk.write(msg.getMessageType());
		pk.write(msg.getTopicId());
		serializeArgs(msg.getEvent(), pk, false);

		pk.writeArrayEnd(true);
	}

	public static String publishMsg(OutputWampPublishMessage msg,ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		StringBuffer result = startMsg(msg.getMessageType());

		appendString(result, msg.getTopicId());

		if(msg.getEvent() != null)
			result.append(objectMapper.writeValueAsString(msg.getEvent()));
		else
			result.append(",null");

		if(msg.isExcludeMe())
			result.append(",true");
		else
			if( msg.getEligible() != null || msg.getExclude() != null ){
				result.append(',');
				result.append(msg.getExclude()==null?"[]":objectMapper.writeValueAsString(msg.getExclude()));

				result.append(',');
				result.append(msg.getEligible()==null?"[]":objectMapper.writeValueAsString(msg.getEligible()));
			}

		return endMsg(result);
	}

	public static void publishMsg(OutputWampPublishMessage msg, Packer pk) throws IOException {

		int size;

		if(msg.isExcludeMe())
			size = 4;
		else 
			if( msg.getEligible() != null || msg.getExclude() != null )
				size = 5;
			else
				size = 3;

		pk.writeArrayBegin(size);
		pk.write(msg.getMessageType());
		serializeArgs(msg.getEvent(), pk, false);

		if( size == 4)
			pk.write(true);
		else
			if( size == 5 ){
				pk.write(msg.getExclude());
				pk.write(msg.getEligible());
			}

		pk.writeArrayEnd(true);
	}

	public static String subscribeMsg(OutputWampSubscribeMessage msg, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		StringBuffer result = startMsg(msg.getMessageType());

		appendString(result, msg.getTopicId());

		if(msg.getArguments() != null)
			serializeArgs(result, msg.getArguments(), objectMapper, true);

		return endMsg(result);
	}

	public static void subscribeMsg(OutputWampSubscribeMessage msg, Packer pk) throws IOException {

		pk.writeArrayBegin(2 + (msg.getArguments() == null ? 0 : msg.getArguments().size()) );

		pk.write(msg.getMessageType());
		pk.write(msg.getTopicId());
		serializeArgs(msg.getArguments(), pk, true);

		pk.writeArrayEnd(true);
	}

	public static String unsubscribeMsg(OutputWampUnsubscribeMessage msg){

		StringBuffer result = startMsg(msg.getMessageType());

		appendString(result, msg.getTopicId());

		return endMsg(result);
	}

	public static void unsubscribeMsg(OutputWampUnsubscribeMessage msg, Packer pk) throws IOException {

		pk.writeArrayBegin(2);

		pk.write(msg.getMessageType());
		pk.write(msg.getTopicId());

		pk.writeArrayEnd(true);
	}

	public static String welcomeMsg(WampWelcomeMessage msg){

		StringBuffer result = startMsg(msg.getMessageType());

		appendString(result, msg.getSessionId());
		result.append(',');
		result.append(msg.getProtocolVersion());
		result.append(',');
		appendString(result, msg.getImplementation());

		return endMsg(result);
	}

	public static void welcomeMsg(WampWelcomeMessage msg, Packer pk) throws IOException {

		pk.writeArrayBegin(4);

		pk.write(msg.getMessageType());
		pk.write(msg.getSessionId());
		pk.write(msg.getProtocolVersion());
		pk.write(msg.getImplementation());

		pk.writeArrayEnd(true);
	}
	
	public static String prefixMsg(WampPrefixMessage msg){

		StringBuffer result = startMsg(msg.getMessageType());
		
		result.append(',');
		appendString(result, msg.getPrefix());
		result.append(',');
		appendString(result, msg.getUri());
		
		return endMsg(result);
	}
	
	public static void prefixMsg(WampPrefixMessage msg, Packer pk) throws IOException {

		pk.writeArrayBegin(3);

		pk.write(msg.getMessageType());
		pk.write(msg.getPrefix());
		pk.write(msg.getUri());

		pk.writeArrayEnd(true);
	}
	
	private static void serializeArgs(WritableWampArrayObject v, Packer pk, boolean allowMultipleArguments)
			throws IOException {

		if(allowMultipleArguments){
			if(v != null && v.args != null)
				for(Object obj : v.args)
					pk.write(obj);
		}else{
			int size;
			if(v != null && (size = v.size()) > 0){
				if(size == 1)
					pk.write(v.args.get(0));
				else{
					pk.writeArrayBegin(size);
					for(Object obj : v.args)
						pk.write(obj);
					pk.writeArrayEnd();
				}
			}else
				pk.writeNil();
		}
	}

	private static void serializeArgs(StringBuffer result, WritableWampArrayObject argument, ObjectMapper objectMapper, boolean allowMultipleArguments) throws JsonGenerationException, JsonMappingException, IOException {

		if(allowMultipleArguments){
			if(argument != null && argument.args != null)
				for(Object obj : argument.args){
					result.append(',');
					result.append(objectMapper.writeValueAsString(obj));
				}
		}else{
			int size;
			result.append(',');
			if(argument != null && (size = argument.size()) > 0){
				if(size == 1)
					result.append(objectMapper.writeValueAsString(argument.args.get(0)));
				else
					result.append(objectMapper.writeValueAsString(argument.args));
			}else
				result.append("null");
		}
	}
}
