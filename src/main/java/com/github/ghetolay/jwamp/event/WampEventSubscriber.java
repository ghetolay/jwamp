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
import java.util.List;

import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.event.DefaultEventSubscriber.EventResult;
import com.github.ghetolay.jwamp.utils.ResultListener;

public interface WampEventSubscriber extends WampMessageHandler{
	
	public void subscribe(String topicId) throws IOException;
	public void subscribe(String topicId, ResultListener<Object> resultListener) throws IOException;
	public void unsubscribe(String topicId) throws IOException;
	
	public void publish(String topicId, Object event) throws IOException;
	public void publish(String topicId, Object event, boolean excludeMe) throws IOException;
	public void publish(String topicId, Object event, boolean excludeMe, List<String> eligible) throws IOException;
	public void publish(String topicId, Object event, List<String> exclude, List<String> eligible) throws IOException;
	
	public ResultListener<EventResult> getGlobalListener();
	public void setGlobalListener(ResultListener<EventResult> listener);
	
}
