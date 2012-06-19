package com.github.ghetolay.jwamp.message;

import java.util.Arrays;

public class WampCallResultMessage extends WampMessage{

	private String callId;
	private Object result;
	
	public WampCallResultMessage(){
		messageType = CALLRESULT;
	}
	
	public WampCallResultMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 3)
			throw BadMessageFormException.notEnoughParameter("CallResult", JSONArray.length, 3);
		
		try{
			
			setCallId((String) JSONArray[1]);
			setResult(Arrays.copyOfRange(JSONArray, 2, JSONArray.length));
			
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public Object[] toJSONArray() {
		if(result != null){
			return new Object[]{messageType, callId, result};
		}
		else 
			return new Object[]{messageType, callId};
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

}
