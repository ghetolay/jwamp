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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.Test;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampCallMessage;



/**
 * Test of call message according to wamp specification examples
 * http://wamp.ws/spec/#call_message
 * 
 * @author ghetolay
 *
 */
public class CallSerializationTest extends AbstractSerializationTest{

	/////////////////// 1 //////////////////////
	@Test
	public void call1_encode() throws EncodeException, IOException, URISyntaxException{
		OutputWampCallMessage msg = new OutputWampCallMessage();
		msg.setCallId("7DK6TdN4wLiUJgNM");
		msg.setProcURI(new URI("http://example.com/api#howdy"));

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[2,\"7DK6TdN4wLiUJgNM\",\"http://example.com/api#howdy\"]",
				sw.toString());
	}

	@Test
	public void call1_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[2, \"7DK6TdN4wLiUJgNM\", \"http://example.com/api#howdy\"]"));

		if( !(msg instanceof WampCallMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.CALL, msg.getMessageType());

		WampCallMessage cMsg = (WampCallMessage)msg;
		assertEquals("7DK6TdN4wLiUJgNM", cMsg.getCallId());
		assertEquals(new URI("http://example.com/api#howdy"), cMsg.getProcURI());
	}

	/////////////////// 2 //////////////////////
	@Test
	public void call2_array_encode() throws EncodeException, IOException, URISyntaxException{
		OutputWampCallMessage msg = new OutputWampCallMessage();
		msg.setCallId("Yp9EFZt9DFkuKndg");
		msg.setProcURI(new URI("api:add2"));
		msg.setArgument(new int[] {23,99});

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[2,\"Yp9EFZt9DFkuKndg\",\"api:add2\",23,99]",
				sw.toString());
	}

	@Test
	public void call2_list_encode() throws EncodeException, IOException, URISyntaxException{
		OutputWampCallMessage msg = new OutputWampCallMessage();
		msg.setCallId("Yp9EFZt9DFkuKndg");
		msg.setProcURI(new URI("api:add2"));
		msg.setArgument(Arrays.asList(23,99));

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[2,\"Yp9EFZt9DFkuKndg\",\"api:add2\",23,99]",
				sw.toString());
	}

	@Test
	public void call2_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[2, \"Yp9EFZt9DFkuKndg\", \"api:add2\", 23, 99]"));

		if( !(msg instanceof WampCallMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.CALL, msg.getMessageType());

		WampCallMessage cMsg = (WampCallMessage)msg;
		assertEquals("Yp9EFZt9DFkuKndg", cMsg.getCallId());
		assertEquals(new URI("api:add2"), cMsg.getProcURI());
	}

	/////////////////// 3 //////////////////////
	@Test
	public void call3_encode() throws EncodeException, IOException, URISyntaxException{
		OutputWampCallMessage msg = new OutputWampCallMessage();
		msg.setCallId("J5DkZJgByutvaDWc");
		msg.setProcURI(new URI("http://example.com/api#storeMeal"));

		Meal meal = new Meal();
		meal.setCategory("dinner");
		meal.setCalories(2309);

		msg.setArgument(meal);

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[2,\"J5DkZJgByutvaDWc\",\"http://example.com/api#storeMeal\","
				+  '{'
				+     "\"category\":\"dinner\","
				+     "\"calories\":2309"
				+  "}]",
				sw.toString());
	}

	@Test
	public void call3_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[2, \"J5DkZJgByutvaDWc\", \"http://example.com/api#storeMeal\","
						+  '{'
						+     "\"category\": \"dinner\","
						+     "\"calories\": 2309"
						+  "}]"));

		if( !(msg instanceof WampCallMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.CALL, msg.getMessageType());

		WampCallMessage cMsg = (WampCallMessage)msg;
		assertEquals("J5DkZJgByutvaDWc", cMsg.getCallId());
		assertEquals(new URI("http://example.com/api#storeMeal"), cMsg.getProcURI());

		Meal meal = cMsg.getArguments().nextObject(Meal.class);

		assertEquals("dinner", meal.getCategory());
		assertEquals(2309, meal.getCalories());
	}

	public static class Meal{

		private String category;
		private int calories;

		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		public int getCalories() {
			return calories;
		}
		public void setCalories(int calories) {
			this.calories = calories;
		}
	}

	/////////////////// 4 //////////////////////
	@Test
	public void call4_encode() throws EncodeException, IOException, URISyntaxException{
		OutputWampCallMessage msg = new OutputWampCallMessage();
		msg.setCallId("Dns3wuQo0ipOX1Xc");
		msg.setProcURI(new URI("http://example.com/api#woooat"));
		
		//kinda a hack to put a null 
		//cause since argument is not mandatory default behavior is to skip it in case of null
		msg.setArgument(new Object[]{null});
		
		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[2,\"Dns3wuQo0ipOX1Xc\",\"http://example.com/api#woooat\",null]",
				sw.toString());
	}

	@Test
	public void call4_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[2, \"Dns3wuQo0ipOX1Xc\", \"http://example.com/api#woooat\", null]"));

		if( !(msg instanceof WampCallMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.CALL, msg.getMessageType());

		WampCallMessage cMsg = (WampCallMessage)msg;
		assertEquals("Dns3wuQo0ipOX1Xc", cMsg.getCallId());
		assertEquals(new URI("http://example.com/api#woooat"), cMsg.getProcURI());
		
		//fail right or wrong ? cause there is something, a null value
		//assertFalse(cMsg.getArguments().hasNext())
		
		assertNull(cMsg.getArguments().nextObject());
	}

	/////////////////// 5 //////////////////////
	@Test
	public void call5_encode() throws EncodeException, IOException, URISyntaxException{
		OutputWampCallMessage msg = new OutputWampCallMessage();
		msg.setCallId("ujL7WKGXCn8bkvFV");
		msg.setProcURI(new URI("keyvalue:set"));

		SomeObject obj = new SomeObject();
		obj.setValue1("23");
		obj.setValue2("singsing");
		obj.setValue3(true);
		
		calendar.set(2012, 2, 29, 10, 29, 16);
		calendar.set(Calendar.MILLISECOND, 625);
		
		obj.setModified(calendar.getTime());
		
		List<Object> args = new ArrayList<Object>(2);
		args.add("foobar");
		args.add(obj);
		
		msg.setArgument(args);
		
		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[2,\"ujL7WKGXCn8bkvFV\",\"keyvalue:set\","
						   +"\"foobar\","
						   +'{'
						   +   "\"value1\":\"23\","
						   +   "\"value2\":\"singsing\","
						   +   "\"value3\":true,"
						   +   "\"modified\":\"2012-03-29T10:29:16.625Z\""
						   +"}]",
				sw.toString());
	}

	@Test
	public void call5_decode() throws IOException, DecodeException, URISyntaxException{

		WampMessage msg = decoder.decode(
				new StringReader("[2, \"ujL7WKGXCn8bkvFV\", \"keyvalue:set\","
						   +"\"foobar\","
						   +'{'
						   +   "\"value1\": \"23\","
						   +   "\"value2\": \"singsing\","
						   +   "\"value3\": true,"
						   +   "\"modified\": \"2012-03-29T10:29:16.625Z\""
						   +"}]"));

		if( !(msg instanceof WampCallMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.CALL, msg.getMessageType());

		WampCallMessage cMsg = (WampCallMessage)msg;
		assertEquals("ujL7WKGXCn8bkvFV", cMsg.getCallId());
		assertEquals(new URI("keyvalue:set"), cMsg.getProcURI());
		
		WampArguments args = cMsg.getArguments();
		
		assertEquals(args.nextObject(String.class), "foobar");
		
		SomeObject obj = args.nextObject(SomeObject.class);
		
		assertEquals("23", obj.getValue1());
		assertEquals("singsing", obj.getValue2());
		assertTrue(obj.isValue3());
		
		calendar.set(2012, 2, 29, 10, 29, 16);
		calendar.set(Calendar.MILLISECOND, 625);
		
		assertEquals(calendar.getTime(), obj.getModified());
	}
	
	public static class SomeObject{
		
		private String value1;
		private String value2;
		private boolean value3;
		private Date modified;
		public String getValue1() {
			return value1;
		}
		public void setValue1(String value1) {
			this.value1 = value1;
		}
		public String getValue2() {
			return value2;
		}
		public void setValue2(String value2) {
			this.value2 = value2;
		}
		public boolean isValue3() {
			return value3;
		}
		public void setValue3(boolean value3) {
			this.value3 = value3;
		}
		
		@JsonSerialize(using=JsonDateSerializer.class)
		public Date getModified() {
			return modified;
		}
		public void setModified(Date modified) {
			this.modified = modified;
		}
		
	}
}