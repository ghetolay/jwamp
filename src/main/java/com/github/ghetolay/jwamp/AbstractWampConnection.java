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

package com.github.ghetolay.jwamp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.message.BadMessageFormException;
import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.message.WampUnSubscribeMessage;
import com.github.ghetolay.jwamp.message.WampWelcomeMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;

public abstract class AbstractWampConnection implements WampConnection{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private String sessionId;
	protected ReconnectPolicy autoReconnect = ReconnectPolicy.YES;
	private ObjectMapper mapper;
	
	private boolean connected = false;
	private ResultListener<WampConnection> welcomeListener;
		
	private Collection<WampMessageHandler> messageHandlers;
	private boolean exclusiveHandler = true;
	
	public AbstractWampConnection(ObjectMapper mapper, Collection<WampMessageHandler> messageHandlers,  ResultListener<WampConnection> wl){
		if(mapper != null)
			this.mapper = mapper;
		else
			this.mapper = new ObjectMapper();
		
		this.welcomeListener = wl;
		
		if(messageHandlers!=null)
			this.messageHandlers = messageHandlers;
		else
			messageHandlers = new HashSet<WampMessageHandler>();
	}
	
	public abstract void sendMessage(String data) throws IOException;
	
	public void onClose(int closeCode, String message){
		if(log.isDebugEnabled())
			log.debug("Close connection " + sessionId + " reason : " + message);
			
		for(WampMessageHandler h : messageHandlers)
			h.onClose(sessionId, closeCode);
		
		connected = false;
	}
	
	protected void reset(){
		connected = false;
	}
	
	public void newClientConnection(){
		newClientConnection(null);
	}
	
	public void newClientConnection(String sessionId){
		try {
			sendWelcome(sessionId);
			
			initHandlers();
			
			connected = true;
		} catch (IOException e) {
			// TODO log
			if(log.isErrorEnabled())
				log.error("Unable to send Welcome Message");
		}
	}
	
	private void initHandlers(){
		if(messageHandlers != null)
			for(WampMessageHandler h: messageHandlers)
				h.onConnected(this);
	}
	
	private void sendWelcome(String sessionId) throws IOException{
		if(sessionId == null || sessionId.isEmpty())
			this.sessionId = UUID.randomUUID().toString();
		else
			this.sessionId = sessionId;
		
		if(log.isTraceEnabled())
			log.trace("Send welcome with sessionId : " + this.sessionId);
		
		WampWelcomeMessage msg = new WampWelcomeMessage();
		msg.setImplementation(WampFactory.getImplementation());
		msg.setProtocolVersion(WampFactory.getProtocolVersion());
		msg.setSessionId(this.sessionId);
		
		sendMessage(msg);
	}
	
