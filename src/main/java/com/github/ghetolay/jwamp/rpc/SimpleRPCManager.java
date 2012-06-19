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

			resultMsg = new WampCallResultMessage();
			resultMsg.setCallId(callMsg.getCallId());

			resultMsg.setResult(action.execute(callMsg.getArgs()));	
		}else{
			if(log.isWarnEnabled())
				log.warn("Call " + callMsg.getProcId() + " not found");
			
			WampCallErrorMessage errorMsg = new WampCallErrorMessage();
			errorMsg.setCallId(callMsg.getCallId());
			errorMsg.setErrorUri(callMsg.getProcId());
			errorMsg.setErrorDesc("Action not found");
			
			resultMsg = errorMsg;
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
