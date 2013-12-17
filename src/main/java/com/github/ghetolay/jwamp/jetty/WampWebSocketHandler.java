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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.github.ghetolay.jwamp.WampFactory;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.WampParameter;
import com.github.ghetolay.jwamp.WampSerializer;
import com.github.ghetolay.jwamp.WampWebSocket;

/**
 * @author ghetolay
 *
 */
public class WampWebSocketHandler extends WebSocketHandler implements WebSocketCreator{
	
	private WampSerializer serializer;
	private WampParameter param;
	
	private JettyWebSocketListener listener;
	private Map<JettyServerConnection,UpgradeRequest> requests = new HashMap<JettyServerConnection, UpgradeRequest>();
	
	public WampWebSocketHandler(WampSerializer serializer, WampParameter param, JettyWebSocketListener listener){
		this.serializer = serializer;
		this.param = param;
		this.listener = listener;
	}
	
	public WampSerializer getSerializer() {
		return serializer;
	}

	public void setSerializer(WampSerializer serializer) {
		this.serializer = serializer;
	}

	public WampParameter getParam() {
		return param;
	}

	public void setParam(WampParameter param) {
		this.param = param;
	}

	public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
		resp.setAcceptedSubProtocol(WampFactory.getProtocolName());
		
		if(req.hasSubProtocol(WampFactory.getProtocolName())){
			
			JettyServerConnection connection =  new JettyServerConnection(serializer, param.getHandlers());
			
			if(listener != null)
				requests.put(connection, req);
			
			return connection;
		}
		else
			return null;
	}
	
	public void configure(WebSocketServletFactory factory) {
		factory.setCreator(this);
	}
	
	//TODO possible leak => listener nor null on createWebSocket() but null onWebSocketConnect() or if onWebSocketConnect() never called
	//listener/requests should be done differently
	private class JettyServerConnection extends JettyConnection{
		
		public JettyServerConnection(WampSerializer serializer, Collection<WampMessageHandler> handlers) {
			super(null,serializer, handlers, null);
		}

		@Override
		public void onWebSocketConnect(Session session){
			super.onWebSocketConnect(session);
			newClientConnection();
			if(listener != null)
				listener.newWebSocket(requests.remove(this), new WampWebSocket(this));
		}
		
		@Override
		public void onClose(){
			if(listener != null)
				listener.closedWebSocket(getSessionId());
		}
	}
	
}
