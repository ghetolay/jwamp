package com.github.ghetolay.jwamp.jetty;

import java.util.Collection;


import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.WampFactory;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.WampParameter;
import com.github.ghetolay.jwamp.WampWebSocket;
import com.github.ghetolay.jwamp.WampWebSocketListener;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class WampJettyHandler extends WebSocketHandler{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private WampParameter param;
	private ObjectMapper mapper;
	private WampWebSocketListener listener;
		
	public WampJettyHandler(WampParameter param, ObjectMapper mapper, WampWebSocketListener newConnectionListener){
		this.param = param;
		this.mapper = mapper;
		this.listener = newConnectionListener;
	}
	
	public WebSocket doWebSocketConnect(final HttpServletRequest request, final String subprotocol) {
		if(subprotocol != null && WampFactory.getProtocolName().equals(subprotocol.toUpperCase())){
			
			if(log.isTraceEnabled())
				log.trace("New Wamp Connection from " + request.getRemoteAddr());
			
			JettyServerConnection jsc;
			
			if(listener != null)
				jsc = new JettyServerConnection(mapper , param.getnewHandlers(), 
						new ResultListener<WampConnection>() {
							public void onResult(WampConnection result) {
								listener.newWampWebSocket(request, new WampWebSocket(result));
							}
						});
			else
				jsc = new JettyServerConnection(mapper,param.getnewHandlers(),null);
			
			return jsc;
		}
		else
			return new WebSocket(){

				public void onOpen(Connection connection) {
					//Not sure 1002 is the appropriate close code 
					connection.close(1002, "Unsupported subprotocol : " + subprotocol);
				}

				public void onClose(int closeCode, String message) {}
				
			};
	}
	
	public WampConnection getWampConnection(String uuid){
		return getWampConnection(uuid);
	}
	
	private class JettyServerConnection extends JettyConnection{
		
		public JettyServerConnection(ObjectMapper mapper, Collection<WampMessageHandler> handlers, ResultListener<WampConnection> rl) {
			super(null,mapper, handlers, rl);
		}

		@Override
		public void onOpen(Connection connection){
			super.onOpen(connection);
			newClientConnection();
		}
	}
}
