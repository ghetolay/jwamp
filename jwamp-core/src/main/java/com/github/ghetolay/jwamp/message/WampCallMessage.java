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
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ghetolay.jwamp.utils.JsonBackedObject;


public class WampCallMessage extends WampMessage{

	private final String callId;
	private final URI procURI;
	private final List<JsonBackedObject> args;
	
	public static WampCallMessage create(String callId, URI procURI, List<JsonBackedObject> args){
		return new WampCallMessage(callId, procURI, args);
	}
	
	private WampCallMessage(String callId, URI procURI, List<JsonBackedObject> args){
		super(MessageType.CALL);
		this.callId = callId;
		this.procURI = procURI;
		this.args = Collections.unmodifiableList(args);
	}
	
	public String getCallId() {
		return callId;
	}

	public URI getProcURI() {
		return procURI;
	}
	
	public List<JsonBackedObject> getArgs() {
		return args;
	}
	
	@Override
	public String toString(){
		return " WampCallMessage { "+ callId+ ", " + procURI + ", " + (args != null ? args.size() : "no") + " arguments} ";
	}
}
