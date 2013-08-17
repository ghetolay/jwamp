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


import java.net.URI;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampConnection.ReconnectPolicy;
import com.github.ghetolay.jwamp.utils.ResultListener;
import com.github.ghetolay.jwamp.utils.WaitResponse;

/**
 * This is the base factory class for WampWebsocket creation.
 * 
 * All WampWebSocket should be created with the same factory for efficiency.
 * It permit for example to share the JSON ObjectMapper instead of create one for each connection.
 * 
 * Its also a good way to adapt to any WebSocket implementation by extending this class.  
 * 
 * This class only manage client connection, for server connection an extension must be made because 
 * this is specific to the WebSocket implementation.
 * 
 * @author ghetolay
 *
 */
public abstract class WampFactory {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private WampParameter param = new DefaultWampParameter.SimpleClientParameter();
	
	private WampSerializer serializer = new WampSerializer();
	
	private long timeout = 5000;
	private long waitTimeout = 5000;
	
	private static final String protocolName = "WAMP";
	private static final int protocolVersion = 1;
	private static final String implementation = "jwamp/0.1";//"jwamp/${project.version}";
	
	/**
	 * Return the name of the WebSocket subprotocol.
	 * 
	 * @return The subprotocol name i.e. WAMP 
	 */
	public static String getProtocolName() {
		return protocolName;
	}

	/**
	 * 
	 * @return The protocol version.
	 */
	public static int getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * The WAMP implementation name in the form of "jwamp/VERSION".
	 * 
	 * @return The WAMP implementation name.
	 */
	public static String getImplementation() {
		return implementation;
	}
	
	/**
	 * Get the default {@link WampParameter} used for the creation of {@link WampWebSocket}
	 * 
	 * @return Default {@link WampParamter}
	 * @see #setWampParameter(WampParameter)
	 * @see WampParameter
	 */
	public WampParameter getParameter(){
		return param;
	}

	/**
	 * Set the default {@link WampParameter} to be used for the creation of {@link WampWebSocket}
	 * This parameters can be override with the handlers argument of method connect(). 
	 * 
	 * @param param to be set as default.
	 * @see WampParameter
	 */
	public void setWampParameter(WampParameter param){
		this.param = param;
	}
	
	public WampSerializer getSerializer() {
		return serializer;
	}

	public void setSerializer(WampSerializer serializer) {
		this.serializer = serializer;
	}
	
	/**
	 * Get the default connection timeout in millisecond.
	 * Default set to 5000ms;
	 * 
	 * @return default connection timeout.
	 * @see #setConnectTimeout(long)
	 * @see #setWaitTimeout(long)
	 */
	public long getConnectTimeout() {
		return timeout;
	}

	/**
	 * Set the default connection timeout in millisecond.
	 * This timeout only include the connection attempt and the WebSocket handshaking. 
	 * 
	 * @param timeout to be set.
	 * @see #setWaitTimeout(long)
	 */
	public void setConnectTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * Get the default wait timeout in millisecond.
	 * Default set to 5000ms;
	 * 
	 * @return default wait timeout.
	 * @see #setWaitTimeout(long)
	 * @see #setConnectTimeout(long)
	 */
	public long getwaitTimeout() {
		return waitTimeout;
	}
	/**
	 * 
	 * Set the default wait timeout in millisecond.
	 * The wait time is the time server make to send the Welcome message.
	 * Because until a welcome message is received we can't be sure the server is implementing Wamp subprotocol.
	 * 
	 * @param timeout
	 */
	public void setWaitTimeout(long timeout) {
		this.waitTimeout = timeout;
	}
	
	/**
	 * 
	 * Connect to the specified uri and return a {@link WampConnection} NOT connected yet.
	 * Default WampParameter, connection timeout and wait timeout is used.
	 * 
	 * The ResultListener is used to notify when the connection is open and the welcome message received.
	 * The returned {@link WampConnection} should no be used except for configuration or if you wish to close it.
	 * Once the ResultListener is fired the {@link WampConnection} became effective but you should rather use 
	 * the {@link WampWebSocket} returned in the listener.  
	 * 
	 * This method does not use the wait timeout, if you need it you should implement it on the listener and close 
	 * the connection with the {@link WampConnection} returned;
	 * 
	 * @param uri to connect to.
	 * @param wws is a listener in order to be notified when the connection is effective.
	 * @return a {@link WampConnection} not connected.
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #connect(URI)
	 */
	public WampConnection connect(URI uri, ResultListener<WampWebSocket> wws) throws Exception{
		return connect(uri, timeout, ReconnectPolicy.YES, param.getHandlers(), wws);
	}
	
	/**
	 * Connect with a specific connection timeout.
	 * 
	 * @param uri to connect to.
	 * @param connectTimeout the connect timeout to use.
	 * @param wws is a listener in order to be notified when the connection is effective.
	 * @return a {@link WampConnection} not connected.
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #connect(URI, ResultListener)
	 * @see #connect(URI)
	 */
	public WampConnection connect(URI uri, long connectTimeout, ResultListener<WampWebSocket> wws) throws Exception{
		return connect(uri, connectTimeout, ReconnectPolicy.YES, param.getHandlers(), wws);
	}
	
