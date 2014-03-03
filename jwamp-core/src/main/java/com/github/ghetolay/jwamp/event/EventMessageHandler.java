/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import java.util.Collection;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.actions.ActionProvider;
import com.github.ghetolay.jwamp.message.MessageType;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampMessageHandler;
import com.github.ghetolay.jwamp.session.WampSession;

/**
 * @author Kevin
 *
 */
public class EventMessageHandler implements WampMessageHandler {

	private final static Logger log = LoggerFactory.getLogger(WampMessageHandler.class);

	private final ActionProvider<EventAction> actionProvider;
	
	public EventMessageHandler(ActionProvider<EventAction> actionProvider) {
		this.actionProvider = actionProvider;
	}

	@Override
	public Collection<MessageType> getMessageTypes() {
		return EnumSet.of(MessageType.EVENT);
	}


	@Override
	public void onMessage(WampSession session, WampMessage msg) {
		switch(msg.getMessageType()){
			case EVENT:
				onMessage(session, (WampEventMessage)msg);
				break;
			default:
				log.warn(this + " received unexpected message " + msg);
				return;
		}
	}

	private void onMessage(WampSession session, WampEventMessage message){
		EventAction action = actionProvider.getAction(message.getTopicURI());
		if (action == null){
			log.info("No event action registered for " + message.getTopicURI());
			return;
		}
		
		action.handleEvent(session, message);
	}
}
