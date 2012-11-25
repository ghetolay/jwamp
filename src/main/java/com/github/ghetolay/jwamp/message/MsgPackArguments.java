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
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.msgpack.template.ObjectTemplate;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

import com.github.ghetolay.jwamp.utils.DynamicValue;
import com.github.ghetolay.jwamp.utils.DynamicValueBuilder;

/**
 * @author ghetolay
 *
 */
public class MsgPackArguments implements WampArguments{
	
	private Unpacker unpack;
	
	private int size = 1;
	
	public MsgPackArguments(Unpacker unpack, int size) {
		this.size = size;
		this.unpack = unpack;
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
		
		if(unpack == null)
			return null;
		
		try {
			
			if(size <= 0){
				unpack = null;
				return null;
			}
		
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

	public <T> T nextObject(TypeReference<T> tr) {
		
		if(unpack == null)
			return null;
		
		try {
			
			if(size <= 0){
				unpack = null;
				return null;
			}
		
			T result =read(unpack,tr);
			size--;
			return result;
			
		} catch (Exception e) {
			//TODO
			e.printStackTrace();
		}
		
		unpack = null;
		return null;
	}
	
	//copy from MessagePack...
	@SuppressWarnings("unchecked")
	private <T> T read(Unpacker u, TypeReference<T> v) throws IOException{    	
    	if(v.getType() instanceof ParameterizedType){
    		ParameterizedType pType = (ParameterizedType) v.getType();

    		if( (pType.getRawType() instanceof Class) && List.class.isAssignableFrom((Class<?>) pType.getRawType()) )
    			return (T) Templates.tList(new ObjectTemplate(pType.getActualTypeArguments()[0])).read(u, null);
    		
    		if( (pType.getRawType() instanceof Class) && Collection.class.isAssignableFrom((Class<?>) pType.getRawType()) )
    			return (T) Templates.tCollection(new ObjectTemplate(pType.getActualTypeArguments()[0])).read(u, null);
    		
    		if( (pType.getRawType() instanceof Class) && Map.class.isAssignableFrom((Class<?>) pType.getRawType()) )
    			return (T) Templates.tMap(new ObjectTemplate(pType.getActualTypeArguments()[0]), new ObjectTemplate(pType.getActualTypeArguments()[1])).read(u, null);
    	}
    	
        return (T) Templates.TObject.read(u, v);
    }
	
	public boolean hasNext(){
		try {
			return unpack != null && unpack.getNextType() != null;
		} catch (IOException e) {
			return false;
		}
	}
	
	public int size(){
		return size;
	}
	
	@Override
	public String toString(){
		return "ReadableWampArrayObject { " + unpack==null?"null }":size + " object left }";
	}
}
