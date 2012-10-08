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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.github.ghetolay.jwamp.message.ReadableWampArrayObject;
import com.github.ghetolay.jwamp.utils.ClassType;
import com.github.ghetolay.jwamp.utils.DynamicValue;


/**
 * @author ghetolay
 *
 */
public class WritableWampArrayObject implements ReadableWampArrayObject{
	
	protected List<Object> args;
	
	public WritableWampArrayObject(){}
	
	public WritableWampArrayObject (Object[] args){
		if(args != null && args.length > 0)
			this.args = Arrays.asList(args);
	}
	
	public WritableWampArrayObject (List<?> args){
		int size;
		if(args != null && (size = args.size()) > 0 ){
			this.args = new ArrayList<Object>(size);
			this.args.addAll(args);
		}
	}
	
	public static WritableWampArrayObject withFirstObject(Object obj){
		WritableWampArrayObject result = new WritableWampArrayObject();
		result.addObject(obj);
		return result;
	}
	
	public WritableWampArrayObject(ReadableWampArrayObject readableWampArrayObject){
		DynamicValue obj;
		while((obj = readableWampArrayObject.nextObject()) != null)
			addObject(obj.asObject());
	}
	
	//not fan of the if
	//solution 1, create the arraylist on constructor... waste if no arguments and json string will not be null
	public void addObject(Object arg) {			
		if(args == null)
			args = new ArrayList<Object>(3);
			
		args.add(arg);
	}
	
	public boolean isEmpty(){
		return args== null || args.isEmpty();
	}
	
	public int size(){
		return args==null?0:args.size();
	}
	
	Iterator<Object> Iterator(){
		return args.iterator();
	}
	
	public DynamicValue nextObject() {
		return null;
	}

	public <T> T nextObject(Class<T> ct) {
		return null;
	}
	
	public <T> T nextObject(ClassType<T> ct) {
		return null;
	}
	
	@Override
	public String toString(){
		return args==null?"null":args.toString();
	}
	
	public static NORETURN NORETURN = new NORETURN();
	static class NORETURN extends WritableWampArrayObject{}
}
