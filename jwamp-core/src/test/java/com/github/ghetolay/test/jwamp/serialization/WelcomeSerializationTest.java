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
import com.github.ghetolay.jwamp.message.WampWelcomeMessage;



/**
 * Test of welcome message according to wamp specification examples
 * http://wamp.ws/spec/#welcome_message
 * 
 * Test with jwamp specification
 * 
 * @author ghetolay
 *
 */
public class WelcomeSerializationTest extends AbstractSerializationTest{
	
	@Test
	public void welcomeEncode() throws EncodeException, IOException{
		WampWelcomeMessage msg = new WampWelcomeMessage();
		msg.setSessionId("v59mbCGDXZ7WTyxB");
		msg.setProtocolVersion(1);
		msg.setImplementation("Autobahn/0.5.1");
		
		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);
		
		assertEquals("[0,\"v59mbCGDXZ7WTyxB\",1,\"Autobahn/0.5.1\"]",
				sw.toString());
	}
	
	@Test
	public void welcomeDecode() throws IOException, DecodeException{
		
		WampMessage msg = decoder.decode(
				new StringReader("[0, \"v59mbCGDXZ7WTyxB\", 1, \"Autobahn/0.5.1\"]"));
				
		if( !(msg instanceof WampWelcomeMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.WELCOME, msg.getMessageType());
		
		WampWelcomeMessage wMsg = (WampWelcomeMessage)msg;
		assertEquals("v59mbCGDXZ7WTyxB", wMsg.getSessionId());
		assertEquals(1, wMsg.getProtocolVersion());
		assertEquals("Autobahn/0.5.1", wMsg.getImplementation());
	}
}
