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
package com.github.ghetolay.jwamp.rpc;

import java.util.Collection;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.actions.ActionProvider;
import com.github.ghetolay.jwamp.message.MessageType;
import com.github.ghetolay.jwamp.message.MessageSender;
import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampMessageHandler;
import com.github.ghetolay.jwamp.session.WampSession;

/**
 * @author ghetolay
 *
 */
public class RPCMessageHandler implements WampMessageHandler {

	protected final static Logger log = LoggerFactory.getLogger(RPCMessageHandler.class);

	private final MessageSender remoteMessageSender;
	private final ActionProvider<CallAction> actions;
	
	public RPCMessageHandler(MessageSender remoteMessageSender, ActionProvider<CallAction> actions) {
		this.remoteMessageSender = remoteMessageSender;
		this.actions = actions;
	}
	
	@Override
	public Collection<MessageType> getMessageTypes() {
		return EnumSet.of(MessageType.CALL);
	}
	
	@Override
	public void onMessage(WampSession session, WampMessage msg) {
		
		switch(msg.getMessageType()){
		case CALL:
			onMessage(session, (WampCallMessage)msg);
			break;
		default:
			log.warn(this + " received unexpected message " + msg);
			return;
		}
		
	}

	private void onMessage(WampSession session, WampCallMessage message){
		CallAction action = actions.getAction(message.getProcURI());
		if (action == null){
			remoteMessageSender.sendToRemote(WampCallErrorMessage.createUnknownCall(message));
			log.info("No RPC action registered for " + message.getProcURI());
			return;
		}
		
		try{
			WampCallResultMessage callResultMessage = action.handleCall(session, message);
			if (callResultMessage == null)
				throw new IllegalArgumentException("handleCall must return a non-null WampCallResultMessage");
			
			remoteMessageSender.sendToRemote(callResultMessage);
			
		}catch( CallException e){
			remoteMessageSender.sendToRemote(e.createCallErrorMessage(message.getCallId()));
		}
	}
}


