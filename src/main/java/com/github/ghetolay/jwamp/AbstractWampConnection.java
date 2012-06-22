package com.github.ghetolay.jwamp;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.message.BadMessageFormException;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampWelcomeMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;

public abstract class AbstractWampConnection implements WampConnection{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private String sessionId;
	protected ReconnectPolicy autoReconnect = ReconnectPolicy.YES;
	private ObjectMapper mapper;
	
	private ResultListener<WampConnection> welcomeListener;
		
	private Set<WampMessageHandler> messageHandlers = new HashSet<WampMessageHandler>();
	private boolean exclusiveHandler = true;
	
	public AbstractWampConnection(ObjectMapper mapper, Collection<WampMessageHandler> handlers,  ResultListener<WampConnection> wl){
		if(mapper != null)
			this.mapper = mapper;
		else
			this.mapper = new ObjectMapper();
		
		if(wl != null)
			this.welcomeListener = wl;
		else
			welcomeListener = new ResultListener<WampConnection>() {
				public void onResult(WampConnection result) {}
			};
		
		if(handlers!=null)
			for(WampMessageHandler h : handlers)
				addMessageHandler(h);
	}
	
	public abstract void sendMessage(String data) throws IOException;
	
	public void onClose(int closeCode, String message){
		for(WampMessageHandler h : messageHandlers)
			h.onClose(sessionId, closeCode);
	}
	
	protected void reset(){
		welcomeListener = new ResultListener<WampConnection>() {
			public void onResult(WampConnection result) {}
		};
	}
	
	public void newClientConnection(){
		try {
			sendWelcome();
			
			initHandlers();
			
			if(welcomeListener != null){
				welcomeListener.onResult(this);
				welcomeListener = null;
			}
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
	
	private void sendWelcome() throws IOException{
		UUID uuid = UUID.randomUUID();
		
		if(log.isTraceEnabled())
			log.trace("Send welcome with sessionId : " + uuid.toString());
		
		WampWelcomeMessage msg = new WampWelcomeMessage();
		msg.setImplementation(WampFactory.getImplementation());
		msg.setProtocolVersion(WampFactory.getProtocolVersion());
		msg.setSessionId(uuid.toString());
		
		sendMessage(msg);
		
		sessionId = uuid.toString();
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
			Object[] array = getObjectMapper().readValue(data, Object[].class);
			
			if(array == null || array.length < 2 || array[0] == null)
				throw new BadMessageFormException("WampMessage must be a not null JSON array with at least 2 elements and first element can't be null");
			
			int messageType = (Integer)array[0];
			
	
			switch(messageType){
			/*
				case WampMessage.PREFIX :
					handler.onPrefix(new WampPrefixMessage(array));
					return;
			*/
				case WampMessage.WELCOME :
					onWelcome(new WampWelcomeMessage(array));
					return;
			}
			
			for(WampMessageHandler h : messageHandlers){
				boolean result = h.onMessage(sessionId, messageType, array);
				if(exclusiveHandler && result)
					return;
			}
			
			if(log.isErrorEnabled())
				log.error("Message Id not recognized : " + messageType);
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassCastException e){
			//TODO
			e.printStackTrace();
		} catch (BadMessageFormException e){
			//TODO
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected void onWelcome(WampWelcomeMessage wampWelcomeMessage) {
		
		//onWelcome should only be called once, we use listener as a boolean to determine if onWelcome was already called
		if(welcomeListener != null){
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
		
		//mean we already send or receive the welcome message i.e. we are connected 
		if(welcomeListener == null)
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
