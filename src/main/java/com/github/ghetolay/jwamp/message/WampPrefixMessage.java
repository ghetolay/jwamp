package com.github.ghetolay.jwamp.message;

public class WampPrefixMessage extends WampMessage{

	private String prefix;
	private String uri;
	
	public WampPrefixMessage(){
		messageType = PREFIX;
	}
	
	public WampPrefixMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 3)
			throw BadMessageFormException.notEnoughParameter("Prefix", JSONArray.length, 3);
		
		try{
			setPrefix((String) JSONArray[1]);
			setUri((String) JSONArray[2]);
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public Object[] toJSONArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
