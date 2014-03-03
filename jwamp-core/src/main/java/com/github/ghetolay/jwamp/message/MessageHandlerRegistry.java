/**
 * 
 */
package com.github.ghetolay.jwamp.message;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.session.WampSession;

/**
 * @author Kevin
 *
 */
public class MessageHandlerRegistry implements WampMessageHandler {
	private static final Logger log = LoggerFactory.getLogger(MessageHandlerRegistry.class);

	private final Map<MessageType, WampMessageHandler> handlers;

	private MessageHandlerRegistry(Builder b){
		this.handlers = b.handlers;
	}

	@Override
	public Collection<MessageType> getMessageTypes() {
		return handlers.keySet();
	}

	@Override
	public void onMessage(WampSession session, WampMessage message){
		WampMessageHandler handler = handlers.get(message.getMessageType());
		if (handler == null){
			log.warn("No handler registered for " + message.getClass() + " - " + message);
			return;
		}
		
		log.debug("Processing {}", message);
		handler.onMessage(session, message);
	}
	
	public static class Builder{
		Map<MessageType, WampMessageHandler> handlers = new EnumMap<MessageType, WampMessageHandler>(MessageType.class);
		
		public Builder register(WampMessageHandler handler){
			for (MessageType messageType : handler.getMessageTypes()) {
				WampMessageHandler oldValue = handlers.put(messageType, handler);
				if (oldValue != null)
					throw new IllegalArgumentException("Handler for " + messageType + " already registered (" + oldValue +")");
				
				handlers.put(messageType, handler);
			}
			return this;
		}
		
		public MessageHandlerRegistry build(){
			return new MessageHandlerRegistry(this);
		}
	}


}