	/**
	 * Connect with these specifics handlers instead of those generated by the default {@link WampParameter}.
	 * 
	 * @param uri to connect to.
	 * @param wws is a listener in order to be notified when the connection is effective.
	 * @return a {@link WampConnection} not connected.
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #setWampParameter(WampParameter)
	 * @see #connect(URI, ResultListener)
	 * @see #connect(URI)
	 */
	public WampConnection connect(URI uri, Collection<WampMessageHandler> handlers, ResultListener<WampWebSocket> wws) throws Exception{
		return connect(uri, timeout, ReconnectPolicy.YES, handlers, wws);
	}
	/**
	 * Connect with a specific connection timeout and handlers.
	 * 
	 * @param uri to connect to.
	 * @param connectTimeout the connect timeout to use.
	 * @param wws is a listener in order to be notified when the connection is effective.
	 * @return a {@link WampConnection} not connected.
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #connect(URI, Collection, ResultListener)
	 * @see #connect(URI, long, ResultListener)
	 * @see #connect(URI, ResultListener)
	 * @see #connect(URI)
	 */
	public WampConnection connect(URI uri, long connectTimeout, ReconnectPolicy reconnectPolicy, final Collection<WampMessageHandler> handlers, final ResultListener<WampWebSocket> wws) throws Exception{
				
		ResultListener<WampConnection> connectionListener = new ResultListener<WampConnection>() {

			public void onResult(WampConnection connection) {
				if(connection != null)
					wws.onResult(new WampWebSocket(connection));
				else
					wws.onResult(null);
			}
		};
		
		WampConnection connection =  getConnection(uri, connectTimeout, reconnectPolicy, handlers, connectionListener);
		
		return connection;
	}
	
	/**
	 * 
	 * Connect to the specified uri with the default connection and wait timeout and with the default {@link WampParameter}.
	 * This method will block until the connection is avalaible or a timeout expires.
	 * If you rather need a non blocking method see {@link #connect(URI, ResultListener)}.
	 * 
	 * @param uri to connect to.
	 * @return A effective {@link WampWebSocket}
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #setConnectTimeout(long)
	 * @see #setWaitTimeout(long)
	 * @see #setWampParameter(WampParameter)
	 * @see #connect(URI, ResultListener)
	 */
	public WampWebSocket connect(URI uri) throws Exception{
		return connect(uri, timeout, waitTimeout, ReconnectPolicy.YES, param.getHandlers());
	}
	
	/**
	 * 
	 * Connect with the specified timeouts.
	 * 
	 * @param uri to connect to.
	 * @param connectTimeout the connect timeout to use.
	 * @param waitTimeout the wait timeout to use.
	 * @param reconnectPolicy specify if whether or not the connection will auto reconnect if the connection permits it.
	 * @return A effective {@link WampWebSocket}
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #connect(URI)
	 * @see #setWampParameter(WampParameter)
	 */
	public WampWebSocket connect(URI uri, long connectTimeout, long waitTimeout, ReconnectPolicy reconnectPolicy) throws Exception{
		return connect(uri,connectTimeout, waitTimeout, reconnectPolicy, param.getHandlers());
	}
	
	/**
	 * 
	 * Connect with default timeouts and the specified handlers instead of those generated by the default {@link WampParameter}.
	 * 
	 * @param uri to connect to.
	 * @param handlers the handlers to use.
	 * @return A effective {@link WampWebSocket}
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #connect(URI)
	 * @see #setWampParameter(WampParameter)
	 */
	public WampWebSocket connect(URI uri, Collection<WampMessageHandler> handlers) throws Exception{
		return connect(uri, timeout, waitTimeout, ReconnectPolicy.YES,handlers);
	}
	/**
	 * 
	 * Connect with the specified timeouts handlers.
	 * 
	 * @param uri to connect to.
	 * @param connectTimeout the connect timeout to use.
	 * @param waitTimeout the wait timeout to use.
	 * @param reconnectPolicy specify if whether or not the connection will auto reconnect.
	 * @param handlers the handlers to use.
	 * @return A effective {@link WampWebSocket}
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #connect(URI)
	 */
	//TODO: reconnectPolicy only at connection (retry on fail)!
	public WampWebSocket connect(URI uri, long connectTimeout, long waitTimeout, ReconnectPolicy reconnectPolicy, Collection<WampMessageHandler> handlers) throws Exception{
			
		WaitResponse<WampConnection> wr = new WaitResponse<WampConnection>(waitTimeout);
		
		WampConnection connection = getConnection(uri, connectTimeout, reconnectPolicy, handlers, wr);
	
		
		if(connection != null){
			WampWebSocket wws = new WampWebSocket(connection);
			
			try{
				if( connection.equals(wr.call()))
					return wws;
				else
					connection.close(-1, null);
			}catch (Exception e){
				//TODO log
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	protected abstract WampConnection getConnection(URI uri, long connectTimeout, ReconnectPolicy reconnectPolicy, Collection<WampMessageHandler> handlers, ResultListener<WampConnection> wc) throws Exception;
}
