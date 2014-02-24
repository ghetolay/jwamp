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
import java.util.Random;
import java.util.concurrent.TimeoutException;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.endpoint.SessionManager;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampCallMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap.TimeoutListener;
import com.github.ghetolay.jwamp.utils.WaitResponse;

public class DefaultRPCSender implements WampRPCSender{
	
	protected static final Logger log = LoggerFactory.getLogger(DefaultRPCSender.class);
	
	private SessionManager sessionManager;
	private String sessionId;
	
	private TimeoutHashMap<String, ResultListener<WampCallResultMessage>> resultListeners = new TimeoutHashMap<String, ResultListener<WampCallResultMessage>>();
	
	private final TimeoutListener<String, ResultListener<WampCallResultMessage>> myTimeoutListener = new TimeoutListener<String, ResultListener<WampCallResultMessage>>(){
		public void timedOut(String key, ResultListener<WampCallResultMessage> value) {
			// TODO: KD - this feels wrong to me... null is a magic value that has to be specially interpreted.  Should ResultListener also have an onTimeout() method?
			value.onResult(null);
		}
	};

	public DefaultRPCSender(){
	}
	
	@Override
	public void init(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Override
	public void onOpen(String sessionId) {
		this.sessionId = sessionId;
	}
	
<<<<<<< HEAD
	public void onClose(String sessionId, CloseReason closeReason) {}
=======
	public void onClose(String sessionId, int closeCode) {
		conn = null;
	}
>>>>>>> refs/heads/timeouthashmap-redesign
	
	public WampArguments call(URI procURI, long timeout, Object... args) throws IOException, TimeoutException, EncodeException, CallException{
		if(timeout == 0)
			throw new IllegalArgumentException("Timeout can't be infinite, use #call(String procId, ResultListener<WampCallResultMessage> listener, long timeout, Object... args)");

		if(timeout > 0){
			WaitResponse<WampCallResultMessage> wr = new WaitResponse<WampCallResultMessage>();
			
			call(procURI,wr,timeout,args);
			
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
		
		call(procURI,null,-1,args);
		return null;
	}
	
<<<<<<< HEAD
	public String call(URI procURI, ResultListener<WampCallResultMessage> listener, long timeout, Object... args) throws IOException, EncodeException{
=======
	public String call(String procId, ResultListener<WampCallResultMessage> listener, long timeout, Object... args) throws IOException, SerializationException{
		if (conn == null)
			throw new IllegalStateException("Not connected");
		
>>>>>>> refs/heads/timeouthashmap-redesign
		String callId = generateCallId();
		
		OutputWampCallMessage msg = new OutputWampCallMessage();
		msg.setProcURI(procURI);
		msg.setCallId(callId);
		
		if(args.length > 0){
			if(args.length == 1)
				msg.setArgument(args[0]);
			else
				msg.setArgument(args);
		}
			
		sessionManager.sendMessageTo(sessionId, msg);
			
		if(listener != null)
			if(timeout >= 0)
				resultListeners.put(callId, listener, timeout, myTimeoutListener);
			else//weird listener will never be called
				if(log.isWarnEnabled())
					log.warn("ResultListener not null but timeout < 0. ResultListener will never be called.");
			
		return callId;
	}
	
	public void onMessage(String sessionId, WampCallErrorMessage msg) {
		onMessage(sessionId, (WampCallResultMessage)msg);
	}
	
<<<<<<< HEAD
	public void onMessage(String sessionId, WampCallResultMessage msg) {
		if(resultListeners.containsKey(msg.getCallId())){
			ResultListener<WampCallResultMessage> listener = resultListeners.get(msg.getCallId());
			
			if(listener != null)
				listener.onResult(msg);
=======
	private void onCallResult(WampCallResultMessage msg) {
		
		ResultListener<WampCallResultMessage> listener = resultListeners.remove(msg.getCallId());
		if (listener != null){
			listener.onResult(msg);
		} else {
			if (log.isDebugEnabled())
				log.debug("callId from CallResultMessage not recognized : " + msg.toString());
>>>>>>> refs/heads/timeouthashmap-redesign
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
