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


/**
 * @author ghetolay
 *
 */
public interface WampSubscription {
	
	//topicId is supposed to be fix, used on equals
	public String getTopicId();
	
	//can change over time, if we re-subscribe.
	public Object getSubscribeArguments();
	
	public class Impl implements WampSubscription{

		String topicId;
		Object args;
		
		public Impl(String topicId){
			if(topicId ==null || topicId.isEmpty())
				throw new IllegalArgumentException("TopicId can't be null or empty");
			
			this.topicId = topicId;
		}
		
		public Impl(String topicId, Object args){
			this(topicId);
			this.args = args;
		}
		
		public String getTopicId() {
			return topicId;
		}

		public Object getSubscribeArguments() {
			return args;
		}
	}
}
