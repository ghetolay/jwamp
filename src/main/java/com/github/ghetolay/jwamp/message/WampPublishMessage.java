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

import java.util.List;

public class WampPublishMessage extends WampMessage{

	protected String topicId;
	protected Object event;
	protected List<String> exclude;
	protected List<String> eligible;
	protected boolean excludeMe;
	
	protected WampPublishMessage(){
		messageType = PUBLISH;
	}
	
	public String getTopicId() {
		return topicId;
	}

	public Object getEvent() {
		return event;
	}

	public List<String> getExclude() {
		return exclude;
	}

	public List<String> getEligible() {
		return eligible;
	}

	public boolean isExcludeMe() {
		return excludeMe;
	}
	
	@Override
	public String toString(){
		return " WampPublishMessage { "+ excludeMe + " , " + event + " , exclude : " + exclude + " , eligible : " + eligible + " } ";
	}
}
