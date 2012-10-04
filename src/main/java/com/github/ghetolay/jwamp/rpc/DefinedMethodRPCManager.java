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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampObjectArray;

/**
 * @author ghetolay
 *
 */
public class DefinedMethodRPCManager extends AbstractRPCManager {

	Object objectClass;
	
	public DefinedMethodRPCManager(Object objectClass){
		if(objectClass == null)
			throw new IllegalArgumentException("objectClass can't be null");
		
		this.objectClass = objectClass;
	}
	
	//TODO logging
	@Override
	protected RunnableAction getRunnableAction(String sessionId, WampCallMessage message) {
		
		String methodName = "do" + Character.toUpperCase(message.getProcId().charAt(0)) + message.getProcId().substring(1);
		
		try {
			Method m = objectClass.getClass().getMethod(methodName, String.class, WampCallMessage.class);
			
			if(m.getReturnType().equals(WampObjectArray.class)){
			
				if(log.isTraceEnabled())
					log.trace("found matching method " + methodName + " of class " + objectClass.getClass().getName() + "for procId : " + message.getProcId());
				
				MethodAction result = new MethodAction(sessionId, message);
				result.setMethod(m);
			
				return result;
			}
		} catch (SecurityException e) {
			if(log.isTraceEnabled())
				log.trace("",e);
		} catch (NoSuchMethodException e) {
			if(log.isTraceEnabled())
				log.trace("Unable to find method for " + message.getProcId(),e);
		}
		
		return null;
	}

	private class MethodAction extends RunnableAction{
		
		private Method m;
		
		public MethodAction(String sessionId, WampCallMessage message) {
			super(sessionId, message);
		}
		
		private void setMethod(Method method){
			m = method;
		}
		
		public void run() {
			try{
				try{
					if(log.isTraceEnabled())
						log.trace("Calling Method " + m.getName() + " of class " + objectClass.getClass().getName());
					
					WampObjectArray result = (WampObjectArray) m.invoke(objectClass, sessionId, message);
					//TODO change test, handle with a exception or do not send result automatically or....
					//NoReturn must be used in case of multiple result only.
					if(result != WampObjectArray.NORETURN)
						sendResult(message.getCallId(), result);
						
				}catch(InvocationTargetException e){
					
					sendError(message.getCallId(), message.getProcId(), e.getTargetException());
			
				} catch (IllegalArgumentException e) {
					log.warn("blabla",e);
				} catch (IllegalAccessException e) {
					log.warn("blabla",e);
				}
			}catch(IOException e){
				log.debug("Unable send response blabla");
			}
		}
		
	}
}
