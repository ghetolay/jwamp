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

import com.github.ghetolay.jwamp.WampBuilder;

public class WampWelcomeMessage extends WampMessage{

	private final String sessionId;
	private final int protocolVersion;
	private final String implementation;
	
	public static WampWelcomeMessage create(String sessionId, int protocolVersion, String implementation){
		return new WampWelcomeMessage(sessionId, protocolVersion, implementation);
	}
	
	public static WampWelcomeMessage create(String sessionId){
		return new WampWelcomeMessage(
				sessionId,
				WampBuilder.getProtocolVersion(),
				WampBuilder.getImplementation()
				);
	}
	
	protected WampWelcomeMessage(String sessionId, int protocolVersion, String implementation){
		super(MessageType.WELCOME);
		this.sessionId = sessionId;
		this.protocolVersion = protocolVersion;
		this.implementation = implementation;
	}

	public String getSessionId() {
		return sessionId;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public String getImplementation() {
		return implementation;
	}


	
	@Override
	public String toString(){
		return " WampWelcomeMessage { "+ sessionId + ", "  + protocolVersion + ", "  + implementation + " } ";
	}

}
