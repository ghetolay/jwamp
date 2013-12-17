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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.WampCallMessage;

/**
 * @author ghetolay
 *
 */
public class DefinedMethodRPCManager extends AbstractRPCManager{

	Object objectClass;
	
	HashMap<String,Method> mapping = new HashMap<String,Method>();
	
	public DefinedMethodRPCManager(Object objectClass){
		if(objectClass == null)
			throw new IllegalArgumentException("objectClass can't be null");
		
		this.objectClass = objectClass;
		
		for(Method m : objectClass.getClass().getMethods()){
			String name = m.getName();
			Class<?>[] params = m.getParameterTypes();
			if(params != null && params.length == 3
					&& name.startsWith("do")
					&& name.length() > 2
					&& params[0].equals(String.class) 
					&& params[1].equals(WampArguments.class)
					&& params[2].equals(CallResultSender.class))
				
				mapping.put(Character.toUpperCase(name.charAt(2)) + name.substring(3), m);
		}
	}
	
	//TODO logging
	@Override
	protected RunnableAction getRunnableAction(String sessionId, WampCallMessage message) {
		
		String methodName = Character.toUpperCase(message.getProcId().charAt(0)) + message.getProcId().substring(1);
		final Method method = mapping.get(methodName);
			
		if(method != null){
			if(log.isTraceEnabled())
				log.trace("found matching method " + methodName + " of class " + objectClass.getClass().getName() + "for procId : " + message.getProcId());
				
			return new RunnableAction(sessionId, message){			
				protected void excuteAction(String sessionID, WampArguments args, CallResultSender sender) throws CallException, Throwable {
					try{
						method.invoke(objectClass, sessionID, args, sender);
					} catch(InvocationTargetException e){
						throw e.getTargetException();
					}
				}
			};
		}
		
		if(log.isTraceEnabled())
			log.trace("Unable to find method for " + message.getProcId());
		return null;
	}
}
