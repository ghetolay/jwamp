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

import java.net.URI;


public class WampCallMessage extends WampMessage{

	protected String callId;
	protected URI procURI;
	
	protected WampArguments args;
	
	protected WampCallMessage(){
		messageType = CALL;
	}
	
	public String getCallId() {
		return callId;
	}

	public URI getProcURI() {
		return procURI;
	}
	
	public WampArguments getArguments(){
		return args;
	}
	
	@Override
	public String toString(){
		return " WampCallMessage { "+ callId+ ", " + procURI + ", " + (args != null ? args.size() : "no") + " arguments} ";
	}
}
