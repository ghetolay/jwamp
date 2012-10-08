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

import java.util.ArrayList;
import java.util.List;

import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.output.WritableWampArrayObject;

/**
 * @author ghetolay
 *
 */
public class TestDefinedAction {

	
	public WritableWampArrayObject doEcho(String sessionId, WampCallMessage msg){
		return new WritableWampArrayObject(msg.getArguments());
	}
	
	public WritableWampArrayObject doOneList(String sessionId, WampCallMessage msg){
		List<String> list = new ArrayList<String>();
		
		list.add("lol");
		list.add("prout");
		list.add("youk");
		
		return new WritableWampArrayObject(list);
	}
	
	public WritableWampArrayObject doSingleReturn(String sessionId, WampCallMessage msg){
		return WritableWampArrayObject.withFirstObject(1);
	}
}
