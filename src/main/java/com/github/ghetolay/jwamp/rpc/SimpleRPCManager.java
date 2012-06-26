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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.message.BadMessageFormException;
import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.utils.ActionMapping;

public class SimpleRPCManager implements WampMessageHandler{

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private WampConnection conn;

	private ActionMapping<CallAction> callMapping;
	
	public SimpleRPCManager( ActionMapping<CallAction> callMapping){
		this.callMapping = callMapping;
	}
	
	public void onConnected(WampConnection connection) {
		conn = connection;
	}

	public void onClose(String sessionId, int closeCode) {}
	
	public boolean onMessage(String sessionId, int messageType, Object[] array) throws BadMessageFormException {
		
		if(messageType == WampMessage.CALL){
			processAction(new WampCallMessage(array));
			return true;
		}
	
		return false;
	}
	
	public void processAction(WampCallMessage callMsg){
		WampCallResultMessage resultMsg;
		
		if(log.isDebugEnabled())
			log.debug("Processing Call " + callMsg.getProcId() + " with id " + callMsg.getCallId());

		CallAction action;
		if( (action = callMapping.getAction(callMsg.getProcId())) != null){
			try{
				Object result = action.execute(callMsg.getArgs());
				
				if(result != null && result instanceof MultipleResult){
					MultipleResult r = (MultipleResult)result;
					
					resultMsg = new WampCallResultMessage(r.isLast());
					resultMsg.setResult(r.getResult());	
				}else{
					resultMsg = new WampCallResultMessage();
					resultMsg.setResult(result);	
				}	
	
				resultMsg.setCallId(callMsg.getCallId());
			}catch(Exception e){
				if(log.isDebugEnabled())
					log.debug("Error on action " + callMsg.getCallId(),e);
				
				WampCallErrorMessage errorMsg = new WampCallErrorMessage(callMsg.getCallId(), callMsg.getProcId(),e.getLocalizedMessage());
				if(!e.getMessage().equals(e.getLocalizedMessage()))
					errorMsg.setErrorDetails(e.getMessage());
				
				resultMsg = errorMsg;
			}
		}else{
			if(log.isWarnEnabled())
				log.warn("Call " + callMsg.getProcId() + " not found");
			
			resultMsg = new WampCallErrorMessage(callMsg.getCallId(), callMsg.getProcId(),"Action nor found");
		}

		if(resultMsg != null)
			try {
				conn.sendMessage(resultMsg);
			} catch (IOException e) {
				//TODO log
				if(log.isErrorEnabled())
					log.error("Enable to send message in response of Call " + callMsg.getProcId() + " with id " + callMsg.getCallId());
			}
	}
}
