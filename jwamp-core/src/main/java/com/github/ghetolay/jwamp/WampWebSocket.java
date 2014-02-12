/**
*Copyright [2012] [Ghetolay]
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package com.github.ghetolay.jwamp;


import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;

import com.github.ghetolay.jwamp.endpoint.WampDispatcher;
import com.github.ghetolay.jwamp.event.WampEventSubscriber;
import com.github.ghetolay.jwamp.event.EventSubscription;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.rpc.CallException;
import com.github.ghetolay.jwamp.rpc.WampRPCSender;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class WampWebSocket {

	private WampDispatcher dispatcher;
	private String sessionId;
	
	private WampRPCSender rpcSender;
	private WampEventSubscriber eventSubscriber;
	
	public WampWebSocket(WampDispatcher dispatcher, String sessionId) {
		this.dispatcher = dispatcher;
	}
	
	private WampRPCSender getRPCSender() throws UnsupportedWampActionException{
		//Refresh in case it was removed from the connection
		if(rpcSender == null || dispatcher.containsMessageHandler(rpcSender)){
			rpcSender = dispatcher.getMessageHandler(WampRPCSender.class);
			if(rpcSender == null)
				throw new UnsupportedWampActionException();
		}
		return rpcSender;
	}
	
	private WampEventSubscriber getEventSubscriber() throws UnsupportedWampActionException{
		//Refresh in case it was removed from the connection
		if(eventSubscriber == null || dispatcher.containsMessageHandler(eventSubscriber)){
			eventSubscriber = dispatcher.getMessageHandler(WampEventSubscriber.class);
			if(eventSubscriber == null)
				throw new UnsupportedWampActionException();
		}
		return eventSubscriber;
	}
	
	public void close(CloseReason closeReason) throws IOException{
		dispatcher.getSessionManager().getWampSession(sessionId).getSession().close(closeReason);
	}
	
	public WampArguments simpleCall(URI procURI, Object... args) throws IOException, TimeoutException, UnsupportedWampActionException, EncodeException, CallException{
		return getRPCSender().call(procURI, 5000, args);
	}
	
	//TODO issue this method can be miss-call
	public WampArguments call(URI procURI, long timeout, Object... args) throws IOException, TimeoutException, UnsupportedWampActionException, EncodeException, CallException{
		return getRPCSender().call(procURI, timeout, args);
	}
	
	public String call(URI procURI, ResultListener<WampCallResultMessage> listener, long timeout, Object... args) throws IOException, UnsupportedWampActionException, EncodeException, CallException{
		return getRPCSender().call(procURI, listener, timeout, args);
	}
	
	public WampArguments call(URI procURI, long timeout, Object args) throws IOException, TimeoutException, UnsupportedWampActionException, EncodeException, CallException{
		return getRPCSender().call(procURI, timeout, args);
	}
	
	public String call(URI procURI, ResultListener<WampCallResultMessage> listener, long timeout, Object args) throws IOException, UnsupportedWampActionException, EncodeException, CallException{
		return getRPCSender().call(procURI, listener, timeout, args);
	}
	
	public void subscribe(URI topicURI) throws IOException, EncodeException, UnsupportedWampActionException{
		getEventSubscriber().subscribe(topicURI);
	}
	
	public void subscribe(URI topicURI, ResultListener<WampArguments> listener) throws IOException, EncodeException, UnsupportedWampActionException{
		getEventSubscriber().subscribe(topicURI, listener);
	}
	
	public void subscribe(EventSubscription subscription) throws IOException, UnsupportedWampActionException, EncodeException{
		getEventSubscriber().subscribe(subscription);
	}
	
	public void subscribe(EventSubscription subscription, ResultListener<WampArguments> listener) throws IOException, UnsupportedWampActionException, EncodeException{
		getEventSubscriber().subscribe(subscription, listener);
	}
	
	public void unsubscribe(URI topicURI) throws IOException, UnsupportedWampActionException, EncodeException{
		getEventSubscriber().unsubscribe(topicURI);
	}
	
	public void unsubscribeAll() throws IOException, UnsupportedWampActionException{
		getEventSubscriber().unsubscribeAll();
	}
	
	public void publish(URI topicURI, Object event) throws IOException, UnsupportedWampActionException, EncodeException{
		getEventSubscriber().publish(topicURI, event);
	}
	public void publish(URI topicURI, Object event, boolean excludeMe) throws IOException, UnsupportedWampActionException, EncodeException{
		getEventSubscriber().publish(topicURI, event, excludeMe);
	}
	public void publish(URI topicURI, Object event, boolean excludeMe, List<String> eligible) throws IOException, UnsupportedWampActionException, EncodeException{
		getEventSubscriber().publish(topicURI, event, excludeMe, eligible);
	}
	public void publish(URI topicURI, Object event, List<String> exclude, List<String> eligible) throws IOException, UnsupportedWampActionException, EncodeException{
		getEventSubscriber().publish(topicURI, event, exclude, eligible);
	}
	
	/*
	public void setGlobalEventListener(ResultListener<EventResult> listener){
		getEventSubscriber().setGlobalListener(listener);
	}
	*/
	
}
