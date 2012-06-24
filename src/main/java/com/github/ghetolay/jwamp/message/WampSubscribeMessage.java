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
package com.github.ghetolay.jwamp.message;


/**
 * Also UnsubscribeMessage
 * @author ghetolay
 *
 */
public class WampSubscribeMessage extends WampMessage{

	private String topicId;
	
	public WampSubscribeMessage(int msgType, String topicId){
		if(msgType != SUBSCRIBE && msgType != UNSUBSCRIBE)
			throw new IllegalArgumentException("MessageType must be 5 (SUBSCRIBE) or 6 (UNSUBSCRIBE)");
		
		this.messageType = msgType;
		this.topicId = topicId;
	}
	
	public WampSubscribeMessage(Object[] JSONArray) throws BadMessageFormException{
		
		if(JSONArray.length < 2)
			throw BadMessageFormException.notEnoughParameter("Subscribe", JSONArray.length, 2);
		
		try{
			messageType = (Integer) JSONArray[0];
			setTopicId((String) JSONArray[1]);
		
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public Object[] toJSONArray() {
		return new Object[]{messageType,topicId};
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

}
