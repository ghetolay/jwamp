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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.Test;

import com.github.ghetolay.jwamp.message.MessageType;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.message.WampUnsubscribeMessage;



/**
 * Test of subscribe and unsubscribe message according to wamp specification examples
 * http://wamp.ws/spec/#subscribe_message
 * http://wamp.ws/spec/#unsubscribe_message
 * 
 * @author ghetolay
 *
 */
public class Sub_UnsubSerializationTest extends AbstractSerializationTest{

	/////////////// Subscribe ////////////////
	/////////////////// 1 //////////////////////
	@Test
	public void subscribe1Encode() throws EncodeException, IOException, URISyntaxException{
		WampSubscribeMessage msg = WampSubscribeMessage.create(new URI("http://example.com/simple"));

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[5,\"http://example.com/simple\"]",
				sw.toString());
	}

	@Test
	public void subscribe1Decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[5, \"http://example.com/simple\"]"));

		if( !(msg instanceof WampSubscribeMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.SUBSCRIBE, msg.getMessageType());

		WampSubscribeMessage sMsg = (WampSubscribeMessage)msg;
		assertEquals(new URI("http://example.com/simple"), sMsg.getTopicURI());
	}

	/////////////////// 2 //////////////////////
	@Test
	public void subscribe2Encode() throws EncodeException, IOException, URISyntaxException{
		WampSubscribeMessage msg = WampSubscribeMessage.create(new URI("event:myevent1"));

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[5,\"event:myevent1\"]",
				sw.toString());
	}

	@Test
	public void subscribe2Decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[5, \"event:myevent1\"]"));

		if( !(msg instanceof WampSubscribeMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.SUBSCRIBE, msg.getMessageType());

		WampSubscribeMessage sMsg = (WampSubscribeMessage)msg;
		assertEquals(new URI("event:myevent1"), sMsg.getTopicURI());
	}

	/////////////// Unsubscribe ////////////////
	/////////////////// 1 //////////////////////
	@Test
	public void unsubscribe1Encode() throws EncodeException, IOException, URISyntaxException{
		WampUnsubscribeMessage msg = WampUnsubscribeMessage.create(new URI("http://example.com/simple"));

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[6,\"http://example.com/simple\"]",
				sw.toString());
	}

	@Test
	public void unsubscribe1Decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[6, \"http://example.com/simple\"]"));

		if( !(msg instanceof WampUnsubscribeMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.UNSUBSCRIBE, msg.getMessageType());

		WampUnsubscribeMessage uMsg = (WampUnsubscribeMessage)msg;
		assertEquals(new URI("http://example.com/simple"), uMsg.getTopicURI());
	}

	/////////////////// 2 //////////////////////
	@Test
	public void unsubscribe2Encode() throws EncodeException, IOException, URISyntaxException{
		WampUnsubscribeMessage msg = WampUnsubscribeMessage.create(new URI("event:myevent1"));

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[6,\"event:myevent1\"]",
				sw.toString());
	}

	@Test
	public void unsubscribe2Decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[6, \"event:myevent1\"]"));

		if( !(msg instanceof WampUnsubscribeMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.UNSUBSCRIBE, msg.getMessageType());

		WampUnsubscribeMessage uMsg = (WampUnsubscribeMessage)msg;
		assertEquals(new URI("event:myevent1"), uMsg.getTopicURI());
	}
}
