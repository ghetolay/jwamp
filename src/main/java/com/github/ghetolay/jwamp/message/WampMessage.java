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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WampMessage {
	
	protected transient final Logger log = LoggerFactory.getLogger(getClass());
	
	public transient final static int WELCOME     = 0;
	public transient final static int PREFIX      = 1;
	public transient final static int CALL        = 2;
	public transient final static int CALLRESULT  = 3;
	public transient final static int CALLERROR   = 4;
	public transient final static int SUBSCRIBE   = 5;
	public transient final static int UNSUBSCRIBE = 6;
	public transient final static int PUBLISH     = 7;
	public transient final static int EVENT       = 8;
	
	//add by me
	public transient final static int CALLMORERESULT = 300;
	
	protected int messageType;
	
	public int getMessageType(){
		return messageType;
	}
}
