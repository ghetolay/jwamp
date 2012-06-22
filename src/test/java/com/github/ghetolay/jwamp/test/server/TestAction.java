package com.github.ghetolay.jwamp.test.server;

import com.github.ghetolay.jwamp.event.AbstractEventAction;
import com.github.ghetolay.jwamp.rpc.CallAction;

public class TestAction extends AbstractEventAction implements CallAction{

	@Override
	public void subscribe(String sessionId){
		super.subscribe(sessionId);
		
		eventAll("EventAction");
	}
	
	//RPC
	public Object execute(Object[] args) {
		return "SUCCEED";
	}
}
