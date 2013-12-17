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


import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import com.github.ghetolay.jwamp.AbstractWampConnection;
import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.WampSerializer;
import com.github.ghetolay.jwamp.utils.ResultListener;
import com.github.ghetolay.jwamp.utils.WaitResponse;

public class JettyConnection extends AbstractWampConnection implements WebSocketListener {

	private URI uri;
	protected Session connection;
	
	private boolean intentionallyClosed = false;
	
	public JettyConnection(URI uri, WampSerializer serializer, Collection<WampMessageHandler> handlers, ResultListener<WampConnection> wr) {
		super(serializer, handlers, wr);
		if(uri != null)
			this.uri = uri;
		else
			autoReconnect = ReconnectPolicy.IMPOSSIBLE;
	}

	public void onWebSocketConnect(Session session) {
		if(log.isTraceEnabled())
			log.trace("New connection opened");
		
		this.connection = session;
	}

	public void onWebSocketText(String data) {
		onMessage(data);
	}
	
	public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
		//TODO: send error ?
	}
	
	public void onWebSocketClose(int closeCode, String message){
		super.onClose(closeCode, message);
		
		if(connection != null 
			&& !intentionallyClosed 
			&& autoReconnect == ReconnectPolicy.YES){
			
			connection = null;
			String oldId = getSessionId();
			if(reconnect()){
				if(log.isDebugEnabled())
					log.debug("Automatic reconnection.\nOld Id:\""+ oldId + "\"  New Id:\"" + getSessionId() + "\"");
				return;
			}
		}
		
		//implementors don't care about being noticed about temporary disconnection
		onClose();
	}
	
	public void onWebSocketError(Throwable error) {
		//TODO 
		log.warn("WebsocketError ",error);
	}
	
	/**
	 * meant to be override
	 */
	public void onClose(){
		
	}
	
	public void close(int closeCode, String message){
		intentionallyClosed = true;
		connection.close(closeCode, message);
	}
	
	public void setMaxIdleTime(long ms) {
		connection.setIdleTimeout(ms);
	}

	public long getMaxIdleTime() {
		return connection.getIdleTimeout();
	}
	
	@Override
	public void sendMessage(String data) throws IOException{
		connection.getRemote().sendString(data);
	}
	
	protected boolean reconnect(){
		reset();
		try {
			//Give it some time
			Thread.sleep(1000);
			
			int i;
			for(i = 0; i <5 ; i++){
				try{
					WaitResponse<WampConnection> wr = new WaitResponse<WampConnection>(3000);
					setWelcomeListener(wr);
					
					WampJettyFactory.getInstance().connect(uri, 10000, this);
					
					if(this.equals(wr.call()))
						return true;
				}catch (Exception e){
					if(log.isDebugEnabled())
						log.debug("Failed to reconnect to " + uri.toString() + " [" + (i+1) + "/5] : " + e.getMessage());	
					
					setWelcomeListener(null);
					Thread.sleep(5000);
				}
			}
			if( i == 5
				&& log.isWarnEnabled())
				log.warn("Unable to reconnect to " + uri.toString());
		} catch (InterruptedException e){
			e.printStackTrace();
			if(log.isErrorEnabled())
				log.error("Thread interrupted while trying to reconnect", e);
		}
		
		return false;
	}	
	
	public Session getConnection(){
		return connection;
	}

}
