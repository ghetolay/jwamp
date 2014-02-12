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

import com.github.ghetolay.jwamp.WampFactory;

public class WampWelcomeMessage extends WampMessage{

	protected String sessionId;
	protected int protocolVersion;
	protected String implementation;
	
	public WampWelcomeMessage(){
		messageType = WELCOME;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public String getImplementation() {
		return implementation;
	}

	public void setImplementation(String implementation) {
		this.implementation = implementation;
	}
	
	public static WampWelcomeMessage newJWamp(String sessionId){
		WampWelcomeMessage msg = new WampWelcomeMessage();
		msg.setImplementation(WampFactory.getImplementation());
		msg.setProtocolVersion(WampFactory.getProtocolVersion());
		msg.setSessionId(sessionId);
		
		return msg;
	}
	
	@Override
	public String toString(){
		return " WampWelcomeMessage { "+ sessionId + ", "  + protocolVersion + ", "  + implementation + " } ";
	}

}
