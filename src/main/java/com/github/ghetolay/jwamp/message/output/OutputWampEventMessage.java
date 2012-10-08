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
package com.github.ghetolay.jwamp.message.output;

import org.msgpack.annotation.Message;

import com.github.ghetolay.jwamp.message.WampEventMessage;

/**
 * @author ghetolay
 *
 */
@Message
public class OutputWampEventMessage extends WampEventMessage{

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	
	public void setEvent(WritableWampArrayObject event){
		this.event = event;
	}
	
	@Override
	public WritableWampArrayObject getEvent(){
		return (WritableWampArrayObject) event;
	}
	
	public void addEventObject(Object obj){
		((WritableWampArrayObject)event).addObject(obj);
	}

}
