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
package com.github.ghetolay.jwamp.event;


import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.websocket.EncodeException;

import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.utils.ResultListener;

public interface WampEventSubscriber extends WampMessageHandler, WampMessageHandler.Event{
	
	public void subscribe(URI topicURI) throws IOException, EncodeException;
	public void subscribe(URI topicURI, ResultListener<WampArguments> eventListener) throws IOException, EncodeException;
	public void subscribe(EventSubscription subscription) throws IOException, EncodeException;
	public void subscribe(EventSubscription subscription, ResultListener<WampArguments> eventListener) throws IOException, EncodeException;
	public void unsubscribe(URI topicURI) throws IOException;
	public void unsubscribeAll() throws IOException;
	
	public void publish(URI topicURI, Object event) throws IOException, EncodeException;
	public void publish(URI topicURI, Object event, boolean excludeMe) throws IOException, EncodeException;
	public void publish(URI topicURI, Object event, boolean excludeMe, List<String> eligible) throws IOException, EncodeException;
	public void publish(URI topicURI, Object event, List<String> exclude, List<String> eligible) throws IOException, EncodeException;
	
	//public ResultListener<EventResult> getGlobalListener();
	//public void setGlobalListener(ResultListener<EventResult> listener);
	
}
