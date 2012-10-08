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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;
import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.message.BadMessageFormException;
import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampMessageDeserializer;
import com.github.ghetolay.jwamp.message.WampWelcomeMessage;
import com.github.ghetolay.jwamp.message.output.WampMessageSerializer;
import com.github.ghetolay.jwamp.utils.MyMessagePack;
import com.github.ghetolay.jwamp.utils.ResultListener;

public abstract class AbstractWampConnection implements WampConnection{

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private String sessionId;
	protected ReconnectPolicy autoReconnect = ReconnectPolicy.YES;
	private ObjectMapper mapper;

	private boolean connected = false;
	//bad design should do otherwise
	private ResultListener<WampConnection> welcomeListener;

	//TODO possibility to change the comparator
	private PriorityQueue<WampMessageHandler> messageHandlers;
	private boolean exclusiveHandler = true;

	private boolean preferBynaryMessaging = false;
	private MessagePack msgPack = new MyMessagePack();

	public AbstractWampConnection(ObjectMapper mapper, Collection<WampMessageHandler> messageHandlers,  ResultListener<WampConnection> wl){
		if(mapper != null)
			this.mapper = mapper;
		else
			this.mapper = new ObjectMapper();

		this.welcomeListener = wl;

		if(messageHandlers != null){
			this.messageHandlers = new PriorityQueue<WampMessageHandler>(messageHandlers.size(),new WampMessageHandlerComparator());
			this.messageHandlers.addAll(messageHandlers);
		}else
			this.messageHandlers = new PriorityQueue<WampMessageHandler>(0,new WampMessageHandlerComparator());
	}

	public abstract void sendMessage(String data) throws IOException;
	public abstract void sendMessage(byte[] data) throws IOException;

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
		} catch (Exception e) {
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

	private void sendWelcome(String sessionId) throws SerializationException, IOException{
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

	public void sendMessage(WampMessage msg) throws SerializationException, IOException{
		if(preferBynaryMessaging)
			sendAsBinaryMessage(msg);
		else
			sendAsTextMessage(msg);
	}

	public void sendAsBinaryMessage(WampMessage msg) throws SerializationException, IOException{

		sendMessage(WampMessageSerializer.serialize(msg, msgPack));

		if(log.isDebugEnabled())
			log.debug("Send Binary Message " + msg.toString());
	}

	public void	sendAsTextMessage(WampMessage msg) throws SerializationException, IOException{

		String jsonMsg = WampMessageSerializer.serialize(msg, getObjectMapper());
		sendMessage(jsonMsg);

		if(log.isDebugEnabled())
			log.debug("Send Text Message " + jsonMsg + " sent.");
	}

	public void onMessage(String data){

		if(log.isDebugEnabled())
			log.debug("Receive Text Wamp Message " + data);

		try{
			dispatch(WampMessageDeserializer.deserialize(getObjectMapper().getJsonFactory().createJsonParser(data)));

		} catch(SerializationException e){
			if(log.isWarnEnabled())
				log.warn("Unable to deserialize message : " + data.toString());
		} catch (Exception e){
			if(log.isWarnEnabled()){
				log.warn("Error dispatching nmessage : " + data.toString() + "\nException : " + e.getLocalizedMessage());
				if(log.isTraceEnabled())
					log.trace("Warning error stacktrace : ", e);
			}
		}

	}

	public void onMessage(byte[] data, int offset, int length) {

		if(log.isDebugEnabled())
			log.debug("Receive Binary Wamp Message. size : " + length );

		try{
			WampMessage msg = WampMessageDeserializer.deserialize(data, offset, length, msgPack);
			
			if(log.isDebugEnabled())
				log.debug(" Message received : " + msg.toString());
			
			dispatch(msg);
			
		} catch(SerializationException e){
			if(log.isWarnEnabled())
				log.warn("Unable to deserialize message : " + data.toString());
			if(log.isTraceEnabled())
				log.trace("Warning error stacktrace : ", e);
		} catch (Exception e){
			if(log.isWarnEnabled())
				log.warn("Message not handled because of internal error.\nmessage : " + data + "\nException : " + e.getLocalizedMessage());
			if(log.isTraceEnabled())
				log.trace("Warning error stacktrace : ", e);
		}
	}

	private void dispatch(WampMessage msg) throws BadMessageFormException{

		if(msg.getMessageType() == WampMessage.WELCOME)
			onWelcome((WampWelcomeMessage) msg);

		else{
			for(WampMessageHandler h : messageHandlers)
				for(int type : h.getMsgType()){
					if(msg.getMessageType() == type){
						boolean result = h.onMessage(sessionId, msg);
						if(exclusiveHandler && result)
							return;

						break;
					}
				}

			if(log.isWarnEnabled())
				log.warn("Message not handled.");
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

			if(welcomeListener != null){
				welcomeListener.onResult(this);
				welcomeListener = null;
			}

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

	public void setPreferBinaryMessaging(boolean bool){
		preferBynaryMessaging = bool;
	}

	public boolean preferBinaryMessaging(){
		return preferBynaryMessaging;
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
