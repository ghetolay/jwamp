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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.Test;

import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampCallErrorMessage;



/**
 * Test of callerror message according to wamp specification examples
 * http://wamp.ws/spec/#callerror_message
 * 
 * @author ghetolay
 *
 */
public class CallErrorSerializationTest extends AbstractSerializationTest{

	/////////////////// 1 //////////////////////
	@Test
	public void callerror1_encode() throws EncodeException, IOException, URISyntaxException{
		OutputWampCallErrorMessage msg = new OutputWampCallErrorMessage();
		msg.setCallId("gwbN3EDtFv6JvNV5");
		msg.setErrorUri(new URI("http://autobahn.tavendo.de/error#generic"));
		msg.setErrorDesc("math domain error");		

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[4,\"gwbN3EDtFv6JvNV5\","
				+ "\"http://autobahn.tavendo.de/error#generic\","
				+ "\"math domain error\"]",
				sw.toString());
	}

	@Test
	public void callerror1_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[4, \"gwbN3EDtFv6JvNV5\","
						+ "\"http://autobahn.tavendo.de/error#generic\","
						+ "\"math domain error\"]"));

		if( !(msg instanceof WampCallErrorMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.CALLERROR, msg.getMessageType());

		WampCallErrorMessage crMsg = (WampCallErrorMessage)msg;
		assertEquals("gwbN3EDtFv6JvNV5", crMsg.getCallId());
		assertEquals(new URI("http://autobahn.tavendo.de/error#generic"), crMsg.getErrorUri());
		assertEquals("math domain error", crMsg.getErrorDesc());
	}

	/////////////////// 2 //////////////////////
	@Test
	public void callerror2_encode() throws EncodeException, IOException, URISyntaxException{
		OutputWampCallErrorMessage msg = new OutputWampCallErrorMessage();
		msg.setCallId("7bVW5pv8r60ZeL6u");
		msg.setErrorUri(new URI("http://example.com/error#number_too_big"));
		msg.setErrorDesc("1001 too big for me, max is 1000");
		msg.setErrorDetails(1000);		

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[4,\"7bVW5pv8r60ZeL6u\","
				+"\"http://example.com/error#number_too_big\","
				+"\"1001 too big for me, max is 1000\","
				+"1000]",
				sw.toString());
	}

	@Test
	public void callerror2_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[4, \"7bVW5pv8r60ZeL6u\","
						+"\"http://example.com/error#number_too_big\","
						+"\"1001 too big for me, max is 1000\","
						+"1000]"));

		if( !(msg instanceof WampCallErrorMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.CALLERROR, msg.getMessageType());

		WampCallErrorMessage crMsg = (WampCallErrorMessage)msg;
		assertEquals("7bVW5pv8r60ZeL6u", crMsg.getCallId());
		assertEquals(new URI("http://example.com/error#number_too_big"), crMsg.getErrorUri());
		assertEquals("1001 too big for me, max is 1000", crMsg.getErrorDesc());
		assertEquals(new Integer(1000), crMsg.getErrorDetails().nextObject(Integer.class));
	}

	/////////////////// 3 //////////////////////
	@Test
	public void callerror3_encode() throws EncodeException, IOException, URISyntaxException{
		OutputWampCallErrorMessage msg = new OutputWampCallErrorMessage();
		msg.setCallId("AStPd8RS60pfYP8c");
		msg.setErrorUri(new URI("http://example.com/error#invalid_numbers"));
		msg.setErrorDesc("one or more numbers are multiples of 3");
		msg.setErrorDetails(new int[] {0,3});		

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[4,\"AStPd8RS60pfYP8c\","
					   +"\"http://example.com/error#invalid_numbers\","
					   +"\"one or more numbers are multiples of 3\","
					   +"[0,3]]",
				sw.toString());
	}

	@Test
	public void callerror3_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[4, \"AStPd8RS60pfYP8c\","
					   +"\"http://example.com/error#invalid_numbers\","
					   +"\"one or more numbers are multiples of 3\","
					   +"[0, 3]]"));

		if( !(msg instanceof WampCallErrorMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.CALLERROR, msg.getMessageType());

		WampCallErrorMessage crMsg = (WampCallErrorMessage)msg;
		assertEquals("AStPd8RS60pfYP8c", crMsg.getCallId());
		assertEquals(new URI("http://example.com/error#invalid_numbers"), crMsg.getErrorUri());
		assertEquals("one or more numbers are multiples of 3", crMsg.getErrorDesc());
		assertEquals(Arrays.asList(0,3), crMsg.getErrorDetails().nextObject().asList());
	}
}