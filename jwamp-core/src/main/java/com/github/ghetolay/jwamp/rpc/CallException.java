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

import com.github.ghetolay.jwamp.message.WampCallErrorMessage;

/**
 * @author ghetolay
 *
 */
public class CallException extends Throwable {

	private static final long serialVersionUID = -6836214026673216476L;

	private String details;
	
	public CallException(String errorDesc, String errorDetails){
		super(errorDesc);
		details = errorDetails;
	}
	
	public CallException(WampCallErrorMessage msg){
		super(msg.getErrorDesc());
		details = msg.getErrorDetails();
	}
	
	/**
	 * Equivalent to Throwable.getMessage()
	 * 
	 * @return details error.
	 */
	public String getErrorDescription(){
		return this.getMessage();
	}
	
	
	public String getErrorDetails(){
		return details;
	}
}
