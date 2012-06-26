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

	private String callId;
	private Object result;
	
	public WampCallResultMessage(){
		messageType = CALLRESULT;
	}
	
	public WampCallResultMessage(boolean last){
		messageType = last?CALLRESULT:CALLMORERESULT;
	}
	
	public WampCallResultMessage(Object[] JSONArray) throws BadMessageFormException{
		if(JSONArray.length < 3)
			throw BadMessageFormException.notEnoughParameter("CallResult", JSONArray.length, 3);
		
		try{
			messageType = (Integer)JSONArray[0];
			setCallId((String) JSONArray[1]);
			setResult(JSONArray[2]);
			
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public Object[] toJSONArray() {
		return new Object[]{messageType, callId, result};
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public boolean isLast(){
		return messageType != CALLMORERESULT;
	}
}
