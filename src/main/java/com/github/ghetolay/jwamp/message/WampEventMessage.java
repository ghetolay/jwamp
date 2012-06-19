package com.github.ghetolay.jwamp.message;

public class WampEventMessage extends WampMessage{

	private String topicId;
	private Object event;
	
	public WampEventMessage(){
		messageType = EVENT;
	}
	
	public WampEventMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 3)
			throw BadMessageFormException.notEnoughParameter("Event", JSONArray.length, 3);
		
		try{
			
			setTopicId((String) JSONArray[1]);
			setEvent(JSONArray[2]);
			
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public Object[] toJSONArray() {
		return new Object[] { messageType, topicId, event};
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	public Object getEvent() {
		return event;
	}

	public void setEvent(Object event) {
		this.event = event;
	}

}
