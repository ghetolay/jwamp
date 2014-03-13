/**
 * 
 */
package com.github.ghetolay.jwamp.rpc;

import com.github.ghetolay.jwamp.utils.TimeoutHashMap;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap.TimeoutListener;

/**
 * @author Kevin
 *
 */
public class RPCTimeoutManager {
	private final TimeoutHashMap<CallIdTimeoutKey, CallResultListener> timeoutMap = new TimeoutHashMap<CallIdTimeoutKey, CallResultListener>();

	/**
	 * 
	 */
	public RPCTimeoutManager() {
	}

	public void registerCall(String sessionId, String callId, CallResultListener callResultListener, long timeout, TimeoutListener<CallIdTimeoutKey, CallResultListener> timeoutListener){
		timeoutMap.put(createTimeoutKey(sessionId, callId), callResultListener, timeout, timeoutListener);
	}
	
	public CallResultListener remove(String sessionId, String callId){
		return timeoutMap.remove(createTimeoutKey(sessionId, callId));
	}
	
	private static CallIdTimeoutKey createTimeoutKey(String sessionId, String callId){
		return new CallIdTimeoutKey(sessionId, callId);
	}

}
