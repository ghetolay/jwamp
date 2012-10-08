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
package com.github.ghetolay.jwamp.test.server;

import com.github.ghetolay.jwamp.event.SimpleEventAction;
import com.github.ghetolay.jwamp.message.ReadableWampArrayObject;
import com.github.ghetolay.jwamp.message.output.WritableWampArrayObject;
import com.github.ghetolay.jwamp.rpc.CallAction;

public class TestAction extends SimpleEventAction implements CallAction{

	@Override
	public void subscribe(String sessionId, ReadableWampArrayObject args){
		super.subscribe(sessionId, args);

		eventAll(WritableWampArrayObject.withFirstObject("EventAction"));
	}
	
	//RPC
	public WritableWampArrayObject execute(String sessionId, ReadableWampArrayObject args) {
		return null;
	}
	
}
