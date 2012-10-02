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
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
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
public class WampArguments {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected List<Object> args;

	private JsonParser parser; 

	void setParser(JsonParser parser){
		this.parser = parser;
	}

	public void toJSONMessage(StringBuffer result, ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException {
		for(Object obj : args){
			result.append(',');
			result.append(objectMapper.writeValueAsString(obj));
		}
	}

		//TODO: pas super le logging
		public List<Object> getArguments(){
			if(parser==null)
				return args;

			try{
				args = new ArrayList<Object>();

				while(parser.nextToken() != JsonToken.END_ARRAY)
					args.add(parser.readValueAs(Object.class));
			}catch(Exception e){
				log.error("ParseException",e);
			}

			parser = null;
			return args;
		}

		public Object nextArgument() {
			return nextArgument(Object.class);
		}

		//TODO a bcp tester, liste vide, tout type de collection
		//Si jamais pas de list mais un seul element (token == object), renvoyer une liste avec un elements
		public <T> T nextArgument(TypeReference<T> type){
			if(parser == null)
				return null;

			try{
				if(parser.getCurrentToken() != null)
					return parser.readValueAs(type);

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

		public <T> T nextArgument(Class<T> c){
			if(parser == null)
				return null;

			try{
				if(parser.getCurrentToken() != null)
					return parser.readValueAs(c);

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

		void setArguments(List<Object> args){
			this.args = args;
		}

		//not a fan of the if
		//solution 1, create the arraylist on constructor... waste if no arguments and json string will not be null
		void addArgument(Object arg) {
			if(args == null)
				args = new ArrayList<Object>();

			args.add(arg);
		}

		//TODO a revoir
		public boolean isEmpty(){
			if(args == null){
				if(parser == null)
					return true;

				return false;
			}

			return args.isEmpty();
		}
	}
