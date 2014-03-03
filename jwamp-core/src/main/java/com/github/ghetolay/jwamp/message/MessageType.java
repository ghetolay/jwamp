/**
 * 
 */
package com.github.ghetolay.jwamp.message;

public enum MessageType{
	WELCOME(0),
	PREFIX(1),
	CALL(2),
	CALLRESULT(3),
	CALLERROR(4),
	SUBSCRIBE(5),
	UNSUBSCRIBE(6),
	PUBLISH(7),
	EVENT(8)
	;
	
	public final int id;
	
	private MessageType(int id){
		this.id = id;
	}
	
	public static MessageType forId(int id){
		if (id >= values().length)
			throw new IllegalArgumentException("MessageType " + id + " unknown");
		return values()[id];
	}
}