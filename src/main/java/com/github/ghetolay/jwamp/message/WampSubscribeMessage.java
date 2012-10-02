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
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author ghetolay
 *
 */
public class WampSubscribeMessage extends WampUnSubscribeMessage {

	private WampArguments args = new WampArguments();

	public WampSubscribeMessage(String topicId){
		super(WampMessage.SUBSCRIBE, topicId);
	}

	public WampSubscribeMessage(JsonParser parser)
			throws BadMessageFormException {
		super(WampMessage.SUBSCRIBE, parser);

		try {
			
			JsonToken token = parser.nextToken();
			if(token != null && token != JsonToken.END_ARRAY)
				args.setParser(parser);
			
		} catch (JsonParseException e) {
			throw new BadMessageFormException(e);
		} catch (IOException e) {
			throw new BadMessageFormException(e);
		}
	}

	public WampSubscribeMessage(Object[] JSONArray) throws BadMessageFormException{
		super(JSONArray);

		if(JSONArray.length > 2){
			for(int i = 2 ; i < JSONArray.length; i++)
				args.addArgument(JSONArray[i]);
		}
	}

	@Override
	public String toJSONMessage(ObjectMapper objectMapper) throws JsonGenerationException, JsonMappingException, IOException{
		
		StringBuffer result = startMsg();
		
		result.append(',');
		appendString(result, getTopicId());
		
		args.toJSONMessage(result, objectMapper);
		
		return endMsg(result);
	}

	public WampArguments getArguments(){
		return args;
	}
	
	public void setArguments(List<Object> args){
		this.args.setArguments(args);
	}
	
	public void addArgument(Object arg) {
		args.addArgument(arg);
	}
}
