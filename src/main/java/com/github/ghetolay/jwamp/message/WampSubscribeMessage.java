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
import java.util.List;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;

/**
 * @author ghetolay
 *
 */
public class WampSubscribeMessage extends WampUnSubscribeMessage {

	private List<Object> args;

	private JsonParser parser; 

	public WampSubscribeMessage(String topicId){
		super(WampMessage.SUBSCRIBE, topicId);
	}

	public WampSubscribeMessage(JsonParser parser)
			throws BadMessageFormException {
		super(WampMessage.SUBSCRIBE, parser);

		this.parser = parser;
	}

	public WampSubscribeMessage(Object[] JSONArray) throws BadMessageFormException{
		super(JSONArray);

		if(JSONArray.length > 2){
			args = new ArrayList<Object>(JSONArray.length - 3);
			for(int i = 3 ; i < JSONArray.length; i++)
				args.add(JSONArray[i]);
		}
	}

	@Override
	public Object[] toJSONArray() {
		Object[] superResult = super.toJSONArray();

		if(args == null)
			return superResult;


		Object[] result = Arrays.copyOf(superResult,superResult.length + args.size());

		for(int i = superResult.length; i < result.length; i++)
			result[i] = args.get(i - superResult.length);

		return result;
	}

	//TODO: avoid copy of code from WampCalMessage, use a abstractclass for message with undefined number of arguments
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

	public Object nextArgument() throws JsonProcessingException, IOException{
		return nextArgument(Object.class);
	}

	public <T> T nextArgument(Class<T> c) throws JsonProcessingException, IOException{
		if(parser == null)
			return null;

		if(parser.nextToken() == JsonToken.END_ARRAY){
			parser = null;
			return null;
		}

		return parser.readValueAs(c);
	}

	public void setArguments(List<Object> args){
		this.args = args;
	}

	public void addArgument(Object arg) {
		if(args == null)
			args = new ArrayList<Object>();

		args.add(arg);
	}
}
