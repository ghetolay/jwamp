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
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampCallMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap.TimeoutListener;
import com.github.ghetolay.jwamp.utils.WaitResponse;

public class DefaultRPCSender implements WampRPCSender{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private WampConnection conn;
	
	private TimeoutHashMap<String, ResultListener<WampCallResultMessage>> resultListeners = new TimeoutHashMap<String, ResultListener<WampCallResultMessage>>();
	
	private final TimeoutListener<String, ResultListener<WampCallResultMessage>> myTimeoutListener = new TimeoutListener<String, ResultListener<WampCallResultMessage>>(){
		public void timedOut(String key, ResultListener<WampCallResultMessage> value) {
			// TODO: KD - this feels wrong to me... null is a magic value that has to be specially interpreted.  Should ResultListener also have an onTimeout() method?
			value.onResult(null);
		}
	};

	public DefaultRPCSender(){
	}
	
	public void onConnected(WampConnection connection) {
		conn = connection;
	}
	
	public void onClose(String sessionId, int closeCode) {
		conn = null;
	}
	
	public WampArguments call(String procId, long timeout, Object... args) throws IOException, TimeoutException, SerializationException, CallException{
		if(timeout == 0)
			throw new IllegalArgumentException("Timeout can't be infinite, use #call(String procId, ResultListener<WampCallResultMessage> listener, long timeout, Object... args)");

		if(timeout > 0){
			WaitResponse<WampCallResultMessage> wr = new WaitResponse<WampCallResultMessage>();
			
			call(procId,wr,timeout,args);
			
			WampCallResultMessage result;
			
			try {
				result = wr.call();
			} catch (Exception e) {
				if(log.isErrorEnabled())
					log.error("Error waiting call result : ",e);
				return null;
			}
			
			if(result != null){
				if(result instanceof WampCallErrorMessage)
					throw new CallException((WampCallErrorMessage)result);
				else
					return result.getResults();
			}
				
			throw new TimeoutException();
		}
		
		call(procId,null,-1,args);
		return null;
	}
	
	public String call(String procId, ResultListener<WampCallResultMessage> listener, long timeout, Object... args) throws IOException, SerializationException{
		if (conn == null)
			throw new IllegalStateException("Not connected");
		
		String callId = generateCallId();
		
		OutputWampCallMessage msg = new OutputWampCallMessage();
		msg.setProcId(procId);
		msg.setCallId(callId);
		
		if(args.length > 0){
			if(args.length == 1)
				msg.setArgument(args[0]);
			else
				msg.setArgument(args);
		}
			
		conn.sendMessage(msg);
			
		if(listener != null)
			if(timeout >= 0)
				resultListeners.put(callId, listener, timeout, myTimeoutListener);
			else//weird listener will never be called
				if(log.isWarnEnabled())
					log.warn("ResultListener not null but timeout < 0. ResultListener will never be called.");
			
		return callId;
	}
	
	public boolean onMessage(String sessionId, WampMessage msg) {
		if(msg.getMessageType() == WampMessage.CALLRESULT
			|| msg.getMessageType() == WampMessage.CALLERROR){
				onCallResult((WampCallResultMessage)msg);
				return true;
			}
			
		return false;
	}
	
	private void onCallResult(WampCallResultMessage msg) {
		
		ResultListener<WampCallResultMessage> listener = resultListeners.remove(msg.getCallId());
		if (listener != null){
			listener.onResult(msg);
		} else {
			if (log.isDebugEnabled())
				log.debug("callId from CallResultMessage not recognized : " + msg.toString());
		}

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
