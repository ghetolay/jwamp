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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.Test;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.ghetolay.jwamp.message.MessageType;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.utils.ObjectHolderFactory;



/**
 * Test of callresult message according to wamp specification examples
 * http://wamp.ws/spec/#callresult_message
 * 
 * @author ghetolay
 *
 */
public class CallResultSerializationTest extends AbstractSerializationTest{

	/////////////////// 1 //////////////////////
	@Test
	public void callresult1_encode() throws EncodeException, IOException{
		WampCallResultMessage msg = WampCallResultMessage.create("CcDnuI2bl2oLGBzO", ObjectHolderFactory.VOID);

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[3,\"CcDnuI2bl2oLGBzO\",null]",
				sw.toString());
	}

	@Test
	public void callresult1_decode() throws IOException, DecodeException{

		WampMessage msg = decoder.decode(
				new StringReader("[3, \"CcDnuI2bl2oLGBzO\", null]"));

		if( !(msg instanceof WampCallResultMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.CALLRESULT, msg.getMessageType());

		WampCallResultMessage crMsg = (WampCallResultMessage)msg;
		assertEquals("CcDnuI2bl2oLGBzO", crMsg.getCallId());
		assertNull(crMsg.getResult().getAs(Object.class));
	}

	/////////////////// 2 //////////////////////
	@Test
	public void callresult2_encode() throws EncodeException, IOException{
		WampCallResultMessage msg = WampCallResultMessage.create("otZom9UsJhrnzvLa", ObjectHolderFactory.createForObject("Awesome result .."));

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[3,\"otZom9UsJhrnzvLa\",\"Awesome result ..\"]",
				sw.toString());
	}

	@Test
	public void callresult2_decode() throws IOException, DecodeException{

		WampMessage msg = decoder.decode(
				new StringReader("[3, \"otZom9UsJhrnzvLa\", \"Awesome result ..\"]"));

		if( !(msg instanceof WampCallResultMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.CALLRESULT, msg.getMessageType());

		WampCallResultMessage crMsg = (WampCallResultMessage)msg;
		assertEquals("otZom9UsJhrnzvLa", crMsg.getCallId());
		assertEquals("Awesome result ..", crMsg.getResult().getAs(String.class));
	}

	/////////////////// 3 //////////////////////
	@Test
	public void callresult3_encode() throws EncodeException, IOException{
		
		SomeObject obj = new SomeObject();
		obj.setValue3(true);
		obj.setValue2("singsing");
		obj.setValue1("23");
		
		calendar.set(2012, 2, 29, 10, 29, 16);
		calendar.set(Calendar.MILLISECOND, 625);
		
		obj.setModified(calendar.getTime());

		WampCallResultMessage msg = WampCallResultMessage.create("CcDnuI2bl2oLGBzO", ObjectHolderFactory.createForObject(obj));

		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);

		assertEquals("[3,\"CcDnuI2bl2oLGBzO\","
					   + '{'
					   +   "\"value3\":true,"
					   +   "\"value2\":\"singsing\","
					   +   "\"value1\":\"23\","
					   +   "\"modified\":\"2012-03-29T10:29:16.625Z\""
					   + "}]",
				sw.toString());
	}

	@Test
	public void callresult3_decode() throws IOException, DecodeException{

		WampMessage msg = decoder.decode(
				new StringReader("[3, \"CcDnuI2bl2oLGBzO\","
					   + '{'
					   +   "\"value3\": true,"
					   +   "\"value2\": \"singsing\","
					   +   "\"value1\": \"23\","
					   +   "\"modified\": \"2012-03-29T10:29:16.625Z\""
					   + "}]"));

		if( !(msg instanceof WampCallResultMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.CALLRESULT, msg.getMessageType());

		WampCallResultMessage crMsg = (WampCallResultMessage)msg;
		assertEquals("CcDnuI2bl2oLGBzO", crMsg.getCallId());
		
		SomeObject obj = crMsg.getResult().getAs(SomeObject.class);
		
		assertTrue(obj.getValue3());
		assertEquals("singsing", obj.getValue2());
		assertEquals("23", obj.getValue1());
		
		calendar.set(2012, 2, 29, 10, 29, 16);
		calendar.set(Calendar.MILLISECOND, 625);
		assertEquals(calendar.getTime(), obj.getModified());
	}
	
	public static class SomeObject{
		
		private boolean value3;
		private String value2;
		private String value1;
		private Date modified;
		
		public boolean getValue3() {
			return value3;
		}
		public void setValue3(boolean value3) {
			this.value3 = value3;
		}
		public String getValue2() {
			return value2;
		}
		public void setValue2(String value2) {
			this.value2 = value2;
		}
		public String getValue1() {
			return value1;
		}
		public void setValue1(String value1) {
			this.value1 = value1;
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