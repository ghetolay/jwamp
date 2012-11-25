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



public class WampCallResultMessage extends WampMessage{

	protected String callId;
	protected WampArguments result;
	
	protected WampCallResultMessage(){
		this(true);
	}
	
	protected WampCallResultMessage(boolean last) {
		messageType = last?CALLRESULT:CALLMORERESULT;
	}
	
	public String getCallId() {
		return callId;
	}
	
	public WampArguments getResults(){
		return result;
	}

	public boolean isLast(){
		return messageType != CALLMORERESULT;
	}
	
	@Override
	public String toString(){
		return " WampCallResultMessage { "+ callId+ " , " + result.size() + " results} ";
	}
}
