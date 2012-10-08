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
package com.github.ghetolay.jwamp;

import java.util.Comparator;

import com.github.ghetolay.jwamp.message.WampMessage;

/**
 * @author ghetolay
 *
 */
public class WampMessageHandlerComparator implements Comparator<WampMessageHandler>{

	
	public int compare(WampMessageHandler o1, WampMessageHandler o2) {
		return getPriority(o2.getMsgType()) - getPriority(o1.getMsgType());
	}
	
	private int getPriority(int[] msgType){
		
		if(msgType == null)
			throw new IllegalStateException("The Method WampMessageHandler.getMsgType() cannot return null");
		
		int priority = 0;
			for(int type : msgType)
				switch(type){
					case WampMessage.CALL:
					case WampMessage.CALLRESULT:
					case WampMessage.CALLMORERESULT:
					case WampMessage.EVENT:
					case WampMessage.PUBLISH:
						priority = Math.max(priority, 2);
						break;	
					case WampMessage.SUBSCRIBE:
						priority = Math.max(priority, 1);
						break;
					default:
				}
		
		return priority;
	}
	
}

