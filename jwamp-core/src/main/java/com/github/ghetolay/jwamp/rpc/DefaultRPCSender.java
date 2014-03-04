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
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.message.MessageType;
import com.github.ghetolay.jwamp.message.MessageSender;
import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampMessageHandler;
import com.github.ghetolay.jwamp.session.WampSession;
import com.github.ghetolay.jwamp.utils.JsonBackedObject;
import com.github.ghetolay.jwamp.utils.JsonBackedObjectFactory;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap.TimeoutListener;

public class DefaultRPCSender implements RPCSender, WampMessageHandler{
	
	private static final Logger log = LoggerFactory.getLogger(DefaultRPCSender.class);

	//TODO: this is now set up to where we could share the same instance across all DefaultRPCSender instances - if we do that, we can reintroduce the cleaner thread concept (this would result in a single cleaner thread for the entire application)
	private TimeoutHashMap<CallIdTimeoutKey, CallResultListener> rpcTimeoutManager = new TimeoutHashMap<CallIdTimeoutKey, CallResultListener>();
	
	private final TimeoutListener<CallIdTimeoutKey, CallResultListener> myTimeoutListener = new TimeoutListener<CallIdTimeoutKey, CallResultListener>(){
		public void timedOut(CallIdTimeoutKey key, CallResultListener value) {
			value.onTimeout();
		}
	};

	private final MessageSender remoteMessageSender;
	private final String sessionId;
	private final Random rand = new Random();

	public DefaultRPCSender(TimeoutHashMap<CallIdTimeoutKey, CallResultListener> rpcTimeoutManager, MessageSender remoteMessageSender, String sessionId){
		this.rpcTimeoutManager = rpcTimeoutManager;
		this.remoteMessageSender = remoteMessageSender;
		this.sessionId = sessionId;
	}

	
	public JsonBackedObject callSynchronously(URI procURI, long timeout, Object... args) throws IOException, TimeoutException, EncodeException, CallException, InterruptedException{

		AsyncCallResultBlocker callResultBlocker = new AsyncCallResultBlocker();
		
		callAsynchronously(procURI, timeout, callResultBlocker, args);
		
		return callResultBlocker.getResult(timeout);

	}
	
	private CallIdTimeoutKey getTimeoutKey(String callId){
		return new CallIdTimeoutKey(sessionId, callId);
	}
	
	public String callAsynchronously(URI procURI, long timeout, CallResultListener listener, Object... args) throws IOException, EncodeException{
		if(timeout < 0)
			throw new IllegalArgumentException("Timeout can't be negative");

		String callId = generateCallId();
		
		List<JsonBackedObject> jsonArgs = JsonBackedObjectFactory.createForObjects(args);
		
		WampCallMessage msg = WampCallMessage.create(callId, procURI, jsonArgs);
			
		if(listener != null)
			rpcTimeoutManager.put(getTimeoutKey(callId), listener, timeout, myTimeoutListener);

		remoteMessageSender.sendToRemote(msg);
			
			
		return callId;
	}
	
	@Override
	public Collection<MessageType> getMessageTypes() {
		return EnumSet.of(MessageType.CALLRESULT, MessageType.CALLERROR);
	}
	
	@Override
	public void onMessage(WampSession session, WampMessage inMsg) {
		switch (inMsg.getMessageType()){
			case CALLRESULT:
				onMessage(session, (WampCallResultMessage)inMsg);
				break;
			case CALLERROR:
				onMessage(session, (WampCallErrorMessage)inMsg);
				break;
			default:
				log.warn(this + " received unexpected message " + inMsg);
				return;
		}
		
	}
	
	private void onMessage(WampSession session, WampCallResultMessage msg){
		CallResultListener listener = rpcTimeoutManager.remove(getTimeoutKey(msg.getCallId()));
		if (listener != null){
			listener.onSuccess(msg);
		} else {
			if (log.isDebugEnabled())
				log.debug("callId from CallResultMessage not recognized : " + msg.toString());
		}
	}
	
	private void onMessage(WampSession session, WampCallErrorMessage msg){
		CallResultListener listener = rpcTimeoutManager.remove(getTimeoutKey(msg.getCallId()));
		if (listener != null){
			listener.onError(msg);
		} else {
			if (log.isDebugEnabled())
				log.debug("callId from CallResultMessage not recognized : " + msg.toString());
		}
	}
	
	private static char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
	
	private String generateCallId(){
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < 12; i++){
			sb.append(chars[rand.nextInt(chars.length)]); 
		}
		
		return sb.toString();
	}
	
	private static class AsyncCallResultBlocker implements CallResultListener {

		private WampCallResultMessage resultMessage = null;
		private WampCallErrorMessage errorMessage = null;
		
		public synchronized JsonBackedObject getResult(long timeout) throws TimeoutException, CallException, InterruptedException {
			wait(timeout);
			if (resultMessage != null) return resultMessage.getResult();
			if (errorMessage != null) throw new CallException(errorMessage);
			throw new TimeoutException();
		}

		@Override
		public synchronized void onSuccess(WampCallResultMessage msg) {
			resultMessage = msg;
			notifyAll();
		}

		@Override
		public synchronized void onError(WampCallErrorMessage msg) {
			errorMessage = msg;
			notifyAll();

		}

		@Override
		public synchronized void onTimeout() {
			notifyAll();
		}

		
	}
}
