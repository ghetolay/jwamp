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
package com.github.ghetolay.jwamp.jetty;


import java.net.URI;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.WampConnection.ReconnectPolicy;
import com.github.ghetolay.jwamp.WampFactory;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.WampParameter;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class WampJettyFactory extends WampFactory{
	
	private static WampJettyFactory instance;
	
	private WebSocketClient websocketClient;
		
	public WebSocketClient getWebsocketClient() {
		if( websocketClient == null)
			websocketClient = new WebSocketClient();
		
		return websocketClient;
	}

	public void setWebsocketClient(WebSocketClient websocketClient) {
		this.websocketClient = websocketClient;
	}

	protected WampConnection getConnection(URI uri, long timeout, ReconnectPolicy reconnectPolicy, Collection<WampMessageHandler> handlers, ResultListener<WampConnection> wr) throws TimeoutException, Exception{
		JettyConnection connection = new JettyConnection(uri,getSerializer(),handlers,wr);
		connection.setReconnectPolicy(reconnectPolicy);
		
		connect(uri, timeout, connection);
		
		return connection;
	}
	
	protected void connect(URI uri, long timeout, JettyConnection connection) throws Exception{	

		if(!getWebsocketClient().isStarted())
			websocketClient.start();
		
		ClientUpgradeRequest request = new ClientUpgradeRequest();
		request.setSubProtocols(WampFactory.getProtocolName());
					
		Future<Session> future = websocketClient.connect(connection, uri, request);
		
		if(timeout >0)
			future.get(timeout, TimeUnit.MILLISECONDS);
		else
			future.get();
	}
	
	public void stopWebsocketClient() throws Exception{
		if(websocketClient != null)
			websocketClient.stop();
	}
	
	public WampWebSocketHandler newWebsocketHandler(){
		return newWebsocketHandler(getParameter(),null);
	}
	
	public WampWebSocketHandler newWebsocketHandler(WampParameter param){
		return  newWebsocketHandler(param, null);
	}
	
	public WampWebSocketHandler newWebsocketHandler(JettyWebSocketListener listener){
		return  newWebsocketHandler(getParameter(), listener);
	}
	
	public WampWebSocketHandler newWebsocketHandler(WampParameter param, JettyWebSocketListener listener){
		return new WampWebSocketHandler(getSerializer(),param,listener);
	}
	
	public static WampJettyFactory getInstance(){
		if(instance == null)
			instance = new WampJettyFactory();
		
		return instance;
	}
}
