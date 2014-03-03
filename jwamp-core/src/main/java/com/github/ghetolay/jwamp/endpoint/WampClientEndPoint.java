package com.github.ghetolay.jwamp.endpoint;

import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.ClientEndpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampBuilder;
import com.github.ghetolay.jwamp.message.MessageType;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampMessageDecoder;
import com.github.ghetolay.jwamp.message.WampMessageEncoder;
import com.github.ghetolay.jwamp.message.WampMessageHandler;
import com.github.ghetolay.jwamp.message.WampWelcomeMessage;
import com.github.ghetolay.jwamp.session.WampSession;
import com.github.ghetolay.jwamp.session.WampSessionConfig;
import com.github.ghetolay.jwamp.utils.ResultListener;

@ClientEndpoint(encoders={WampMessageEncoder.Text.class}, decoders={WampMessageDecoder.Text.class}, subprotocols={"wamp"})
public class WampClientEndPoint extends AbstractWampEndpoint {
	
	private static final Logger log = LoggerFactory.getLogger(WampClientEndPoint.class);
	
	private final ResultListener<WampSession> connectionListener;

	public WampClientEndPoint(WampEndpointParameters parameters, ResultListener<WampSession> connectionListener) {
		super(parameters);
		this.connectionListener = connectionListener;
	}

	@OnOpen
	@Override
	public void onOpen(final Session session, EndpointConfig config) {
		super.onOpen(session, config);
		
		session.addMessageHandler(new WelcomeHandler(session, connectionListener));
	}
	
	private class WelcomeHandler implements MessageHandler.Whole<WampMessage>{

		private final ResultListener<WampSession> connectionListener;
		private final Session session;
		
		private WampMessageHandler messageHandler = new QueueingMessageHandler(100);
		private WampSession wampSession = null;
		private final Object messageHandlerValueLock = new Object();
		
		WelcomeHandler(Session session, ResultListener<WampSession> connectionListener){
			this.session = session;
			this.connectionListener = connectionListener;
		}
		
		private boolean setWampSessionConfig(WampSessionConfig sessionConfig){
			synchronized(messageHandlerValueLock){
				try{
					QueueingMessageHandler original = (QueueingMessageHandler)this.messageHandler;
					this.wampSession = sessionConfig.getWampSession();
					this.messageHandler = sessionConfig.getMessageHandler();
					original.transferTo(wampSession, messageHandler);
					return true;
				} catch (ClassCastException e){
					return false;
				}
			}
		}
		
		@Override
		public void onMessage(WampMessage message) {
			if(message.getMessageType() == MessageType.WELCOME){
				log.debug("WelcomeHandler received " + message);
				// we only synchronize on the welcome message - this shouldn't be a significant performance impact
				synchronized(messageHandlerValueLock){
					if (!(messageHandler instanceof QueueingMessageHandler)){
						log.warn("Ignoring additional Welcome message - " + message);
						return;
					}
					
					WampWelcomeMessage welcomeMessage = (WampWelcomeMessage)message;
				
					if(welcomeMessage.getProtocolVersion() != WampBuilder.getProtocolVersion()){
						if (log.isWarnEnabled())
							log.warn("server's Wamp protocol version ('"+welcomeMessage.getProtocolVersion()+"') differs from this implementation version ('" + WampBuilder.getProtocolVersion() +"')\n"
									+"Errors and weird behavior may occurs");
					}
	
					if(log.isTraceEnabled())
						log.trace("Server's Wamp Implementation : " + welcomeMessage.getImplementation());
	
	
					final WampSessionConfig wampSessionConfig = createAndRegisterWampSessionConfig(session, welcomeMessage.getSessionId());
	
					if (!setWampSessionConfig(wampSessionConfig)){
						throw new RuntimeException("Huge problem during welcome message processing - we should not be able to get to here - " + message);
					}
					
					connectionListener.onResult(wampSessionConfig.getWampSession());
				}
				
			} else {
				messageHandler.onMessage(wampSession, message);
			}
		}
	}
	
	/**
	 * 
	 * A message handler that queues any inbound messages.  Users of this class can then transfer the queued messages to a different handler using {@link QueueingMessageHandler#transferTo(WampMessageHandler)
	 *
	 */
	private static class QueueingMessageHandler implements WampMessageHandler{

		private final ConcurrentLinkedQueue<WampMessage> queuedMessages = new ConcurrentLinkedQueue<WampMessage>();
		private final int maxQueueLength;
		private int approximateQueueLength = 0;
		
		public QueueingMessageHandler(int maxQueueLength) {
			this.maxQueueLength = maxQueueLength;
		}
		
		@Override
		public Collection<MessageType> getMessageTypes() {
			return EnumSet.allOf(MessageType.class);
		}

		@Override
		public void onMessage(WampSession session, WampMessage message) {
			if (approximateQueueLength > maxQueueLength){
				log.warn("Dropping message - " + message);
				return;
			}
			approximateQueueLength++;
			queuedMessages.add(message);
		}
		
		public void transferTo(WampSession session, WampMessageHandler target){
			for(WampMessage msg; (msg = queuedMessages.poll()) != null;){
				target.onMessage(session, msg);
			}
		}
	}



}