	//Need optimization
	public void sendMessage(WampMessage msg) throws IOException{
		
		Object[] array = msg.toJSONArray();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		
		try {
			getObjectMapper().writeValue(baos,array);
			String jsonMsg = baos.toString("UTF-8");
				
			if(log.isDebugEnabled())
				log.debug("Sending Message " + jsonMsg);
			
			sendMessage(jsonMsg);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onMessage(String data){
		
		if(log.isDebugEnabled())
			log.debug("Receive Wamp Message " + data);
		
		try {
			JsonParser parser = getObjectMapper().getJsonFactory().createJsonParser(data);
			
			if(parser.nextToken() != JsonToken.START_ARRAY)
				throw new BadMessageFormException("WampMessage must be a not null JSON array");
			
			if(parser.nextToken() != JsonToken.VALUE_NUMBER_INT)
				throw new BadMessageFormException("The first array element must be a int");
			
			int messageType = parser.getIntValue();
	
			WampMessage msg;
			
			switch(messageType){
			/*
				case WampMessage.PREFIX :
					handler.onPrefix(new WampPrefixMessage(array));
					return;
			*/
				case WampMessage.CALL :
					msg = new WampCallMessage(parser);
					break;
			
				case WampMessage.CALLERROR :
					msg = new WampCallErrorMessage(parser);
					break;
					
				case WampMessage.CALLRESULT :
					msg = new WampCallResultMessage(parser);
					break;
				
				case WampMessage.CALLMORERESULT :
					msg = new WampCallResultMessage(WampMessage.CALLMORERESULT, parser);
					break;
					
				case WampMessage.EVENT :
					msg = new WampEventMessage(parser);
					break;
				
				case WampMessage.PUBLISH :
					msg = new WampPublishMessage(parser);
					break;
				
				case WampMessage.SUBSCRIBE :
					msg = new WampSubscribeMessage(parser);
					break;
					
				case WampMessage.UNSUBSCRIBE :	
					msg = new WampUnSubscribeMessage(parser);
					break;
					
				case WampMessage.WELCOME :
					onWelcome(new WampWelcomeMessage(parser));
					return;
					
				default :
					//TODO log
					log.debug("Unknown messagetype " + messageType);
					return;
			}
			
			for(WampMessageHandler h : messageHandlers){
				boolean result = h.onMessage(sessionId, msg);
				if(exclusiveHandler && result)
					return;
			}
			
			if(log.isWarnEnabled())
				log.warn("Message not handled : " + data);
			
		} catch (Exception e){
			if(log.isWarnEnabled()){
				log.warn("Message not handled because of internal error.\nmessage : " + data + "\nException : " + e.getLocalizedMessage());
				if(log.isTraceEnabled())
					log.trace("Warning error stacktrace : ", e);
			}
		}
		
	}
	
	protected void onWelcome(WampWelcomeMessage wampWelcomeMessage) {
		
		//onWelcome should only be called once
		if(!connected){
			if(wampWelcomeMessage.getProtocolVersion() != WampFactory.getProtocolVersion()
			 && log.isWarnEnabled())
				log.warn("server's Wamp protocol version ('"+wampWelcomeMessage.getProtocolVersion()+"') differs from this implementation version ('" + WampFactory.getProtocolVersion() +"')\n"
						+"Errors and weird behavior may occurs");
			
			if(log.isTraceEnabled())
				log.trace("Server's Wamp Implementation : " + wampWelcomeMessage.getImplementation());
	
			sessionId = wampWelcomeMessage.getSessionId();
			
			initHandlers();
			
			welcomeListener.onResult(this);
			//save some memory since listener should be used only once
			welcomeListener=null;
			
			connected=true;
		}else if(log.isErrorEnabled())
			//TODO error log
			log.error("onWelcome called twice on the same connection !!");
			
	}
	
	public void setExclusiveHandler(boolean exclusive){
		this.exclusiveHandler = exclusive;
	}
	
	public boolean isExclusiveHandler(){
		return exclusiveHandler;
	}
	
	public boolean setReconnectPolicy(ReconnectPolicy reconnect){
		if(autoReconnect != ReconnectPolicy.IMPOSSIBLE){
			autoReconnect = reconnect;
			return true;
		}
		return false;
	}
	public ReconnectPolicy getReconnectPolicy(){
		return autoReconnect;
	}
	
	public void addMessageHandler(WampMessageHandler handler){
		messageHandlers.add(handler);
		
		if(connected)
			handler.onConnected(this);
	}

	public boolean containsMessageHandler(WampMessageHandler handler){
		return messageHandlers.contains(handler);
	}
	
	public void removeMessageHandler(WampMessageHandler handler){
		messageHandlers.remove(handler);
	}
	
	public <T extends WampMessageHandler> T getMessageHandler(Class<T> handlerClass){
		for(Iterator<WampMessageHandler> it = messageHandlers.iterator(); it.hasNext();){
			WampMessageHandler h = it.next();
			if(handlerClass.isInstance(h))
				return handlerClass.cast(h);
		}
		return null;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public boolean isConnected(){
		return connected;
	}
	
	protected ObjectMapper getObjectMapper() {
		return mapper;
	}

	protected void setObjectMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	protected ResultListener<WampConnection> getWelcomeListener() {
		return welcomeListener;
	}

	protected void setWelcomeListener(ResultListener<WampConnection> listener) {
		this.welcomeListener = listener;
	}
}
