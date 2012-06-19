package com.github.ghetolay.jwamp.message;

public class WampPublishMessage extends WampMessage{

	private String topicId;
	private Object event;
	private String[] exclude;
	private String[] eligible;
	private boolean excludeMe;
	
	public WampPublishMessage(){
		messageType = PUBLISH;
	}
	
	public WampPublishMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 3)
			throw BadMessageFormException.notEnoughParameter("Publish", JSONArray.length, 3);
		
		try{
			
			setTopicUri((String) JSONArray[1]);
			setEvent(JSONArray[2]);
			
			if(JSONArray.length > 4)
				if(JSONArray[4] instanceof Boolean)
					setExcludeMe((Boolean) JSONArray[4]);
				else
					setExclude((String[]) JSONArray[4]);
			
			if(JSONArray.length > 5)
				setEligible((String[]) JSONArray[5]);
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public Object[] toJSONArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicUri(String topicId) {
		this.topicId = topicId;
	}

	public Object getEvent() {
		return event;
	}

	public void setEvent(Object event) {
		this.event = event;
	}

	public String[] getExclude() {
		return exclude;
	}

	public void setExclude(String[] exclude) {
		this.exclude = exclude;
	}

	public String[] getEligible() {
		return eligible;
	}

	public void setEligible(String[] eligible) {
		this.eligible = eligible;
	}

	public boolean isExcludeMe() {
		return excludeMe;
	}

	public void setExcludeMe(boolean excludeMe) {
		this.excludeMe = excludeMe;
	}

}
