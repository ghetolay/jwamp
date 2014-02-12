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

package com.github.ghetolay.jwamp.endpoint;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampPrefixMessage;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.message.WampUnsubscribeMessage;
import com.github.ghetolay.jwamp.message.WampWelcomeMessage;

public class SortedWampDispatcher extends AbstractWampDispatcher {
	
	private static final Class<?>[][] castClasses = new Class<?>[][]{
		/* Welcome		*/	{ WampWelcomeMessage.class, 	WampMessageHandler.Welcome.class},
		/* Prefix 		*/	{ WampPrefixMessage.class, 		WampMessageHandler.Prefix.class},
		/* Call 		*/	{ WampCallMessage.class, 		WampMessageHandler.Call.class},
		/* CallResult 	*/	{ WampCallResultMessage.class, 	WampMessageHandler.CallResult.class},
		/* CallError 	*/	{ WampCallErrorMessage.class, 	WampMessageHandler.CallError.class},
		/* Subscribe 	*/	{ WampSubscribeMessage.class, 	WampMessageHandler.Subscribe.class},
		/* Unsubscribe 	*/	{ WampUnsubscribeMessage.class, WampMessageHandler.Unsubscribe.class},
		/* Publish 		*/	{ WampPublishMessage.class,		WampMessageHandler.Publish.class},
		/* Event 		*/	{ WampEventMessage.class, 		WampMessageHandler.Event.class},
		
	};

	private List<?>[] sortedHandlers = new LinkedList<?>[9];
	
	public SortedWampDispatcher(SessionManager sessions){
		super(sessions);
	}
	
	/*
	public WampEndpoint(Collection<WampMessageHandler> messageHandlers){
		setMessageHandler(messageHandlers);
	}
	*/
	
	protected void onOpen(Session session, EndpointConfig config){
		sessions.newSession(session);
	}
	
	protected void onClose(Session session, CloseReason closeReason){
		sessions.closeSession(session);
	}

	/*
	public void onMessage(String data){

		if(log.isDebugEnabled())
			log.debug("Receive Text Wamp Message " + data);

		try{
			dispatch(WampMessageDeserializer.deserialize(serializer.getObjectMapper().getJsonFactory().createJsonParser(data)));

		} catch(SerializationException e){
			if(log.isWarnEnabled()){
				log.warn("Unable to deserialize message : " + data.toString());
				if(log.isTraceEnabled())
					log.trace("Warning error stacktrace : ", e);
			}
		} catch (Exception e){
			if(log.isWarnEnabled()){
				log.warn("Error dispatching nmessage : " + data.toString() + "\nException : " + e.getLocalizedMessage());
				if(log.isTraceEnabled())
					log.trace("Warning error stacktrace : ", e);
			}
		}

	}

	private void dispatch(WampMessage msg) throws BadMessageFormException{
*/
	
	public void onMessage(WampMessage msg, String sessionId) {

			int type = msg.getMessageType();
			//don't test let's the outofbound exception be thrown
			//if(idx < 9)
			try{
				if(sortedHandlers[type] != null){
				
					Iterator<?> it = sortedHandlers[type].iterator();
					Class<?> handlerCast = castClasses[type][1];
					Class<?> messageCast = castClasses[type][0];
					
					while(it.hasNext()){	
						boolean handled = false;
						
						try {
							handled = (Boolean)handlerCast.getMethod("onMessage").invoke(it.next(),
																						 sessionId, messageCast.cast(msg));
						} catch(InvocationTargetException e){
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ReflectiveOperationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (RuntimeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 

						if(isExclusiveHandler() && handled)
							return;
					}
				}
			} catch(IndexOutOfBoundsException e){
				if(log.isWarnEnabled())
					log.warn("Unknown Message type : " + type);
			}
			
			if(log.isWarnEnabled())
				log.warn("Message not handled.");
	}
	
	@SuppressWarnings("unchecked")
	private void addHandler(int type, WampMessageHandler handler){
		if(sortedHandlers[type] == null)
			sortedHandlers[type] = new LinkedList<WampMessageHandler>();
		
		((LinkedHashSet<WampMessageHandler>)sortedHandlers[type]).add(handler);
	}
	
	public void addMessageHandler(WampMessageHandler handler){
		
		Iterator<Class<?>> interfacesIt = ClassUtils.getAllInterfaces( handler.getClass())
										  .iterator();
		
		boolean implementsMessage = false;
		
		while(interfacesIt.hasNext()){
			Class<?> clazz = interfacesIt.next();
			
			for(int i = 0; i < 9; i++)
				if(castClasses[i][1].isAssignableFrom(clazz)){
					addHandler(i, handler);
					implementsMessage = true;
				}
		}
			
		if(!implementsMessage)
			throw new IllegalArgumentException("Handler must implements at least one message type handler");
		
		Iterator<Session> it = sessions.getActiveSessions();
		while(it.hasNext()){
			Session s = it.next();
			if(s.getUserProperties().get("jwamp.connected") != null)
				handler.onOpen(s.getId());
		}
		
		handler.init(this);
	}
	
	public boolean containsMessageHandler(WampMessageHandler handler){
		for(int i = 0; i < 7; i++)
			if(sortedHandlers[i] != null && sortedHandlers[i].contains(handler))
				return true;
		return false;
	}

	public void removeMessageHandler(WampMessageHandler handler){
		for(int i = 0; i < 7; i++)
			if(sortedHandlers[i] != null)
				sortedHandlers[i].remove(handler);
	}

	//we may check multiple times same messagehandler
	//who cares ? it's very rarely using this methods
	public <T extends WampMessageHandler> T getMessageHandler(Class<T> handlerClass){
		for(int i = 0; i < 7; i++)
			for(Iterator<?> it = sortedHandlers[i].iterator(); it.hasNext();){
				Object h = it.next();
				if(handlerClass.isInstance(h))
					return handlerClass.cast(h);
			}
		return null;
	}

	public SessionManager getSessionManager() {
		return sessions;
	}

	@Override
	protected Iterator<WampMessageHandler> getHandlerIterator() {
		// TODO Auto-generated method stub
		return null;
	}
}
