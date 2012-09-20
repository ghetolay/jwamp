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
import java.util.Arrays;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.message.BadMessageFormException;
import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap.TimeoutListener;
import com.github.ghetolay.jwamp.utils.WaitResponse;

public class DefaultRPCSender implements WampRPCSender, TimeoutListener<String, ResultListener<WampCallResultMessage>>{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private WampConnection conn;
	
	private TimeoutHashMap<String, ResultListener<WampCallResultMessage>> resultListeners = new TimeoutHashMap<String, ResultListener<WampCallResultMessage>>();
	
	//TODO COMMMIT : add timeout listener
	public DefaultRPCSender(){
		resultListeners.addListener(this);
	}
	
	public void onConnected(WampConnection connection) {
		conn = connection;
	}
	
	public void onClose(String sessionId, int closeCode) {}
	
	public WampCallResultMessage call(String procId, long timeout, Object... args) throws IOException{
		if(timeout < 0)
			throw new IllegalArgumentException("Timeout can't be infinite, use #call(String,Object[],int,ResultListener<WampCallResultMessage>)");
		
		WaitResponse<WampCallResultMessage> wr = new WaitResponse<WampCallResultMessage>();
		
		call(procId,timeout,wr,args);
		
		try {
			return wr.call();
		} catch (Exception e) {
			if(log.isErrorEnabled())
				log.error("Error waiting call result : ",e);
			return null;
		}
	}
	
	//TODO possibilité de passer un autre type d'argument/ voir WampCallMessage.setArgument
	public String call(String procId, long timeout, ResultListener<WampCallResultMessage> listener, Object... args) throws IOException{

		String callId = generateCallId();
		
		WampCallMessage msg = new WampCallMessage();
		msg.setProcId(procId);
		msg.setCallId(callId);
		msg.setArguments(Arrays.asList(args));
		
		conn.sendMessage(msg);
			
		if(listener != null)
			resultListeners.put(callId, listener, timeout);
			
		return callId;
	}
	
	public boolean onMessage(String sessionId, WampMessage msg) throws BadMessageFormException {
		
		if(msg.getMessageType() == WampMessage.CALLRESULT
				|| msg.getMessageType() == WampMessage.CALLERROR
				|| msg.getMessageType() == WampMessage.CALLMORERESULT){
			onCallResult((WampCallResultMessage)msg);
			return true;
		}
		
		return false;
	}
	
	private void onCallResult(WampCallResultMessage msg) {
		if(resultListeners.containsKey(msg.getCallId())){
			ResultListener<WampCallResultMessage> listener;
			if(msg.isLast() || msg.getMessageType() == WampMessage.CALLERROR)
				listener = resultListeners.remove(msg.getCallId());
			else
				listener = resultListeners.get(msg.getCallId());
			
			if(listener != null)
				listener.onResult(msg);
		}
		else if(log.isDebugEnabled())
			log.debug("callId from CallResultMessage not recognized : " + msg.toString());
	}	

	public void timedOut(String key, ResultListener<WampCallResultMessage> value) {
		value.onResult(null);
	}
	
	private String generateCallId(){
		Random rand = new Random();
		String id ="";
		
		for(int i = 0; i < 12; i++){
			int r = rand.nextInt(62);
			if(r > 35)
				id += (char)(r+61);
			else if(r > 9)
				id += (char)(r+55);
			else
				id += r; 
		}
		
		return id;
	}
}
