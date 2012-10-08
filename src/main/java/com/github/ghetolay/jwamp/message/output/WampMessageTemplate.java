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
package com.github.ghetolay.jwamp.message.output;

import java.io.IOException;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

import com.github.ghetolay.jwamp.message.WampMessage;

/**
 * @author ghetolay
 *
 */
public abstract class WampMessageTemplate<T extends WampMessage> implements Template<T> {
	
	public void write(Packer pk, T v, boolean required) throws IOException {
		write(pk,v);
	}

	public T read(Unpacker u, T to) throws IOException {
		return read(u,to,false);
	}

	public T read(Unpacker u, T to, boolean required) throws IOException {
		throw new MessageTypeException("WampMessage should not use a template for unserialization. Use JsonWampMessageBuilder instead.");
	}
}
