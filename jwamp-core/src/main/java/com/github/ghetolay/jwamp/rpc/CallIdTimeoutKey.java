/**
 * 
 */
package com.github.ghetolay.jwamp.rpc;


/**
 * @author Kevin
 *
 */
public class CallIdTimeoutKey {

	private final String sessionId;
	private final String callId;
	private final int hash;
	
	/**
	 * 
	 */
	public CallIdTimeoutKey(String sessionId, String callId) {
		this.sessionId = sessionId;
		this.callId = callId;
		this.hash = computeHash();
	}

	@Override
	public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof CallIdTimeoutKey))
            return false;
		
        CallIdTimeoutKey other = (CallIdTimeoutKey)o;
        
        if (hash != other.hash) // quick check
        	return false;
        
        if (!sessionId.equals(other.sessionId))
        	return false;
        
        return callId.equals(other.callId);
	}
	
	private int computeHash(){
        int hashCode = 1;
        
        hashCode = 31*hashCode + (sessionId.hashCode());
        hashCode = 31*hashCode + (callId.hashCode());
        
        return hashCode;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
}
