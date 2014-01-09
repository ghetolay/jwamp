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
package com.github.ghetolay.jwamp.server.jetty;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.github.ghetolay.jwamp.DefaultWampParameter;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.WampWebSocket;
import com.github.ghetolay.jwamp.jetty.JettyWebSocketListener;
import com.github.ghetolay.jwamp.jetty.WampJettyFactory;
import com.github.ghetolay.jwamp.jetty.WampWebSocketHandler;
import com.github.ghetolay.jwamp.rpc.DefinedMethodRPCManager;
import com.github.ghetolay.jwamp.server.rpc.TestDefinedAction;

public class TestServer implements JettyWebSocketListener {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private static Server server;
	
	private static Map<String,WampWebSocket> connections = new HashMap<String,WampWebSocket>();
	
	public static void main(String[] args){
		new TestServer().test();
	}
	
	@Test
	public void test(){

		server = new Server(8080);
		
		WampJettyFactory wampFact = WampJettyFactory.getInstance();
		
		try{
			InputStream is = getClass().getResourceAsStream("/wamp-server.xml");
			
			WampWebSocketHandler wampHandler = wampFact.newWebsocketHandler(new Parameters(is),this);
			
			//wampFact.getSerializer().setDesiredFormat(WampSerializer.format.BINARY);
	
			
			server.setHandler(wampHandler);
	
			server.start();
			//server.join();
		}catch(InterruptedException e){
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void stopConnections(){
		for(Entry<String,WampWebSocket> entry: connections.entrySet())
			entry.getValue().getConnection().close(StatusCode.SHUTDOWN, "");
	}
	
	public static void stop(){
		try {
			server.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void newWebSocket(UpgradeRequest request, WampWebSocket wws) {
		connections.put(wws.getConnection().getSessionId(),wws);
	}

	public void closedWebSocket(String sessionId){
		connections.remove(sessionId);
	}
	
	private class Parameters extends DefaultWampParameter.SimpleServerParameter{

		public Parameters(InputStream is) throws Exception {
			super(is);
		}
		
		@Override
		public Collection<WampMessageHandler> getHandlers(){
			Collection<WampMessageHandler> handlers = super.getHandlers();
			
			handlers.add(new DefinedMethodRPCManager(new TestDefinedAction()));
			
			return handlers;
		}
		
	}
}
