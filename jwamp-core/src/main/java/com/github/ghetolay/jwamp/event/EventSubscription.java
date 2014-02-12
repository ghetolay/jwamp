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

import java.net.URI;


/**
 * @author ghetolay
 *
 */
public interface EventSubscription {
	
	//topicId is supposed to be fix, used on equals
	public URI getTopicURI();
	
	public class Impl implements EventSubscription{

		URI topicURI;
		Object args;
		
		public Impl(URI topicURI){
			if(topicURI ==null)
				throw new IllegalArgumentException("TopicURI can't be null or empty");
			
			this.topicURI = topicURI;
		}
		
		public Impl(URI topicURI, Object args){
			this(topicURI);
			this.args = args;
		}
		
		public URI getTopicURI() {
			return topicURI;
		}
	}
}
