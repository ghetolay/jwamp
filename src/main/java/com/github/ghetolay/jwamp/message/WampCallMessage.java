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

import java.util.Arrays;

public class WampCallMessage extends WampMessage{

	private String callId;
	private String procId;
	private Object[] args;
	
	public WampCallMessage(){
		messageType = CALL;
	}
	
	public WampCallMessage(Object[] JSONArray) throws BadMessageFormException{
		this();
		
		if(JSONArray.length < 3)
			throw BadMessageFormException.notEnoughParameter("Call", JSONArray.length, 3);
		
		try{
			setCallId((String) JSONArray[1]);
			setProcId((String) JSONArray[2]);
			
			if(JSONArray.length > 3)
				setArgs(Arrays.copyOfRange(JSONArray, 3, JSONArray.length));
		} catch(ClassCastException e){
			throw new BadMessageFormException(e);
		}
	}
	
	@Override
	public Object[] toJSONArray() {
		int argsLength = 0;
		if(args != null)
			argsLength = args.length;
		
		Object[] result = new Object[argsLength + 3];
		
		result[0] = messageType;
		result[1] = callId;
		result[2] = procId;
		
		for(int i = argsLength - 1; i >= 0; i--)
			result[i + 3] = args[i];
		
		return result;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String getProcId() {
		return procId;
	}

	public void setProcId(String procId) {
		this.procId = procId;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object args) {
		if(args instanceof Object[])
			this.args = (Object[]) args;
		else
			this.args = new Object[]{ args };
	}

}
