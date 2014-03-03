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
import java.io.StringWriter;
import java.io.Writer;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.github.ghetolay.jwamp.utils.JsonBackedObject;

/**
 * @author ghetolay
 *
 */
public abstract class WampMessageEncoder implements Encoder{

	/**
	 * Singleton JsonFactory used by all instances of MessageEncoder.  If sub-classes wish to use a different JsonFactory, they can do so
	 * by overriding {@link WampMessageEncoder#getJsonFactory()}
	 */
	private static final JsonFactory jsonFactory = new MappingJsonFactory();

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {		
	}

	protected JsonFactory getJsonFactory() {
		return jsonFactory;
	}
	
	public static class Text extends WampMessageEncoder implements Encoder.Text<WampMessage>{
		@Override
		public String encode(WampMessage msg) throws EncodeException{
			StringWriter writer = new StringWriter();
			internalEncode(msg, writer);
			return writer.toString();
		}
	}
	
	public static class TextStream extends WampMessageEncoder implements Encoder.TextStream<WampMessage>{

		@Override
		public void encode(WampMessage msg, Writer writer) throws EncodeException, IOException {
			internalEncode(msg, writer);
		}
		
	}
	
	
	protected void internalEncode(WampMessage msg, Writer writer) throws EncodeException{

		try{
			JsonGenerator generator = jsonFactory.createGenerator(writer);
			
			generator.writeStartArray();
			generator.writeNumber(msg.getMessageType().id);

			try{
				switch(msg.getMessageType()){
				case CALL :
					callMsg((WampCallMessage) msg, generator);
					break;

				case CALLRESULT :
					callResultMsg((WampCallResultMessage) msg, generator);
					break;

				case PUBLISH :
					publishMsg((WampPublishMessage) msg, generator);
					break;

				case EVENT :
					eventMsg((WampEventMessage) msg, generator);
					break;

				case CALLERROR :
					callErrorMsg((WampCallErrorMessage) msg, generator);
					break;

				case SUBSCRIBE :
					subscribeMsg((WampSubscribeMessage) msg, generator);
					break;

				case UNSUBSCRIBE :	
					unsubscribeMsg((WampUnsubscribeMessage) msg, generator);
					break;

				case WELCOME :
					welcomeMsg((WampWelcomeMessage) msg, generator);
					break;

				case PREFIX :
					prefixMsg((WampPrefixMessage) msg, generator);
					break;

				default :
					throw new EncodeException(msg, "Unknown message type : " + msg.getMessageType());
				}

				generator.writeEndArray();
			} finally{
				generator.close();
			}
		}catch(IOException e){
			throw new RuntimeException("Cannot happen", e);
		}

	}

	public static void callErrorMsg(WampCallErrorMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{

		generator.writeString(msg.getCallId());
		generator.writeString(msg.getErrorUri().toString());
		generator.writeString(msg.getErrorDesc());

		if(msg.isErrorDetailsPresent())
			generator.writeObject(msg.getErrorDetails().getAs(Object.class));
	}

	public static  void callMsg(WampCallMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{ 

		generator.writeString(msg.getCallId());
		generator.writeString(msg.getProcURI().toString());
		for (JsonBackedObject arg : msg.getArgs()) {
			generator.writeObject(arg.getAs(Object.class));
		}
	}

	public static  void callResultMsg(WampCallResultMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{

		generator.writeString(msg.getCallId());
		generator.writeObject(msg.getResult().getAs(Object.class));
	}

	public static  void eventMsg(WampEventMessage msg, JsonGenerator generator) throws JsonProcessingException, IOException{

		generator.writeString(msg.getTopicURI().toString());
		generator.writeObject(msg.getEvent().getAs(Object.class));
	}

	public static  void publishMsg(WampPublishMessage msg, JsonGenerator generator) throws JsonProcessingException, IOException{

		generator.writeString(msg.getTopicURI().toString());
		generator.writeObject(msg.getEvent().getAs(Object.class));

		if(msg.isExcludeMe())
			generator.writeBoolean(true);
		else{
			if (!msg.isExcludeSpecified() && msg.isEligibleSpecified()){
				generator.writeStartArray();
				generator.writeEndArray();
			}
			if (msg.isExcludeSpecified()){
				generator.writeObject(msg.getExclude());
			}
			if (msg.isEligibleSpecified()){
				generator.writeObject(msg.getEligible());
			}
		}
	}

	public static  void subscribeMsg(WampSubscribeMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{
		generator.writeString(msg.getTopicURI().toString());

	}

	//same as subcribeMsg, what a waste...
	public static  void unsubscribeMsg(WampUnsubscribeMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{
		generator.writeString(msg.getTopicURI().toString());
	}

	public static  void welcomeMsg(WampWelcomeMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{

		generator.writeString(msg.getSessionId());
		generator.writeNumber(msg.getProtocolVersion());
		generator.writeString(msg.getImplementation());
	}

	public static  void prefixMsg(WampPrefixMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{

		generator.writeString(msg.getPrefix());
		generator.writeString(msg.getUri().toString());
	}

}
