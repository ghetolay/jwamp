/**
 * 
 */
package com.github.ghetolay.jwamp.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * @author Kevin
 *
 */
public class JsonBackedObjectFactory {

	private JsonBackedObjectFactory() {
	}

	
	static public JsonBackedObject readNextObject(JsonParser parser) throws JsonProcessingException, IOException{
		TreeNode node = parser.readValueAsTree();
		return new JacksonParserBasedObject(parser.getCodec(), node);
	}
	
	static public JsonBackedObject createForObject(Object o){
		return new ObjectBasedObject(o);
	}
	
	static public List<JsonBackedObject> createForObjects(Object... objects){
		List<JsonBackedObject> jsonArgs = new ArrayList<JsonBackedObject>(objects.length);
		for (Object arg : objects) {
			jsonArgs.add(JsonBackedObjectFactory.createForObject(arg));
		}
		return jsonArgs;
	}
	
	public static JsonBackedObject VOID = new JsonBackedObject() {
		
		@Override
		public <T> T getAs(TypeReference<T> typeReference) {
			return null;
		}
		
		@Override
		public <T> T getAs(Class<T> clazz) {
			return null;
		}
		
		public String toString() {
			return "void";
		};
	};
	
	private static class ObjectBasedObject implements JsonBackedObject{

		private final Object val;
		
		public ObjectBasedObject(Object val) {
			this.val = val;
		}

		@Override
		public <T> T getAs(Class<T> clazz) {
			return clazz.cast(val);
		}
		
		@Override
		public <T> T getAs(TypeReference<T> typeReference){
			return (T)val;
		}
		
		@Override
		public String toString() {
			return val.toString();
		}

	}	
	
	private static class JacksonParserBasedObject implements JsonBackedObject{

		private final ObjectCodec codec;
		private final TreeNode val;
		
		public JacksonParserBasedObject(ObjectCodec codec, TreeNode val) {
			this.codec = codec;
			this.val = val;
		}

		@Override
		public <T> T getAs(Class<T> clazz) {
			try {
				return codec.readValue(codec.treeAsTokens(val), clazz);
			} catch (IOException e) {
				throw new ClassCastException("Unable to get " + val + " as " + clazz + " - " + e.getMessage());
			}
		}
		
		@Override
		public <T> T getAs(TypeReference<T> typeReference){
			try {
				return codec.readValue(codec.treeAsTokens(val), typeReference);
			} catch (IOException e) {
				throw new ClassCastException("Unable to get " + val + " as " + typeReference + " - " + e.getMessage());
			}
		}
		
		@Override
		public String toString() {
			return val.toString();
		}

	}
}
