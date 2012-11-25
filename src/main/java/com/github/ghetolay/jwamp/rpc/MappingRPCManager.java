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
package com.github.ghetolay.jwamp.rpc;


import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.utils.ActionMapping;

public class MappingRPCManager extends AbstractRPCManager{

	private ActionMapping<CallAction> callMapping;
	
	public MappingRPCManager( ActionMapping<CallAction> callMapping){
		this.callMapping = callMapping;
	}
	
	@Override
	protected RunnableAction getRunnableAction(String sessionId, WampCallMessage message){
		
		if(log.isDebugEnabled())
			log.debug("Processing Call " + message.getProcId() + " with id " + message.getCallId());

		CallAction action;
		if( (action = callMapping.getAction(message.getProcId())) != null)
			return new RunnableCallAction(sessionId, message, action);
		
		return null;
	}

	private class RunnableCallAction extends RunnableAction{
		
		public CallAction action;
		
		public RunnableCallAction(String sessionId, WampCallMessage message, CallAction action) {
			super(sessionId, message);
			this.action = action;
		}

		protected void excuteAction(String sessionID, WampArguments args, CallResultSender sender) throws Exception {
			action.execute(sessionID, args, sender);
		}		
	}

}
