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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.ContainerProvider;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.Encoder;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.endpoint.WampClientEndPoint;
import com.github.ghetolay.jwamp.endpoint.WampDispatcher;
import com.github.ghetolay.jwamp.message.WampMessageStreamDecoder;
import com.github.ghetolay.jwamp.message.output.WampMessageStreamEncoder;
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
public class WampFactory {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private WampSerializers encoders = new WampSerializers();
	
	private long wampConnectTimeout = 5000;
	
	private static final String protocolName = "wamp";
	private static final int protocolVersion = 1;
	private static final String implementation = "jwamp/0.1";//"jwamp/${project.version}";
	
	private static WampFactory instance;
	
	private WampFactory(){}
	
	public static WampFactory getInstance(){
		if(instance == null)
			instance = new WampFactory();
			
		return instance;
	}
	
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

	public WampSerializers getWampEncoders(){
		return encoders;
	}
	
	public void setWampEncoders(WampSerializers encoders){
		this.encoders = encoders;
	}
	
	/**
	 * Get the default Wamp connect timeout in millisecond.
	 * This is the timeout before both Wamp ends have initiate the wamp connection (welcome message exchanged)
	 * Default set to 5000ms;
	 * 
	 * @return default wait timeout.
	 * @see #setWaitTimeout(long)
	 * @see #setConnectTimeout(long)
	 */
	public long getWampConnectTimeout() {
		return wampConnectTimeout;
	}
	/**
	 * 
	 * Set the default wamp connect timeout in millisecond.
	 * This is the timeout before server send the Welcome message.
	 * Because until a welcome message is received we can't be sure the server is implementing Wamp subprotocol.
	 * 
	 * @param timeout
	 */
	public void setWampConnectTimeout(long timeout) {
		this.wampConnectTimeout = timeout;
	}
	
	/**
	 * 
	 * Connect to the specified uri and return a {@link WampEndpoint} NOT connected yet.
	 * Default WampParameter, connection timeout and wait timeout is used.
	 * 
	 * The ResultListener is used to notify when the connection is open and the welcome message received.
	 * The returned {@link WampEndpoint} should no be used except for configuration or if you wish to close it.
	 * Once the ResultListener is fired the {@link WampEndpoint} became effective but you should rather use 
	 * the {@link WampWebSocket} returned in the listener.  
	 * 
	 * This method does not use the wait timeout, if you need it you should implement it on the listener and close 
	 * the connection with the {@link WampEndpoint} returned;
	 * 
	 * @param uri to connect to.
	 * @param wws is a listener in order to be notified when the connection is effective.
	 * @return a {@link WampEndpoint} not connected.
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #connect(URI)
	 */

//	public WampEndpoint connect(URI uri, ResultListener<WampWebSocket> wws) throws Exception{
//		return connect(uri, true, param.getHandlers(), wws);
//	}
	
	/**
	 * Connect with these specifics handlers instead of those generated by the default {@link WampParameter}.
	 * 
	 * @param uri to connect to.
	 * @param wws is a listener in order to be notified when the connection is effective.
	 * @return a {@link WampEndpoint} not connected.
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #setWampParameter(WampParameter)
	 * @see #connect(URI, ResultListener)
	 * @see #connect(URI)
	 */
//	public WampEndpoint connect(URI uri, Collection<WampMessageHandler> handlers, ResultListener<WampWebSocket> wws) throws Exception{
//		return connect(uri, true, handlers, wws);
//	}
	/**
	 * Connect with a specific connection timeout and handlers.
	 * 
	 * @param uri to connect to.
	 * @param wws is a listener in order to be notified when the connection is effective.
	 * @return a {@link ClientWampConnection} not connected.
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #connect(URI, Collection, ResultListener)
	 * @see #connect(URI, long, ResultListener)
	 * @see #connect(URI, ResultListener)
	 * @see #connect(URI)
	 */
//	public ClientWampConnection connect(URI uri, boolean autoReconnect, final Collection<WampMessageHandler> handlers, final ResultListener<WampWebSocket> wws) throws Exception{
//				
//		ResultListener<ClientWampConnection> connectionListener = new ResultListener<ClientWampConnection>() {
//
//			public void onResult(ClientWampConnection connection) {
//				if(connection != null)
//					wws.onResult(new WampWebSocket(connection));
//				else
//					wws.onResult(null);
//			}
//		};
//		
//		ClientWampConnection connection =  getConnection(uri, autoReconnect, handlers, connectionListener);
//		
//		return connection;
//	}
	
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
//	public WampWebSocket connect(URI uri) throws Exception{
//		return connect(uri, wampConnectTimeout, true, param.getHandlers());
//	}
	
