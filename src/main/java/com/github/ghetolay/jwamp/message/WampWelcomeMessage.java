package com.github.ghetolay.jwamp.message;

public class WampWelcomeMessage extends WampMessage{

	private String sessionId;
	private int protocolVersion;
	private String implementation;
	
	public WampWelcomeMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 4)
			throw BadMessageFormException.notEnoughParameter("Welcome", JSONArray.length, 4);
		
		try{
			setSessionId((String) JSONArray[1]);
			setProtocolVersion((Integer) JSONArray[2]);
			setImplementation((String) JSONArray[3]);
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	public WampWelcomeMessage(){
		messageType = WELCOME;
	}
	
	@Override
	public Object[] toJSONArray() {
		return new Object[]{ messageType, sessionId, protocolVersion, implementation};
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public String getImplementation() {
		return implementation;
	}

	public void setImplementation(String implementation) {
		this.implementation = implementation;
	}

}
