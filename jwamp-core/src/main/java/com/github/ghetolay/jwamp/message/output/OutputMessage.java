package com.github.ghetolay.jwamp.message.output;


public class OutputMessage {
	
	public static OutputWampCallMessage Call(){
		return new OutputWampCallMessage();
	}
	
	public static OutputWampCallResultMessage CallResult(){
		return new OutputWampCallResultMessage();
	}
	
	public static OutputWampCallErrorMessage CallError(){
		return new OutputWampCallErrorMessage();
	}
	
	public static OutputWampSubscribeMessage Subscribe(){
		return new OutputWampSubscribeMessage();
	}
	
	public static OutputWampUnsubscribeMessage Unsubscribe(){
		return new OutputWampUnsubscribeMessage();
	}
	
	public static OutputWampEventMessage Event(){
		return new OutputWampEventMessage();
	}
	
	public static OutputWampPublishMessage Publish(){
		return new OutputWampPublishMessage();
	}
}
