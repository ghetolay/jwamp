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
package com.github.ghetolay.test;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;
import org.msgpack.packer.BufferPacker;
import org.msgpack.unpacker.BufferUnpacker;

import com.github.ghetolay.jwamp.test.server.SomeObject;

/**
 * @author ghetolay
 *
 */
public class SerializeTest {
	
	public static void main(String args[]){
		
		try {
			msgPackParameterized();
			
			//dynamicType();
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void msgPackParameterized(){
		
		MessagePack mp = new MessagePack();
		
		Param<String> param = new Param<String>("lol");
		
		try {
			byte[] b = mp.write(param);
			
			mp.read(b, new TypeReference<Param<String>>(){});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static void dynamicType() throws Exception{
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_DEFAULT);
		
		MessagePack mp = new MessagePack();
		
		boolean boolTest = true;
		int intTest = 450;
		String stringTest = "TEST";
		
		SomeObject objectTest = new SomeObject();
		objectTest.setFieldOne("fieldOne");
		objectTest.setFieldTwo(2);
		
		List<String> stringlistTest = Arrays.asList( new String[]{ "ONE", "TWO", "THREE", "FOUR" } );
		List<Object> listTest = Arrays.asList( new Object[]{ "ONE", 5, true, "FOUR" } );
		Float floatTest = 2.456f;
		Double doubleTest = 999999999999d;
		byte[] rawTest = new byte[]{120,110,-87,58,62,45,86,95,125,48,3,-97,25, -111};
		
		Object[] msg = new Object[]{ boolTest, objectTest, intTest, stringTest, stringlistTest, listTest, floatTest, doubleTest, rawTest};
		
		String jsonMsg = mapper.writeValueAsString(msg);
		
		JsonParser parser = mapper.getJsonFactory().createJsonParser(jsonMsg);
		parser.nextToken();
		parser.nextToken();
		
		System.out.println("------ JSON ------");
		
		Object boolRes = parser.readValueAs(Object.class);
		System.out.println("bool " + boolRes.getClass().getName());
		
		Object objectRes = parser.readValueAs(Object.class);
		System.out.println("object " + objectRes.getClass().getName());
		
		Object intRes = parser.readValueAs(Object.class);
		System.out.println("int " + intRes.getClass().getName());
		
		Object stringRes = parser.readValueAs(Object.class);
		System.out.println("string " + stringRes.getClass().getName());
		
		Object stringlistRes = parser.readValueAs(Object.class);
		System.out.println("string list " + stringlistRes.getClass().getName() +  " " + ((ParameterizedType)stringlistRes.getClass().getGenericSuperclass()).getActualTypeArguments()[0] );
		
		Object listRes = parser.readValueAs(Object.class);
		System.out.println("list " + listRes.getClass().getName() +  " " + ((ParameterizedType)stringlistTest.getClass().getGenericSuperclass()).getActualTypeArguments()[0] );
		
		Object floatRes = parser.readValueAs(Object.class);
		System.out.println("float " + floatRes.getClass().getName());
		
		Object doubleRes = parser.readValueAs(Object.class);
		System.out.println("double " + doubleRes.getClass().getName());
		
		Object rawRes = parser.readValueAs(Object.class);
		System.out.println("raw " + rawRes.getClass().getName());
		
		BufferPacker packer = mp.createBufferPacker();
		packer.write(msg);
		
		BufferUnpacker unpacker = mp.createBufferUnpacker(packer.toByteArray());
		
		Object o = unpacker.read();
		
		System.out.println("------ Message Pack ------");
		
		printObject(o);
	}	
	
	public static void printList(List<Object> l){
		System.out.println("--- printList --- ");
		for(Object o : l)
			printObject(o);
		System.out.println("--- endList --- ");
	}
	
	public static void printMap(Map<Object,Object> m){
		System.out.println("--- printMap --- ");
		for(Entry<Object,Object> e : m.entrySet()){
			System.out.print("key : ");
			printObject(e.getKey());
			System.out.print("Value : ");
			printObject(e.getValue());
		}
		System.out.println("--- endMap --- ");
	}
	
	@SuppressWarnings("unchecked")
	public static void printObject(Object o){
		if( o == null)
			System.out.println("null");
		else if( o instanceof List)
			printList((List<Object>)o);
		else if(o instanceof Map)
			printMap((Map<Object,Object>)o);
		else
			System.out.println(o);
	}
	
	@Message
	private static class Param<T>{
		@SuppressWarnings("unused")
		T t;
		
		Param(T t){
			this.t = t;
		}
	}
}
