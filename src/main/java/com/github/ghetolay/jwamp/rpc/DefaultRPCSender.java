package com.github.ghetolay.jwamp.rpc;


import java.io.IOException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.message.BadMessageFormException;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap;
import com.github.ghetolay.jwamp.utils.WaitResponse;

public class DefaultRPCSender implements WampRPCSender{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private WampConnection conn;
	
	private TimeoutResultListenerMap<String, WampCallResultMessage> resultListeners = new TimeoutResultListenerMap<String, WampCallResultMessage>();
	
	public void onConnected(WampConnection connection) {
		conn = connection;
	}
	
	public void onClose(String sessionId, int closeCode) {}
	
	public WampCallResultMessage call(String procId, Object args, long timeout) throws IOException{
		if(timeout < 0)
			throw new IllegalArgumentException("Timeout can't be infinite, use #call(String,Object[],int,ResultListener<WampCallResultMessage>)");
		
		WaitResponse<WampCallResultMessage> wr = new WaitResponse<WampCallResultMessage>();
		
		call(procId,args,timeout,wr);
		
		try {
			return wr.call();
		} catch (Exception e) {
			if(log.isErrorEnabled())
				log.error("Error waiting call result : ",e);
			return null;
		}
	}
	
	public String call(String procId, Object args, long timeout, ResultListener<WampCallResultMessage> listener) throws IOException{

		String callId = generateCallId();
		
		WampCallMessage msg = new WampCallMessage();
		msg.setProcId(procId);
		msg.setCallId(callId);
		msg.setArgs(args);
		
		conn.sendMessage(msg);
			
		if(listener != null)
			resultListeners.put(callId, listener, timeout);
			
		return callId;
	}
	
	public boolean onMessage(String sessionId, int messageType, Object[] array) throws BadMessageFormException {
		
		switch(messageType){
			case WampMessage.CALLRESULT:
			case WampMessage.CALLERROR :
				onCallResult(new WampCallResultMessage(array));
				break;
			default: return false;
		}
		
		return true;
	}
	
	public void onCallResult(WampCallResultMessage msg) {
		if(resultListeners.containsKey(msg.getCallId()))
			//The Map is designed to call the listener on remove
			resultListeners.remove(msg.getCallId(),msg);
		else if(log.isDebugEnabled())
			log.debug("callId from CallResultMessage not recognized : " + msg.toString());
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
	
	private class TimeoutResultListenerMap<K,T> extends TimeoutHashMap<K, ResultListener<T>>{

		private static final long serialVersionUID = -6289873124717928971L;

		@Override
		protected ResultListener<T> remove(Object key, boolean removeFromSet){
			ResultListener<T> result = super.remove(key,removeFromSet);
			
			if(result != null)
				result.onResult(null);
			
			return result;
		}
		
		protected void remove(Object key, T result){
			ResultListener<T> r = super.remove(key,true);
			if(r != null)
				r.onResult(result);
		}
	}
}
