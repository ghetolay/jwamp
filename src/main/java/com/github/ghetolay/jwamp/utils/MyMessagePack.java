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
package com.github.ghetolay.jwamp.utils;

import java.lang.reflect.Type;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ghetolay
 *
 */
public class MyMessagePack extends MessagePack{
	
	public MyMessagePack(){
		super(new MyTemplateRegistry(null));
	}
	
	private static class MyTemplateRegistry extends TemplateRegistry {
		
		
		public MyTemplateRegistry(TemplateRegistry registry) {
			super(registry);
		}

		@Override
		 public synchronized Template<?> lookup(Type targetType){
			 try{
				return super.lookup(targetType);
			 }catch(MessageTypeException e){
				 if(e.getMessage().startsWith("Cannot find template for")
					 && targetType instanceof Class<?>){
					 	 Logger log = LoggerFactory.getLogger(getClass());
					 	 if(log.isTraceEnabled())
					 		 log.trace("Automatic register " + ((Class<?>)targetType).getName() );
					 	 
						 register((Class<?>)targetType);
						 return lookup(targetType);
				 }else
					 throw e;
			 }
		 }
	}
}
