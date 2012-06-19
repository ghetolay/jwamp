package com.github.ghetolay.jwamp.jetty;


import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.websocket.WebSocket;

import com.github.ghetolay.jwamp.AbstractWampConnection;
import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class JettyConnection extends AbstractWampConnection implements WebSocket.OnTextMessage {

	private URI uri;
	protected Connection connection;
	
	public JettyConnection(URI uri, ObjectMapper mapper, Collection<WampMessageHandler> handlers, ResultListener<WampConnection> wr) {
		super(mapper, handlers, wr);
		if(uri != null)
			this.uri = uri;
		else
			autoReconnect = ReconnectPolicy.IMPOSSIBLE;
	}

	public void onOpen(Connection connection) {
		if(log.isTraceEnabled())
			log.trace("New connection opened");
		
		this.connection = connection;
	}

	public void onClose(int closeCode, String message){
		if(connection != null){
			connection = null;
			super.onClose(closeCode, message);
		}
	}
	
	public void close(int closeCode, String message){
		connection.close(closeCode, message);
		
		connection = null;
	}
	
	@Override
	public void sendMessage(String data) throws IOException{
		connection.sendMessage(data);
	}
	
	@Override
	protected void reconnect(){
		int i;
		for(i = 0; i <5 ; i++){
			try{
				WampJettyFactory.getInstance().connect(uri, 10000, this);
				return;
			}catch (Exception e){
				if(log.isDebugEnabled())
					log.debug("Failed to reconnect to " + uri.toString() + " [" + (i+1) + "/5] : " + e.getMessage());	
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1){
					e.printStackTrace();
					if(log.isErrorEnabled())
						log.error("Thread interrupted while trying to reconnect", e1);
				}
			}
		}
		if( i == 5
			&& log.isWarnEnabled())
			log.warn("Unable to reconnect to " + uri.toString());
	}
	
}
