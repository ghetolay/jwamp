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
import java.util.LinkedList;
import java.util.List;

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

public class DefaultWampDispatcher extends AbstractWampDispatcher {
	
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

	private List<WampMessageHandler> handlers = new LinkedList<WampMessageHandler>();
	
	public DefaultWampDispatcher(SessionManager sessions){
		super(sessions);
	}
	
	
	
	public void onMessage(WampMessage msg, String sessionId) {

		if(log.isTraceEnabled())
			log.trace("Received Message from " + sessionId + " : " + msg);
		
		int type = msg.getMessageType();
		//don't test let's the outofbound exception be thrown
		//if(idx < 9)
		try{
			Iterator<WampMessageHandler> it = handlers.iterator();
			Class<?> handlerCast = castClasses[type][1];
			Class<?> messageCast = castClasses[type][0];
			
			WampMessageHandler h;
			while(it.hasNext() && handlerCast.isInstance( h = it.next() )){						
				try {
					handlerCast.getMethod("onMessage", String.class, messageCast)
									.invoke(h, sessionId, messageCast.cast(msg));
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

				if(isExclusiveHandler())
					return;
			}
			
		} catch(IndexOutOfBoundsException e){
			if(log.isWarnEnabled())
				log.warn("Unknown Message type : " + type);
		}
		
		if(log.isWarnEnabled())
			log.warn("Message not handled.");
	}
	
	public void addMessageHandler(WampMessageHandler handler){
		
		handlers.add(handler);
		handler.init(sessions);
		
		Iterator<WampSession> it = sessions.getActiveWampSessions();
		while(it.hasNext())
			handler.onOpen(it.next().getWampSessionId());
	}
	
	public boolean containsMessageHandler(WampMessageHandler handler){
		return handlers.contains(handler);
	}

	public void removeMessageHandler(WampMessageHandler handler){
		handlers.remove(handler);
	}

	public <T extends WampMessageHandler> T getMessageHandler(Class<T> handlerClass){
		for(WampMessageHandler h : handlers){
			if(handlerClass.isInstance(h))
				return handlerClass.cast(h);
		}
		
		return null;
	}

	@Override
	protected Iterator<WampMessageHandler> getHandlerIterator() {
		return handlers.iterator();
	}
}
