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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ghetolay
 *
 */
public class WampObjectArray {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected List<Object> args;

	private JsonParser parser; 
	private boolean first = true;
	
	public WampObjectArray(){}
	
	//objs become the argument list
	//TODO maybe clone ? Should not be visible
	public WampObjectArray(Object[] objs){
		if(objs != null && objs.length > 0)
			args = Arrays.asList(objs);
	}
	
	public WampObjectArray(List<Object> objs){
		args = objs;
	}
	
	//Add 1 object to the argument list 
	///!\ do not confond with other constructor !!
	public WampObjectArray(Object obj){
		args = new ArrayList<Object>();
		
		args.add(obj);
	}
	
	void setParser(JsonParser parser, boolean allowMultipleArguments) throws JsonParseException, IOException{
		this.parser = parser;
	}

	void toJSONMessage(StringBuffer result, ObjectMapper objectMapper, boolean allowMultipleArguments) throws JsonGenerationException, JsonMappingException, IOException {
		if(parser != null)
			getAsList();
		
		if(allowMultipleArguments){
			for(Object obj : args){
				result.append(',');
				result.append(objectMapper.writeValueAsString(obj));
			}
		}else{
			result.append(',');
			if(args != null && args.size() == 1)
				result.append(objectMapper.writeValueAsString(args.get(0)));
			else
				result.append(objectMapper.writeValueAsString(args));
		}
	}

		//TODO: pas super le logging
		public List<Object> getAsList(){
			if(parser==null)
				return args;

			if(args == null)
				args = new ArrayList<Object>();
			
			Object obj;
			while((obj = nextObject(true)) != null)
				args.add(obj);
				
			return args;
		}

		public Object nextObject() {
			return nextObject(Object.class, false);
		}
		
		public Object nextObject(boolean cache) {
			return nextObject(Object.class);
		}
		
		public <T> T nextObject(TypeReference<T> type){
			return nextObject(type, false);
		}

		//TODO a bcp tester, liste vide, tout type de collection
		//Si jamais pas de list mais un seul element (token == object), renvoyer une liste avec un elements
		public <T> T nextObject(TypeReference<T> type, boolean cache){
			if(parser == null)
				return null;

			try{
				//TODO special case when user send a type defined data =/= List
				/*
				if(first && parser.getCurrentToken() == JsonToken.START_ARRAY 
						&& !type.getType() instanceof List)
					parser.nextToken();
				*/
				
				if(parser.getCurrentToken() != null){
					T result =  parser.readValueAs(type);
					parser.nextToken();
					
					if(result != null){
						if(cache)
							addObject(result);
					
						if(first)
							first = false;
						
						return result;
					}
				}

			}catch(JsonProcessingException e){
				if(log.isDebugEnabled())
					log.debug("Error parsing arguments of type " + type.toString(), e);
			} catch (IOException e) {
				if(log.isDebugEnabled())
					log.debug("Error parsing arguments of type " + type.toString(), e);
			}

			parser = null;
			return null;
		}

		public <T> T nextObject(Class<T> c){
			return nextObject(c, false);
		}
		
		public <T> T nextObject(Class<T> c, boolean cache){
			if(parser == null)
				return null;

			try{
				if(first && parser.getCurrentToken()==JsonToken.START_ARRAY)
					parser.nextToken();
				
				if(parser.getCurrentToken() != null){
					T result =  parser.readValueAs(c);
					parser.nextToken();
					
					if(result != null){
						if(cache)
							addObject(result);
					
						if(first)
							first = false;
						
						return result;
					}
				}
			}catch(JsonProcessingException e){
				if(log.isDebugEnabled())
					log.debug("Error parsing arguments of type " + c.getName(), e);
			} catch (IOException e) {
				if(log.isDebugEnabled())
					log.debug("Error parsing arguments of type " + c.getName(), e);
			}

			parser = null;
			return null;
		}

		void nextObjectList(List<Object> args){
			this.args = args;
		}

		//not a fan of the if
		//solution 1, create the arraylist on constructor... waste if no arguments and json string will not be null
		public void addObject(Object arg) {			
			if(args == null)
				args = new ArrayList<Object>();

			if(arg != null)
				args.add(arg);
			
		}

		//TODO a revoir
		//false may be unknown
		public boolean isEmpty(){
			if(args == null){
				if(parser == null)
					return true;
				//unknown
				return false;
			}
			
			return args.isEmpty();
		}
		
		/*
		 * DO NOT USE ! only used in test.
		 * It's very difficult to compare 2 WampObjectArray because of the stream
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o){
			if(o!= null && o instanceof WampObjectArray){
				WampObjectArray obj = (WampObjectArray)o;
				if(args != null && obj.args != null){
					if(parser == null && obj.parser == null){
						Iterator<Object> it1 = args.iterator();
						Iterator<Object> it2 = obj.args.iterator();
						
						while( it1.hasNext() && it2.hasNext()){
							Object o1 = it1.next();
							Object o2 = it2.next();
							if(!o1.equals(o2))
							//if(!it1.next().equals(it2.next()))
								return false;
						}
						
						if(!it1.hasNext() && !it2.hasNext())
							return true;
					}//else we don't know so no
				}else if(parser == null && obj.parser == null)
					return true;
			}
			
			return false;
		}
		
		public static NORETURN NORETURN = new NORETURN();
		private static class NORETURN extends WampObjectArray{}
	}
