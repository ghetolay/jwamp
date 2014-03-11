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

import com.github.ghetolay.jwamp.utils.BuiltInURIs;
import com.github.ghetolay.jwamp.utils.ObjectHolder;
import com.github.ghetolay.jwamp.utils.ObjectHolderFactory;


public class WampCallErrorMessage extends WampMessage {

	private final String callId;
	private final URI errorUri;
	private final String errorDesc;
	private final ObjectHolder errorDetails;

	
	public static WampCallErrorMessage create(String callId, URI errorUri, String errorDesc){
		return new WampCallErrorMessage(callId, errorUri, errorDesc, null);
	}
	
	public static WampCallErrorMessage create(String callId, URI errorUri, String errorDesc, ObjectHolder errorDetails){
		return new WampCallErrorMessage(callId, errorUri, errorDesc, errorDetails);
	}
	
	public static WampCallErrorMessage createUnknownCall(WampCallMessage msg){
		return new WampCallErrorMessage(msg.getCallId(), BuiltInURIs.Errors.unknownCall, "No registered handler for procedure URI", ObjectHolderFactory.createForObject(msg.getProcURI()));
	}
	
	private WampCallErrorMessage(String callId, URI errorUri, String errorDesc, ObjectHolder errorDetails){
		super(MessageType.CALLERROR);
		this.callId = callId;
		this.errorUri = errorUri;
		this.errorDesc = errorDesc;
		this.errorDetails = errorDetails;
	}
	
	public String getCallId() {
		return callId;
	}
	
	public URI getErrorUri() {
		return errorUri;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public boolean isErrorDetailsPresent(){
		return errorDetails != null;
	}
	
	public ObjectHolder getErrorDetails() {
		return errorDetails;
	}
	
	@Override
	public String toString(){
		if (errorDetails != null)
			return " WampCallErrorMessage { "+ callId + ", " + errorUri+ ", " + errorDesc + ", " + errorDetails + " } ";
		
		return " WampCallErrorMessage { "+ callId + ", " + errorUri+ ", " + errorDesc + " } ";
	}
}
