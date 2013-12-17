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

	public static String callErrorMsg(OutputWampCallErrorMessage msg){

		StringBuffer result = startMsg(msg.getMessageType());

		appendString(result, msg.getCallId());
		result.append(',');
		appendString(result, msg.getErrorUri());
		result.append(',');
		appendString(result, msg.getErrorDesc());

		if(msg.getErrorDetails() != null && !msg.getErrorDetails().isEmpty()){
			result.append(',');
			appendString(result, msg.getErrorDetails());
		}

		return endMsg(result);
	}

	public static String callMsg(OutputWampCallMessage msg, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{

		StringBuffer result = startMsg(msg.getMessageType()); 
		ArgumentSerializer arg = new ArgumentSerializer(msg.getArgument());
		
		appendString(result, msg.getCallId());
		result.append(',');
		appendString(result, msg.getProcId());
		arg.serialize(result,  objectMapper);

		return endMsg(result);
	}

	public static String callResultMsg(OutputWampCallResultMessage msg, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		StringBuffer result = startMsg(msg.getMessageType());
		ArgumentSerializer arg = new ArgumentSerializer(msg.getResult());
		
		appendString(result, msg.getCallId());
		arg.serialize(result,  objectMapper);

		return endMsg(result);
	}

	public static String eventMsg(OutputWampEventMessage msg, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		StringBuffer result = startMsg(msg.getMessageType());
		ArgumentSerializer arg = new ArgumentSerializer(msg.getEvent());
		
		appendString(result, msg.getTopicId());
		arg.serialize(result,  objectMapper);

		return endMsg(result);
	}

	//TODO new publish args at the end
	public static String publishMsg(OutputWampPublishMessage msg,ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		StringBuffer result = startMsg(msg.getMessageType());

		appendString(result, msg.getTopicId());

		if(msg.getEvent() != null)
			result.append(','+objectMapper.writeValueAsString(msg.getEvent()));
		
		if(msg.isExcludeMe())
			result.append(",true");
		else{
			boolean hasEligible = msg.getEligible() != null && !msg.getEligible().isEmpty();
			boolean hasExclude = msg.getExclude()  != null && !msg.getExclude().isEmpty();
			
			if( hasEligible || hasExclude ){
				result.append(',');
				result.append( hasExclude?objectMapper.writeValueAsString(msg.getExclude()):"[]" );

				if( hasEligible ){	
					result.append(',');
					result.append(objectMapper.writeValueAsString(msg.getEligible()));
				}
			}
		}
		
		return endMsg(result);
	}

	public static String subscribeMsg(OutputWampSubscribeMessage msg, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		StringBuffer result = startMsg(msg.getMessageType());
		
		appendString(result, msg.getTopicId());

		return endMsg(result);
	}

	public static String unsubscribeMsg(OutputWampUnsubscribeMessage msg){

		StringBuffer result = startMsg(msg.getMessageType());

		appendString(result, msg.getTopicId());

		return endMsg(result);
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
	
	public static String prefixMsg(WampPrefixMessage msg){

		StringBuffer result = startMsg(msg.getMessageType());
		
		result.append(',');
		appendString(result, msg.getPrefix());
		result.append(',');
		appendString(result, msg.getUri());
		
		return endMsg(result);
	}
	
}
