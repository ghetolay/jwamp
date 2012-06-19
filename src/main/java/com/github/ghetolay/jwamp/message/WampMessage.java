package com.github.ghetolay.jwamp.message;

public abstract class WampMessage {
	
	public final static int WELCOME     = 0;
	public final static int PREFIX      = 1;
	public final static int CALL        = 2;
	public final static int CALLRESULT  = 3;
	public final static int CALLERROR   = 4;
	public final static int SUBSCRIBE   = 5;
	public final static int UNSUBSCRIBE = 6;
	public final static int PUBLISH     = 7;
	public final static int EVENT       = 8;
	
	public int messageType;
	
	//Both constructor must be override
	public WampMessage(){}
	public WampMessage(Object[] JSONArray) throws BadMessageFormException {}
	
	public abstract Object[] toJSONArray();	
}
