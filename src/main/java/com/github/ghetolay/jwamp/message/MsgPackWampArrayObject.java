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

import java.io.IOException;

import org.msgpack.type.Value;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import com.github.ghetolay.jwamp.utils.ClassType;
import com.github.ghetolay.jwamp.utils.DynamicValue;
import com.github.ghetolay.jwamp.utils.DynamicValueBuilder;

/**
 * @author ghetolay
 *
 */
public class MsgPackWampArrayObject implements ReadableWampArrayObject{
	
	Unpacker unpack;
	
	int size = 1;
	
	public MsgPackWampArrayObject(Unpacker unpack, int size) {
		this.size = size;
		this.unpack = unpack;
	}
	
	public MsgPackWampArrayObject(Unpacker unpack) {
		try{
			if(unpack.getNextType() == ValueType.ARRAY)
				size = unpack.readArrayBegin();
			
			this.unpack = unpack;
		}catch(IOException e){
			//TODO
			e.printStackTrace();
		}
	}


	public DynamicValue nextObject() {
		if(unpack == null)
			return null;
		
		if(size <= 0){
			unpack = null;
			return null;
		}
		
		try {
			Value result = unpack.readValue();
			size--;
			
			return DynamicValueBuilder.fromValue(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		unpack = null;
		return null;
	}

	public <T> T nextObject(Class<T> ct) {
		if(ct.equals(Object.class))
			throw new IllegalArgumentException("Class can't Object, use nextObject() instead.");
		
		if(unpack == null)
			return null;
		
		if(size <= 0){
			unpack = null;
			return null;
		}
		
		try {
			T result = unpack.read(ct);
			size--;
			
			return result;
		} catch (Exception e) {
			//TODO
			e.printStackTrace();
		}
		
		unpack = null;
		return null;
	}

	public <T> T nextObject(ClassType<T> ct) {
		return null;
	}
	
	public int size(){
		return size;
	}
	
	@Override
	public String toString(){
		return "ReadableWampArrayObject { " + unpack==null?"null }":size + " object left }";
	}
}
