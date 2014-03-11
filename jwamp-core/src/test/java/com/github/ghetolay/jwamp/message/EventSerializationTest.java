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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.Test;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.ghetolay.jwamp.message.MessageType;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.utils.ObjectHolderFactory;



/**
 * Test of event message according to wamp specification examples
 * http://wamp.ws/spec/#event_message
 * 
 * @author ghetolay
 *
 */
public class EventSerializationTest extends AbstractSerializationTest{

	/////////////////// 1 //////////////////////
	@Test
	public void event1_encode() throws EncodeException, IOException, URISyntaxException{
		WampEventMessage msg = WampEventMessage.create(new URI("http://example.com/simple"), ObjectHolderFactory.createForObject("Hello, I am a simple event."));

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[8,\"http://example.com/simple\",\"Hello, I am a simple event.\"]",
				sw.toString());
	}

	@Test
	public void event1_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[8, \"http://example.com/simple\", \"Hello, I am a simple event.\"]"));

		if( !(msg instanceof WampEventMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.EVENT, msg.getMessageType());

		WampEventMessage eMsg = (WampEventMessage)msg;
		assertEquals(new URI("http://example.com/simple"), eMsg.getTopicURI());
		assertEquals("Hello, I am a simple event.", eMsg.getEvent().getAs(String.class));
	}

	/////////////////// 2 //////////////////////
	@Test
	public void event2_encode() throws EncodeException, IOException, URISyntaxException{
		WampEventMessage msg = WampEventMessage.create(new URI("http://example.com/simple"), ObjectHolderFactory.createForObject((Object)null));

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[8,\"http://example.com/simple\",null]",
				sw.toString());
	}

	@Test
	public void event2_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[8, \"http://example.com/simple\", null]"));

		if( !(msg instanceof WampEventMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.EVENT, msg.getMessageType());

		WampEventMessage eMsg = (WampEventMessage)msg;
		assertEquals(new URI("http://example.com/simple"), eMsg.getTopicURI());
		assertNull(eMsg.getEvent().getAs(Object.class));
	}

	/////////////////// 3 //////////////////////
	@Test
	public void event3_encode() throws EncodeException, IOException, URISyntaxException{

		SomeObject obj = new SomeObject();
		obj.setRand(0.09187032734575862);
		obj.setFlag(false);
		obj.setNum(23);
		obj.setName("Kross");
		
		calendar.set(2012, 2, 29, 10, 41, 9);
		calendar.set(Calendar.MILLISECOND, 864);
		
		obj.setCreated(calendar.getTime());

		WampEventMessage msg = WampEventMessage.create(new URI("http://example.com/event#myevent2"), ObjectHolderFactory.createForObject(obj));
		
		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[8,\"http://example.com/event#myevent2\","
						   +'{'
						   + "\"rand\":0.09187032734575862,"
						   + "\"flag\":false,"
						   + "\"num\":23,"
						   + "\"name\":\"Kross\","
						   + "\"created\":\"2012-03-29T10:41:09.864Z\""
						   +"}]",
				sw.toString());
	}

	@Test
	public void event3_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[8, \"http://example.com/event#myevent2\","
						   +'{'
						   + "\"rand\": 0.09187032734575862,"
						   + "\"flag\": false,"
						   + "\"num\": 23,"
						   + "\"name\": \"Kross\","
						   + "\"created\": \"2012-03-29T10:41:09.864Z\""
						   +"}]"));

		if( !(msg instanceof WampEventMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.EVENT, msg.getMessageType());

		WampEventMessage eMsg = (WampEventMessage)msg;
		assertEquals(new URI("http://example.com/event#myevent2"), eMsg.getTopicURI());
		
		SomeObject obj = eMsg.getEvent().getAs(SomeObject.class);
		
		calendar.set(2012, 2, 29, 10, 41, 9);
		calendar.set(Calendar.MILLISECOND, 864);
		
		assertEquals(calendar.getTime(), obj.getCreated());
	}
	
	public static class SomeObject{
		
		private double rand;
		private boolean flag;
		private int num;
		private String name;
		private Date created;
		
		public double getRand() {
			return rand;
		}
		public void setRand(double rand) {
			this.rand = rand;
		}
		public boolean isFlag() {
			return flag;
		}
		public void setFlag(boolean flag) {
			this.flag = flag;
		}
		public int getNum() {
			return num;
		}
		public void setNum(int num) {
			this.num = num;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		@JsonSerialize(using=JsonDateSerializer.class)
		public Date getCreated() {
			return created;
		}
		public void setCreated(Date created) {
			this.created = created;
		}
	}

}