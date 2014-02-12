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

import javax.websocket.CloseReason;

import com.github.ghetolay.jwamp.endpoint.SessionManager;
import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampPrefixMessage;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.message.WampUnsubscribeMessage;
import com.github.ghetolay.jwamp.message.WampWelcomeMessage;

public interface WampMessageHandler{
	
	public void init(SessionManager sessionManager);
	
	public void onOpen(String sessionId);
	
	public void onClose(String sessionId, CloseReason closeReason);
	
	public interface Welcome{
		public void onMessage(String sessionId, WampWelcomeMessage message);
	}
	
	public interface Prefix{
		public void onMessage(String sessionId, WampPrefixMessage message);
	}
	
	public interface Call{
		public void onMessage(String sessionId, WampCallMessage message);
	}
	
	public interface CallResult{
		public void onMessage(String sessionId, WampCallResultMessage message);
	}
	
	public interface CallError{
		public void onMessage(String sessionId, WampCallErrorMessage message);
	}
	
	public interface Subscribe{
		public void onMessage(String sessionId, WampSubscribeMessage message);
	}
	
	public interface Unsubscribe{
		public void onMessage(String sessionId, WampUnsubscribeMessage message);
	}
	
	public interface Publish{
		public void onMessage(String sessionId, WampPublishMessage message);
	}
	
	public interface Event{
		public void onMessage(String sessionId, WampEventMessage message);
	}
}
