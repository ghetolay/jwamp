package com.github.ghetolay.jwamp.rpc;

import java.io.IOException;

import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;


public interface WampRPCSender extends WampMessageHandler{
	
	public WampCallResultMessage call(String procId, Object args, long timeout) throws IOException;
	public String call(String procId, Object args, long timeout, ResultListener<WampCallResultMessage> listener) throws IOException;

}
