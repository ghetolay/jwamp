package com.github.ghetolay.jwamp.endpoint;

import java.net.URI;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import com.github.ghetolay.jwamp.WampFactory;
import com.github.ghetolay.jwamp.WampWebSocket;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampWelcomeMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class WampClientEndPoint extends ExtendedWampEndpoint{

	private static int MAX_RETRY = 5;
	
	private boolean autoReconnect = false;
	private URI uri;
	
	private ResultListener<WampWebSocket> connectionListener;
	
	public WampClientEndPoint(WampDispatcher dispatcher, ResultListener<WampWebSocket> connectionListener){
		super(dispatcher);
		
		this.connectionListener = connectionListener;
	}

	//TODO same as setURI
	public WampDispatcher getWampDispatcher(){
		return dispatcher;
	}
	
	@Override
	public void onOpen(final Session session, EndpointConfig config) {
		session.addMessageHandler(new WelcomeHandler(session));
	}
	
	@Override
	public void onClose(Session session, CloseReason closeReason){
		
		if(autoReconnect
				//This closecode (1006) means close was done locally (thus intentionally) not remotely see JSR356 2.1.5
				&& closeReason.getCloseCode() != CloseCodes.CLOSED_ABNORMALLY){
			
			int retry = 0;
			while(retry >= MAX_RETRY)
				try{
					///!\ if changed were made on WampFactory they'll apply to this new connection
					if( WampFactory.getInstance().connect(this, uri) != null)
						return;
				}catch(Exception e){
					//TODO log
					retry++;
				}
		}
		
		if(log.isDebugEnabled())
			log.debug("Close connection with WebSocket session id : " + session.getId() + " reason : " + closeReason.getReasonPhrase() 
					+ "\n /!\\ Websocket session id may differ from Wamp session id");

		super.onClose(session, closeReason);
    }
	
	//TODO should not be public better package security level but WampFactory isn't in same package.
	public void setURI(URI uri){
		this.uri = uri;
	}
	
	public void setAutoReconnect(boolean autoReconnect){
		this.autoReconnect = autoReconnect;
	}
	public boolean getAutoReconnect(){
		return autoReconnect;
	}
	
	private class WelcomeHandler implements MessageHandler.Whole<WampMessage>{

		Session session;
		
		WelcomeHandler(Session session){
			this.session = session;
		}
		
		@Override
		public synchronized void onMessage(WampMessage message) {

			if(message.getMessageType() == WampMessage.WELCOME){
				WampWelcomeMessage wlMsg = (WampWelcomeMessage)message;
			
				if(wlMsg.getProtocolVersion() != WampFactory.getProtocolVersion()
						&& log.isWarnEnabled())
					log.warn("server's Wamp protocol version ('"+wlMsg.getProtocolVersion()+"') differs from this implementation version ('" + WampFactory.getProtocolVersion() +"')\n"
							+"Errors and weird behavior may occurs");

				if(log.isTraceEnabled())
					log.trace("Server's Wamp Implementation : " + wlMsg.getImplementation());

				final String sessionId = wlMsg.getSessionId();
				
				WampSession wampSession;
				if(sessionId.equals(session.getId()))
					wampSession = new SameWampSession(session);
				else
					wampSession = new DifferWampSession(session, wlMsg.getSessionId());

				//TODO must be test. what if message comes before we had time to add the definitive messageHandler
				session.removeMessageHandler(this);
				session.addMessageHandler(new MessageHandler.Whole<WampMessage>() {

					@Override
					public void onMessage(WampMessage message) {
						dispatcher.onMessage(message, sessionId);
					}
				});
				
				dispatcher.newConnection(wampSession);
				
				connectionListener.onResult(new WampWebSocket(dispatcher, sessionId));
			}else if(log.isErrorEnabled())
				//TODO error log
				log.error("First messsage should be of type welcome");
		}
	}
}
