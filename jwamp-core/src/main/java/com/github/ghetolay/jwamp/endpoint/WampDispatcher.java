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


import javax.websocket.CloseReason;
import javax.websocket.Session;

import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.message.WampMessage;

/**
 * A WampConnection is a WebSocket connection with Wamp attributes.
 * Each connection is identified by it's session Id.
 * The messages are handled by {@link WampMessageHandler}.   
 * 
 * @author ghetolay
 * @see WampMessageHandler
 */
public interface WampDispatcher{
	
	//TODO we could get EndpointConfig to pass it to handlers
	public void newConnection(WampSession session);
	
	public void closeConnection(Session session, CloseReason reason);
	
	public void onMessage(WampMessage msg, String sessionId);
	
	public SessionManager getSessionManager();
	
	/**
	 * Set whether we should stop at the first successful handler or not.
	 * So if multiple handlers are handling the same message type, exclusiveHandler should be false.
	 * 
	 * @param exclusiveHandler
	 */
	public void setExclusiveHandler(boolean exclusiveHandler);
	/**
	 * Return if handlers are exclusive or not.
	 * 
	 * @return If handlers are exclusive or not.
	 * @see #setExclusiveHandler(boolean)
	 */
	public boolean isExclusiveHandler();
	
	
	/**
	 * This is meant to be called to initiate handlers collection before a connection is made.
	 * It can be called once the connection is established but {@link WampMessageHandler#onConnected(WampConnection)} will not be fired.
	 * If you want to add {@link WampMessageHandler} after a connection was established use {@link #setMessageHandler(Collection)} instead.
	 * 
	 * 
	 * @param messageHandlers collection of {@link WampMessageHandler} to add
	 */
	//public void setMessageHandler(Collection<WampMessageHandler> messageHandlers);
	
	/**
	 * Append a {@link WampMessageHandler} to the end.
	 * @param <T>
	 * 
	 * @param handler The {@link WampMessageHandler} to add.
	 * @see WampMessageHandler
	 */
	public void addMessageHandler(WampMessageHandler handler);
	/**
	 * Test if the handler is (still) contained by this connection.
	 * 
	 * @param handler The {@link WampMessageHandler} whose the presence is tested.
	 * @return True if this connection contains the handler.
	 * @see WampMessageHandler
	 */
	public boolean containsMessageHandler(WampMessageHandler handler);
	/**
	 * Remove the handler.
	 * 
	 * @param handler The {@link WampMessageHandler} to remove.
	 * @see WampMessageHandler
	 */
	public void removeMessageHandler(WampMessageHandler handler);
	/**
	 * Return the first handler of type handlerClass.
	 * This method should be deprecated soon.
	 * 
	 * @param handlerClass The class to be test.
	 * @return The first handler of type handlerClass.
	 * 
	 */
	public <T extends WampMessageHandler> T getMessageHandler(Class<T> handlerClass);
	
}
