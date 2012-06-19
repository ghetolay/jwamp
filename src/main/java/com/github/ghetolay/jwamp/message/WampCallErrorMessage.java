package com.github.ghetolay.jwamp.message;


public class WampCallErrorMessage extends WampCallResultMessage{

	private String errorUri;
	private String errorDesc;
	private String errorDetails;
	
	
	public WampCallErrorMessage(){
		messageType = CALLERROR;
	}
	
	public WampCallErrorMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 4)
			throw BadMessageFormException.notEnoughParameter("CallError", JSONArray.length, 4);
		
		try{
			
			setCallId((String) JSONArray[1]);
			setErrorUri((String) JSONArray[2]);
			setErrorDesc((String) JSONArray[3]);
			
			if(JSONArray.length > 4)
				setErrorDetails((String) JSONArray[4]);
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public Object[] toJSONArray() {
		if(errorDetails != null)
			return new Object[]{ messageType, getCallId(), errorUri, errorDesc, errorDetails };
		else
			return new Object[]{ messageType, getCallId(), errorUri, errorDesc};
	}

	public String getErrorUri() {
		return errorUri;
	}

	public void setErrorUri(String errorUri) {
		this.errorUri = errorUri;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}

}
