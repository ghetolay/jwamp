package com.github.ghetolay.jwamp.jetty;


import java.net.URI;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.WampFactory;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.WampParameter;
import com.github.ghetolay.jwamp.WampWebSocketListener;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class WampJettyFactory extends WampFactory{
	
	private static WampJettyFactory instance;
	
	private WebSocketClientFactory fact = new WebSocketClientFactory();
	
	private WampJettyFactory(){}
	
	public WebSocketClientFactory getJettyFactory() {
		return fact;
	}

	public void setJettyFactory(WebSocketClientFactory fact) {
		this.fact = fact;
	}
		
	protected WampConnection getConnection(URI uri, long timeout, Collection<WampMessageHandler> handlers, ResultListener<WampConnection> wr) throws TimeoutException, Exception{
		JettyConnection connection = new JettyConnection(uri,getObjectMapper(),handlers,wr);
		
		connect(uri, timeout, connection);
		
		return connection;
	}
	
	protected void connect(URI uri, long timeout, JettyConnection connection) throws Exception{	
		if(!fact.isStarted())
			fact.start();
			
		WebSocketClient ws = fact.newWebSocketClient();
		ws.setProtocol(getProtocolName());
		
		if(timeout > 0)
			ws.open(uri, connection, timeout, TimeUnit.MILLISECONDS);
		else
			ws.open(uri,connection);
	}
	
	public WampJettyHandler newJettyHandler() throws Exception{
		return newJettyHandler(getParameter(),null);
	}
	
	public WampJettyHandler newJettyHandler(WampParameter param){
		return new WampJettyHandler(param, getObjectMapper(), null);
	}
	
	public WampJettyHandler newJettyHandler(WampWebSocketListener listener){
		return new WampJettyHandler(getParameter(), getObjectMapper(), listener);
	}
	
	public WampJettyHandler newJettyHandler(WampParameter param, WampWebSocketListener listener){
		return new WampJettyHandler(param, getObjectMapper(), listener);
	}
	
	public static WampJettyFactory getInstance(){
		if(instance == null)
			instance = new WampJettyFactory();
		
		return instance;
	}
}
