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
package com.github.ghetolay.jwamp.message.output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ghetolay.jwamp.message.WampArguments;

/**
 * @author ghetolay
 *
 */

//TODO AWFUL CHANGE THAT FAST !!!
//it's do the job but not really efficient nor pretty/simple coding
public class ArgumentEncoder{

	Object arg;
	Collection<?> argList;
	
	public ArgumentEncoder(Object arg){
		if(arg != null){
			if(arg instanceof Collection)
				argList = (Collection<?>)arg;
			//TODO not use this, keep raw data and send them or convert them is need (msgPack => JSON or JSON => msgPack)
			else if(arg instanceof WampArguments){
				WampArguments wampArg = (WampArguments)arg;
				List<Object> list = new ArrayList<Object>();
				while(wampArg.hasNext())
					list.add(wampArg.nextObject(Object.class));
				
				argList = list;
			}
			else
				this.arg = arg;
		}
	}
	
	public int size(){
		if(argList != null)
			return argList.size();

		return arg == null?0:1;
	}

	/**
	 * mandatory define the process in case there is no arg : 
	 * call message arguments can be non-present, null or present
	 * but
	 * callresult/publish/event/ message must have a argument : null or present
	 * 
	 * 
	 * @param generator
	 * @param mandatory define if we omit or put null in case of no argument
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public void encode(JsonGenerator generator, boolean mandatory) throws JsonProcessingException, IOException{
		if(argList != null)
			for(Object obj : argList)
				generator.writeObject(obj);
			
		else if(arg != null){
			if(arg.getClass().isArray()){
				Class<?> type = arg.getClass().getComponentType();
				if(type.equals(int.class))
					for(int obj : (int[])arg)
						generator.writeObject(obj);
				else if(type.equals(short.class))
					for(short obj : (short[])arg)
						generator.writeObject(obj);
				else if(type.equals(float.class))
					for(float obj : (float[])arg)
						generator.writeObject(obj);
				else if(type.equals(long.class))
					for(long obj : (long[])arg)
						generator.writeObject(obj);
				else if(type.equals(boolean.class))
					for(boolean obj : (boolean[])arg)
						generator.writeObject(obj);
				else if(type.equals(byte.class))
					for(byte obj : (byte[])arg)
						generator.writeObject(obj);
				else
					for(Object obj : (Object[])arg)
						generator.writeObject(obj);
			}else
				generator.writeObject(arg);
		}else if(mandatory)
			generator.writeNull();
	}
}
