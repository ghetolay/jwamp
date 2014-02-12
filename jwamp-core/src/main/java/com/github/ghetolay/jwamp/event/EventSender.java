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

import java.net.URI;


public interface EventSender {
	
	/**
	 * 
	 * @param sessionId
	 * @param eventId
	 * @param event
	 * @return true if event was succesfully sent. /!\ return always true on asynchronous call
	 * @see com.github.ghetolay.jwamp.endpoint.WampEndpoint#setAsynchronousEnabled(boolean)
	 */
	public boolean sendEvent(String sessionId, URI eventURI, Object event);
}
