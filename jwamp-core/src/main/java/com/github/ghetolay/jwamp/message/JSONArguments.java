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

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.utils.DynamicValue;
import com.github.ghetolay.jwamp.utils.DynamicValueBuilder;

/**
 * @author ghetolay
 *
 */
public class JSONArguments implements WampArguments{

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private JsonParser parser;

	public JSONArguments(JsonParser parser){
		if(parser.getCurrentToken() != null && parser.getCurrentToken() != JsonToken.END_ARRAY)
			this.parser = parser;
	}

	public DynamicValue nextObject() {
		return DynamicValueBuilder.fromObject( nextObject(Object.class) );
	}
	
	//TODO a bcp tester, liste vide, tout type de collection
	//Si jamais pas de list mais un seul element (token == object), renvoyer une liste avec un elements
	public <T> T nextObject(TypeReference<T> type){
		if(parser == null)
			return null;

		try{
			if(parser.getCurrentToken() != null){
				T result =  parser.readValueAs(type);
				parser.nextToken();
				return result;
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
		if(parser == null)
			return null;

		try{
			if(parser.getCurrentToken() != null){
				T result =  parser.readValueAs(c);
				parser.nextToken();
				return result;
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
	
	//TODO count number of [] if currentToken = ] and it's the last it means it's the end
	public boolean hasNext(){
		return parser != null && parser.getCurrentToken() != JsonToken.END_ARRAY;
	}
	
	public int size(){
		return -1;
	}
	
	@Override
	public String toString(){
		return "ReadableWampArrayObject { " + parser==null?"null }":"args present }" ;
	}
}