	/**
	 * 
	 * Connect with the specified timeouts.
	 * 
	 * @param uri to connect to.
	 * @param connectTimeout the connect timeout to use.
	 * @param wampConnectTimeout the wamp connect timeout to use.
	 * @param reconnectPolicy specify if whether or not the connection will auto reconnect if the connection permits it.
	 * @return A effective {@link WampWebSocket}
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #connect(URI)
	 * @see #setWampConnectTimeout(long)
	 * @see #setWampParameter(WampParameter)
	 */
//	public WampWebSocket connect(URI uri, long wampConnectTimeout, boolean autoReconnect) throws Exception{
//		return connect(uri,wampConnectTimeout, autoReconnect, param.getHandlers());
//	}
	
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
//	public WampWebSocket connect(URI uri, WampDispatcher dispatcher) throws Exception{
//		return connect(uri, wampConnectTimeout, true,handlers);
//	}
	/**
	 * 
	 * Connect with the specified timeouts handlers.
	 * 
	 * @param uri to connect to.
	 * @param connectTimeout the connect timeout to use.
	 * @param waitTimeout the wait timeout to use.
	 * @param autoReconnect specify if whether or not the connection will auto reconnect.
	 * @param handlers the handlers to use.
	 * @return A effective {@link WampWebSocket}
	 * @throws Exception If the connection is impossible : TimeoutException, InterruptedException, IOException..
	 * this will depend on the WebSocket implementation.
	 * @see #connect(URI)
	 */
	//TODO: reconnectPolicy only at connection (retry on fail)
	public WampWebSocket connect(URI uri, long wampConnectTimeout, boolean autoReconnect, WampDispatcher dispatcher) throws DeploymentException, IOException{
		
		WaitResponse<WampWebSocket> welcomeListener = new WaitResponse<WampWebSocket>(wampConnectTimeout);
		WampClientEndPoint wh = newClientEndpoint(uri, autoReconnect, dispatcher,welcomeListener);
			
		Session session = connect(wh, uri);
			
		WampWebSocket wc = null;
		try{
			wc = welcomeListener.call();
			if(wc == null)
				session.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, "Expecting Welcome message, timeout"));
		}catch (Exception e){
			//TODO log
			e.printStackTrace();
		}
		
		return wc;
	}
	
	
	public Session connect(URI uri, boolean autoReconnect, WampDispatcher dispatcher, ResultListener<WampWebSocket> wws) throws DeploymentException, IOException{	
		return connect(newClientEndpoint(uri, autoReconnect, dispatcher, wws), uri);
	}
	
	public Session connect(WampClientEndPoint endpoint, URI uri) throws DeploymentException, IOException{
		endpoint.setURI(uri);
		
		//can't use Collections.singletonList() :'(
		List<Class<? extends Encoder>> encoders = new ArrayList<Class<? extends Encoder>>(1);
		encoders.add(WampMessageStreamEncoder.class);
		
		List<Class<? extends Decoder>> decoders = new ArrayList<Class<? extends Decoder>>(1);
		decoders.add(WampMessageStreamDecoder.class);
		
		ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
											.preferredSubprotocols(Collections.singletonList("wamp"))
											.encoders(encoders)
											.decoders(decoders)
											.build();
		
		config.getUserProperties().put("jwamp.jsonfactory", this.encoders.getJsonFactory());
		
		return ContainerProvider.getWebSocketContainer().connectToServer(endpoint, config, uri);
	}
	
	private WampClientEndPoint newClientEndpoint(URI uri, boolean autoReconnect, WampDispatcher dispatcher, ResultListener<WampWebSocket> connectionListener) throws DeploymentException, IOException{
		WampClientEndPoint endPoint = new WampClientEndPoint(dispatcher, connectionListener);
		endPoint.setAutoReconnect(autoReconnect);

		return endPoint;
	}
	
}
