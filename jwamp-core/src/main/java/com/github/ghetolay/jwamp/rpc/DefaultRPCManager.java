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

import java.io.IOException;
import java.net.URI;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.endpoint.SessionManager;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampCallErrorMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampCallResultMessage;
import com.github.ghetolay.jwamp.utils.ActionMapping;

/**
 * @author ghetolay
 *
 */
public class DefaultRPCManager implements WampMessageHandler, WampMessageHandler.Call {

	protected final static Logger log = LoggerFactory.getLogger(DefaultRPCManager.class);

	private SessionManager sessionManager;

	private ActionMapping<Action> actions;
	
	public void setActionMapping(ActionMapping<Action> actionMapping){
		this.actions = actionMapping;
	}
	
	public void addAction(URI procURI, CallAction action){
		actions.addAction(procURI, action);
	}
	
	public void addCompleteAction(URI procURI, CompleteCallAction action){
		actions.addAction(procURI, action);
	}
	
	@Override
	public void init(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Override
	public void onOpen(String sessionId) {}
	
	@Override
	public void onClose(String sessionId, CloseReason closeReason) {}

	public void onMessage(String sessionId, WampCallMessage message) {
		Action action = actions.getAction(message.getProcURI());
		if(action != null){
			try{
				if(action instanceof CallAction)
					sendResult(sessionId, message.getCallId(),
							((CallAction)action).execute(message.getArguments()));
				else if(action instanceof CompleteCallAction)
					((CompleteCallAction)action).execute(sessionId, message);
				else
					log.error("CallAction must implements CallAction or CompleteCallAction not Action");
			}catch( CallException e){
				sendError(sessionId, message.getCallId(), e);
			}
		}
	}

	private void sendResult(String sessionId, String callId, Object result){
		if(result != CallAction.Returns.NO_RETURN){
			OutputWampCallResultMessage resultMsg = new OutputWampCallResultMessage();
			resultMsg.setCallId(callId);
			
			if(result != CallAction.Returns.EMPTY)
				resultMsg.setResult(result);
	
			try {
				sessionManager.sendMessageTo(sessionId, resultMsg);
			} catch (IOException | EncodeException e1) {
				if(log.isDebugEnabled())
					log.debug("Unable to send CallResultMessage  : " + resultMsg, e1);
			}
		}
	}

	private void sendError(String sessionId, String callId, CallException e){
		OutputWampCallErrorMessage errorMsg = new OutputWampCallErrorMessage();
		errorMsg.setCallId(callId);
		errorMsg.setErrorUri(e.getErrorURI());
		errorMsg.setErrorDesc(e.getErrorDescription());
		errorMsg.setErrorDetails(e.getErrorDetails());
		
		try {
			sessionManager.sendMessageTo(sessionId, errorMsg);
		} catch (IOException | EncodeException e1) {
			if(log.isDebugEnabled())
				log.debug("Unable to send CallErrorMessage  : " + errorMsg, e1);
		}
		
	}
}


