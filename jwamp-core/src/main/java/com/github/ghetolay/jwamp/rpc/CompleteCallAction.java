package com.github.ghetolay.jwamp.rpc;

import com.github.ghetolay.jwamp.endpoint.SessionManager;
import com.github.ghetolay.jwamp.message.WampCallMessage;

public interface CompleteCallAction extends Action{
	
	public void setSessionManager(SessionManager sessionManager);
	public void execute(String sessionId, WampCallMessage msg) throws CallException;
}
