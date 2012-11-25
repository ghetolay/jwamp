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
package com.github.ghetolay.jwamp;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;

/**
 * @author ghetolay
 *
 */
public class WampSerializer {
	
	public static enum format { JSON, BINARY };
	
	private ObjectMapper mapper;
	private MessagePack msgpack;
	private BufferPacker packer;
	
	
	private format desiredFormat = format.JSON;
	
	public ObjectMapper getObjectMapper() {
		if( mapper == null){
			mapper = new ObjectMapper();
			//maybe give the possibility to change this configuration per connection
			mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_DEFAULT);
		}
		return mapper;
	}

	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public MessagePack getMessagepack() {
		if(msgpack == null)
			setMessagepack(new MessagePack());
		return msgpack;
	}

	BufferPacker getPacker(){
		if(packer == null)
			packer = getMessagepack().createBufferPacker();
		return packer;
	}
	
	public void setMessagepack(MessagePack msgpack) {
		this.msgpack = msgpack;
	}

	public format getDesiredFormat() {
		return desiredFormat;
	}

	public void setDesiredFormat(format desiredFormat) {
		this.desiredFormat = desiredFormat;
	}
}
