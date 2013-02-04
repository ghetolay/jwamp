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

import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.message.WampMessage;

/**
 * A WampConnection is a WebSocket connection with Wamp attributes.
 * Each connection is identified by it's session Id.
 * The messages are handled by {@link WampMessageHandler}.   
 * 
 * @author ghetolay
 * @see WampMessageHandler
 */
public interface WampConnection {
	/**
	 * 
	 * Enum for the reconnect policy. It's a boolean with a third value : IMPOSSIBLE, because in some case it can be impossible to reconnect.
	 * For example when it's a client connection initiate from the other side. 
	 * 
	 * @see WampConnection#setReconnectPolicy(ReconnectPolicy) 
	 * @see	WampConnection#getReconnectPolicy()
	 * @author ghetolay
	 *
	 */
	public static enum ReconnectPolicy{ YES, NO, IMPOSSIBLE };
	
	/**
	 * Send a {@link WampMessage} to the other side of the connection using default format.
	 * This method should not be called directly, use {@link WampWebSocket} methods or a specific {@Link WampMessageHandler} instead.
	 * 
	 * @param msg The {@link WampMessage} to send.
	 * @throws IOException if a connection problem occurs.
	 * @throws SerializationException if a error occurs during serialization.
	 * @see WampWebSocket
	 * @see WampConnection#setPreferBinaryMessaging(boolean)
	 */
	public void sendMessage(WampMessage msg) throws SerializationException, IOException;
	
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
	
	public void setSerializer(WampSerializer serializer);

	public WampSerializer getSerializer();
	
	/**
	 * Set the reconnect policy.
	 * 
	 * @param reconnect The reconnect policy to adopt.
	 * @return If the set was successful i.e. if the redirect is not {@link WampConnection.ReconnectPolicy#IMPOSSIBLE}
	 * @see WampConnection.ReconnectPolicy
	 */
	public boolean setReconnectPolicy(ReconnectPolicy reconnect);
	/**
	 * Return the reconnect policy.
	 * 
	 * @return The reconnect policy.
	 * @see WampConnection.ReconnectPolicy
	 * @see #setReconnectPolicy(ReconnectPolicy)
	 */
	public ReconnectPolicy getReconnectPolicy();
	
	/**
	 * Append a {@link WampMessageHandler} to the end.
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
	
	/**
	 * The session Id is sent by the remote server or set by the server handler.
	 * 
	 * @return The session Id of this connection.
	 */
	public String getSessionId();
	
	/**
	 * Set the max time in millisecond the connection can be idle before it close
	 * 
	 * @param ms Time in millisecond.
	 */
	public void setMaxIdleTime(int ms);
	
	/**
	 * Return the max time in millisecond the connection can be in the idle state before it close.
	 * 
	 * 
	 * @return Max time in millisecond the connection can be idle before it close.
	 */
	public int getMaxIdleTime();
	
	/**
	 * 
	 * Close the connection with the specified closeCode and message.
	 * 
	 * @param closeCode The closeCode to be sent to the other side.
	 * @param message The message or null to be sent to the other side.
	 */
	public void close(int closeCode, String message);
	
	/**
	 * Return true if the connection is connected and active.
	 * 
	 * @return true if connected.
	 */
	public boolean isConnected();
}
