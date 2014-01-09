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

import java.util.List;
import java.util.Set;

import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampEventMessage;

public interface EventAction {
	
	public void setEventId(String id);
	public void setEventSender(EventSender eventSender);
	
	public void subscribe(String sessionId);
	public void unsubscribe(String sessionId);

	public Set<String> getSubscriber();
	
	//TODO made in a urge. let's think about it a bit more :)
	/**
	 * Method called before sending an event after a publish message.
	 * It permits filtering subscribers and editing eventMessage.
	 * 
	 * @param subscribers Current list of subscribers
	 * @param publishMessage Original publish message
	 * @param eventMessage Current event message, can be modified
	 * @return The final list of subscribers event message will be sent to
	 */
	public List<String> publishTo(List<String> subscribers, WampPublishMessage publishMessage, OutputWampEventMessage eventMessage);
}
