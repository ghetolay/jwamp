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
package com.github.ghetolay.jwamp.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.endpoint.SessionManager;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.rpc.Action;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.CallException;
import com.github.ghetolay.jwamp.rpc.RPCCallHandler;

/**
 * @author ghetolay
 *
 */
//TODO how do we do to map a URI to a method name
public class DefinedMethodActionMapping implements ActionMapping<Action>{
	
	protected static final Logger log = LoggerFactory.getLogger(DefinedMethodActionMapping.class);
	
	private HashMap<URI,Action> mapping = new HashMap<URI,Action>();
	
	public DefinedMethodActionMapping(){}
		
	public void addObject(Object object){
		Method sessionSetter = null;
		try {
			sessionSetter = object.getClass().getMethod("setSessionManager", SessionManager.class);
		} catch (NoSuchMethodException | SecurityException e) {}
		
		boolean firstCompleteAction = true;
		
		for(Method m : object.getClass().getMethods()){
			String name = m.getName();
			Class<?>[] params = m.getParameterTypes();
			
			
			if(params != null
					&& params.length == 2
					&& name.startsWith("do")
					&& name.length() > 2){
				//simple call action
				if( params[0].equals(String.class) 
					&& params[1].equals(WampArguments.class))
				
					mapping.put(getActionName(name), new SimpleMethodAction(object, m) );
				//Complete Call Action
				else if ( params[0].equals(String.class) 
						  && params[1].equals(WampCallMessage.class)){
				
					if(sessionSetter == null)
						;//TODO log
					else{
						CompleteMethodAction action;
						if(firstCompleteAction){
							action = new SessionCompleteMethodAction(object, m, sessionSetter);
							firstCompleteAction = false;
						}else	
							action = new CompleteMethodAction(object, m);
						
						mapping.put(getActionName(name), action);
					}
				}
			}
		}
	}
	
	private String getActionName(String methodName){
		return methodName.substring(2).toLowerCase();
	}
	
	public void addAction(URI actionURI, Action action){
		throw new UnsupportedOperationException("use addObject");
	}
	
	@Override
	public Iterator<Action> getActionsIterator() {
		return mapping.values().iterator();
	}
	
	//TODO logging
	@Override
	public Action getAction(URI actionId){
		
		MethodAction methodAction = (MethodAction)mapping.get(actionId.toString().toLowerCase());
			
		if(log.isTraceEnabled())
			if(methodAction != null)
				log.trace("found matching method " + methodAction.method.getName() + " of class " + methodAction.object.getClass().getName() + "for procId : " + actionId);
			else
				log.trace("Unable to find method for " + actionId);
		
		return methodAction;
	}
	
	//waiting for lambda java 8 !!!
	private static class MethodAction implements Action{
		Object object;
		Method method;
		
		MethodAction(Object object, Method method){
			this.object = object;
			this.method = method;
		}
	}
	
	private static class SimpleMethodAction extends MethodAction implements CallAction{
		
		SimpleMethodAction(Object object, Method method){
			super(object, method);
		}

		@Override
		public Object execute(String sessionId, WampArguments args)
				throws CallException {
			try{
				return method.invoke(object, sessionId, args);
			} catch(InvocationTargetException e){
				if(e.getTargetException() instanceof CallException)
					throw (CallException)e.getTargetException();
				
				if(log.isWarnEnabled())
					log.warn("Error on action method : " + e.getTargetException());
				
				throw new CallException(CallException.generic, "Internal Error");
			} catch (Exception e){
				throw new CallException(CallException.generic, "Internal Error");
			}
		}
	}
	
	private static class CompleteMethodAction extends MethodAction implements RPCCallHandler{
		
		CompleteMethodAction(Object object, Method callMethod){
			super(object, callMethod);
		}

		@Override
		public void setSessionManager(SessionManager sessionManager) {}

		@Override
		public void execute(String sessionId, WampCallMessage msg) 
				throws CallException{
			try{
				method.invoke(object, sessionId, msg);
			} catch(InvocationTargetException e){
				if(e.getTargetException() instanceof CallException)
					throw (CallException)e.getTargetException();
				
				if(log.isWarnEnabled())
					log.warn("Error on action method : " + e.getTargetException());
				
				throw new CallException(CallException.generic, "Internal Error");
			} catch (Exception e){
				throw new CallException(CallException.generic, "Internal Error");
			}
		}
	}
	
	//1 per object class
	private static class SessionCompleteMethodAction extends CompleteMethodAction{
		
		Method sessionSetter;
		
		SessionCompleteMethodAction(Object object, Method callMethod, Method sessionSetter){
			super(object, callMethod);
			
			this.sessionSetter = sessionSetter;
		}

		@Override
		public void setSessionManager(SessionManager sessionManager) {
			try {
				sessionSetter.invoke(object, sessionManager);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				if(log.isWarnEnabled())
					log.warn("Error on setSessionManager : ", e);
			}
		}
	}

}
