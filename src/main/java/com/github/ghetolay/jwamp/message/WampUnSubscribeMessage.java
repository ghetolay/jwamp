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

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;


/**
 * Also UnsubscribeMessage
 * @author ghetolay
 *
 */
public class WampUnSubscribeMessage extends WampMessage{

	private String topicId;
	
	protected WampUnSubscribeMessage(int messageType){
		this.messageType = messageType;
	}
	
	protected WampUnSubscribeMessage(int messageType, String topicId){
		this(messageType);
		this.topicId = topicId;
	}
	
	public WampUnSubscribeMessage(String topicId){
		this(WampMessage.UNSUBSCRIBE, topicId);
	}
	
	public WampUnSubscribeMessage(Object[] JSONArray) throws BadMessageFormException{
		
		if(JSONArray.length < 2)
			throw BadMessageFormException.notEnoughParameter("Subscribe", JSONArray.length, 2);
		
		try{
			messageType = (Integer) JSONArray[0];
			setTopicId((String) JSONArray[1]);
		
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	
	
	public WampUnSubscribeMessage(JsonParser parser) throws BadMessageFormException{
		this(WampMessage.UNSUBSCRIBE,parser);
	}
	
	protected WampUnSubscribeMessage(int messageType, JsonParser parser) throws BadMessageFormException{
		this(messageType);
		
		try {
			if(parser.nextToken() != JsonToken.VALUE_STRING)
				throw new BadMessageFormException("TopicUri is required and must be a string");
			setTopicId(parser.getText());
			
		} catch (JsonParseException e) {
			throw new BadMessageFormException(e);
		} catch (IOException e) {
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
