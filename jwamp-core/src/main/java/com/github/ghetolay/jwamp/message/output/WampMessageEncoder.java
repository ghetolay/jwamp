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
import java.io.StringWriter;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.github.ghetolay.jwamp.WampFactory;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampPrefixMessage;
import com.github.ghetolay.jwamp.message.WampWelcomeMessage;

/**
 * @author ghetolay
 *
 */
public class WampMessageEncoder implements Encoder.Text<WampMessage>{

	private JsonFactory jsonFactory;

	@Override
	public void init(EndpointConfig config) {
		this.jsonFactory = new MappingJsonFactory();//(JsonFactory) config.getUserProperties().get("jwamp.jsonfactory");
	}

	@Override
	public void destroy() {		
	}

	@Override
	public String encode(WampMessage msg)
			throws EncodeException{

		StringWriter writer = new StringWriter();

		try{
			//JsonGenerator generator = jsonFactory.createGenerator(writer);
			JsonGenerator generator = WampFactory.getInstance().getWampEncoders().getJsonFactory().createGenerator(writer);
			
			generator.writeStartArray();
			generator.writeNumber(msg.getMessageType());

			try{
				switch(msg.getMessageType()){
				case WampMessage.CALL :
					callMsg((OutputWampCallMessage) msg, generator);
					break;

				case WampMessage.CALLRESULT :
					callResultMsg((OutputWampCallResultMessage) msg, generator);
					break;

				case WampMessage.PUBLISH :
					publishMsg((OutputWampPublishMessage) msg, generator);
					break;

				case WampMessage.EVENT :
					eventMsg((OutputWampEventMessage) msg, generator);
					break;

				case WampMessage.CALLERROR :
					callErrorMsg((OutputWampCallErrorMessage) msg, generator);
					break;

				case WampMessage.SUBSCRIBE :
					subscribeMsg((OutputWampSubscribeMessage) msg, generator);
					break;

				case WampMessage.UNSUBSCRIBE :	
					unsubscribeMsg((OutputWampUnsubscribeMessage) msg, generator);
					break;

				case WampMessage.WELCOME :
					welcomeMsg((WampWelcomeMessage) msg, generator);
					break;

				case WampMessage.PREFIX :
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
			e.printStackTrace();
		}

		return writer.toString();
	}

	public void callErrorMsg(OutputWampCallErrorMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{

		generator.writeString(msg.getCallId());
		generator.writeString(msg.getErrorUri().toString());
		generator.writeString(msg.getErrorDesc());

		if(msg.getOutputErrorDetails() != null)
			generator.writeObject(msg.getOutputErrorDetails());
	}

	public void callMsg(OutputWampCallMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{ 

		generator.writeString(msg.getCallId());
		generator.writeString(msg.getProcURI().toString());
		new ArgumentEncoder(msg.getArgument()).encode(generator, false);
	}

	public void callResultMsg(OutputWampCallResultMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{

		generator.writeString(msg.getCallId());
		new ArgumentEncoder(msg.getResult()).encode(generator, true);
	}

	public void eventMsg(OutputWampEventMessage msg, JsonGenerator generator) throws JsonProcessingException, IOException{

		generator.writeString(msg.getTopicURI().toString());
		new ArgumentEncoder(msg.getEvent()).encode(generator, true);
	}

	public void publishMsg(OutputWampPublishMessage msg, JsonGenerator generator) throws JsonProcessingException, IOException{

		generator.writeString(msg.getTopicURI().toString());
		generator.writeObject(msg.getEvent());

		if(msg.isExcludeMe())
			generator.writeBoolean(true);
		else{
			boolean hasEligible = msg.getEligible() != null && !msg.getEligible().isEmpty();
			boolean hasExclude = msg.getExclude()  != null && !msg.getExclude().isEmpty();

			if( hasEligible || hasExclude ){

				// using: hasExclude?generator.writeObject(msg.getExclude()):generator.writeRaw("[]");
				// ends in : java.lang.Error: Unresolved compilation problems: 
				//   Syntax error on token "{", throw expected after this token
				//   No exception of type void can be thrown; an exception type must be a subclass of Throwable
				if(hasExclude)
					generator.writeObject(msg.getExclude());
				else
					//better than generator.startArray();generator.endArray();
					generator.writeRaw(",[]");

				if( hasEligible )
					generator.writeObject(msg.getEligible());
			}
		}
	}

	public void subscribeMsg(OutputWampSubscribeMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{
		generator.writeString(msg.getTopicURI().toString());

	}

	//same as subcribeMsg, what a waste...
	public void unsubscribeMsg(OutputWampUnsubscribeMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{
		generator.writeString(msg.getTopicURI().toString());
	}

	public void welcomeMsg(WampWelcomeMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{

		generator.writeString(msg.getSessionId());
		generator.writeNumber(msg.getProtocolVersion());
		generator.writeString(msg.getImplementation());
	}

	public void prefixMsg(WampPrefixMessage msg, JsonGenerator generator) throws JsonGenerationException, IOException{

		generator.writeString(msg.getPrefix());
		generator.writeString(msg.getUri());
	}

}
