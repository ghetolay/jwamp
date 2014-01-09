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
package com.github.ghetolay.jwamp.event;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.utils.ActionMapping;


public class ClientEventManager extends AbstractEventManager{
	
	private WampConnection connection;
	
	public ClientEventManager(ActionMapping<EventAction> am){
		super(am);
	}
	
	public void onConnected(WampConnection connection) {
		this.connection = connection;
	}

	@Override
	protected WampConnection getConnection(String sessionId) {
		if(connection.getSessionId().equals(sessionId))
			return connection;
		else
			return null;
	}
	
	@Override
	public void onClose(String sessionId, int closeCode){
		super.onClose(sessionId, closeCode);
		
		connection = null;
	}
}
