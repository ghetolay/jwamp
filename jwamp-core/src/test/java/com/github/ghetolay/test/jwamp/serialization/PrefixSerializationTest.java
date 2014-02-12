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
package com.github.ghetolay.test.jwamp.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.Test;

import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampPrefixMessage;



/**
 * Test of prefix message according to wamp specification examples
 * http://wamp.ws/spec/#prefix_message
 * 
 * 
 * @author ghetolay
 *
 */
public class PrefixSerializationTest extends AbstractSerializationTest{
	
	@Test
	public void prefix1Encode() throws EncodeException, IOException{
		WampPrefixMessage msg = new WampPrefixMessage();
		msg.setPrefix("calc");
		msg.setUri("http://example.com/simple/calc#");
		
		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);
		
		assertEquals("[1,\"calc\",\"http://example.com/simple/calc#\"]",
				sw.toString());
	}
	
	@Test
	public void prefix1Decode() throws IOException, DecodeException{
		
		WampMessage msg = decoder.decode(
				new StringReader("[1, \"calc\", \"http://example.com/simple/calc#\"]"));
				
		if( !(msg instanceof WampPrefixMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.PREFIX, msg.getMessageType());
		
		WampPrefixMessage wMsg = (WampPrefixMessage)msg;
		assertEquals("calc", wMsg.getPrefix());
		assertEquals("http://example.com/simple/calc#", wMsg.getUri());
	}
}
