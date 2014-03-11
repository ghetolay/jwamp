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

import java.net.URI;

import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.utils.ObjectHolder;
import com.github.ghetolay.jwamp.utils.ObjectHolderFactory;
import com.github.ghetolay.jwamp.utils.URIBuilder;

/**
 * @author ghetolay
 *
 */
public class CallException extends Throwable {

	private static final long serialVersionUID = -6836214026673216476L;

	public static final URI generic = URIBuilder.newURI("ws://wamp.ws/error#generic");
	
	private URI errorURI;
	private Object details;
	
	public CallException(URI errorURI, String errorDesc) {
		super(errorDesc);
	}
	
	public CallException(URI errorURI, String errorDesc, Object errorDetails){
		super(errorDesc);
		details = errorDetails;
	}
	
	public CallException(WampCallErrorMessage msg){
		this(msg.getErrorUri(), msg.getErrorDesc());
		if (msg.isErrorDetailsPresent())
			details = msg.getErrorDetails();
		else
			details = null;
	}

	public URI getErrorURI(){
		return errorURI;
	}
	
	
	/**
	 * Equivalent to Throwable.getMessage()
	 * 
	 * @return details error.
	 */
	public String getErrorDescription(){
		return this.getMessage();
	}
	
	
	public Object getErrorDetails(){
		return details;
	}
	
	protected WampCallErrorMessage createCallErrorMessage(String callId){

		if (details == null){
			return WampCallErrorMessage.create(callId, getErrorURI(), getErrorDescription());
		} else {
			ObjectHolder errorDetailsJson = ObjectHolderFactory.createForObject(details);
			return WampCallErrorMessage.create(callId, getErrorURI(), getErrorDescription(), errorDetailsJson);
		}
		
	}
}
