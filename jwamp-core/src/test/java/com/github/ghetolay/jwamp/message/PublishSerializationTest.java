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
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.ghetolay.jwamp.message.MessageType;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.utils.ObjectHolderFactory;



/**
 * Test of publish message according to wamp specification examples
 * http://wamp.ws/spec/#publish_message
 * 
 * @author ghetolay
 *
 */
public class PublishSerializationTest extends AbstractSerializationTest{
	
	/////////////////// 1 //////////////////////
	@Test
	public void publish1_encode() throws EncodeException, IOException, URISyntaxException{
		WampPublishMessage msg = WampPublishMessage.createSimple(new URI("http://example.com/simple"), ObjectHolderFactory.createForObject("Hello, world!"));
		
		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);
		
		assertEquals("[7,\"http://example.com/simple\",\"Hello, world!\"]",
				sw.toString());
	}
	
	@Test
	public void publish1_decode() throws IOException, DecodeException, URISyntaxException{
		
		WampMessage msg = decoder.decode(
				new StringReader("[7,\"http://example.com/simple\",\"Hello, world!\"]"));
				
		if( !(msg instanceof WampPublishMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.PUBLISH, msg.getMessageType());
		
		WampPublishMessage pMsg = (WampPublishMessage)msg;
		assertEquals(new URI("http://example.com/simple"), pMsg.getTopicURI());
		assertEquals("Hello, world!", pMsg.getEvent().getAs(String.class));
	}
	
	/////////////////// 2 //////////////////////
	@Test
	public void publish2_encode() throws EncodeException, IOException, URISyntaxException{
		WampPublishMessage msg = WampPublishMessage.createSimple(new URI("http://example.com/simple"), ObjectHolderFactory.createForObject((Object)null));
		
		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);
		
		assertEquals("[7,\"http://example.com/simple\",null]",
				sw.toString());
	}
	
	@Test
	public void publish2_decode() throws DecodeException, IOException, URISyntaxException{
		WampMessage msg = decoder.decode(
				new StringReader("[7,\"http://example.com/simple\",null]"));
				
		if( !(msg instanceof WampPublishMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.PUBLISH, msg.getMessageType());
		
		WampPublishMessage pMsg = (WampPublishMessage)msg;
		assertEquals(new URI("http://example.com/simple"), pMsg.getTopicURI());
		assertNull(pMsg.getEvent().getAs(Object.class));
	}
		
	/////////////////// 3 //////////////////////
	@Test
	public void publish3_encode() throws EncodeException, IOException, URISyntaxException{
		
		SomeObject obj = new SomeObject();
		obj.setRand(0.09187032734575862);
		obj.setFlag(false);
		obj.setNum(23);
		obj.setName("Kross");
		
		//Seriously month 0-based !!! ?
		calendar.set(2012, 2, 29, 10, 41, 9);
		calendar.set(Calendar.MILLISECOND, 864);

		obj.setCreated(calendar.getTime()); 
		
		WampPublishMessage msg = WampPublishMessage.createSimple(new URI("http://example.com/event#myevent2"), ObjectHolderFactory.createForObject(obj));
		
		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);
		
		assertEquals("[7,\"http://example.com/event#myevent2\","
						+	"{" 
		                +	  "\"rand\":0.09187032734575862,"
		                +	  "\"flag\":false,"
		                +	  "\"num\":23,"
		                +	  "\"name\":\"Kross\","
		                +	  "\"created\":\"2012-03-29T10:41:09.864Z\""
		               	+	"}]",
		             sw.toString()
		);
	}
	
	@Test
	public void publish3_decode() throws DecodeException, IOException, ParseException, URISyntaxException{
		WampMessage msg = decoder.decode(
				new StringReader("[7, \"http://example.com/event#myevent2\","
					+	"{" 
	                +	  "\"rand\": 0.09187032734575862,"
	                +	  "\"flag\": false,"
	                +	  "\"num\": 23,"
	                +	  "\"name\":\"Kross\","
	                +	  "\"created\": \"2012-03-29T10:41:09.864Z\""
	               	+	"}]" 
	      ));
				
		if( !(msg instanceof WampPublishMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.PUBLISH, msg.getMessageType());
		
		WampPublishMessage pMsg = (WampPublishMessage)msg;
		assertEquals(new URI("http://example.com/event#myevent2"), pMsg.getTopicURI());
		
		Object event = pMsg.getEvent().getAs(new TypeReference<Map<String, Object>>(){});
		if(event == null || !(event instanceof Map))
			fail("wrong event type");
		
		@SuppressWarnings("unchecked")
		Map<String,Object> evtObject = (Map<String,Object>)event;
		
		assertEquals(0.09187032734575862, evtObject.get("rand"));
		assertEquals(false, evtObject.get("flag"));
		assertEquals(23, evtObject.get("num"));
		assertEquals("Kross", evtObject.get("name"));
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.set(2012, 2, 29, 10, 41, 9);
		cal.set(Calendar.MILLISECOND, 864);
		
		assertEquals(dateFormat.parse((String)evtObject.get("created")), cal.getTime());
	}
	
	/////////////////// 4 //////////////////////
	@Test
	public void publish4_encode() throws EncodeException, IOException, URISyntaxException{
		WampPublishMessage msg = WampPublishMessage.createWithExclude(new URI("event:myevent1"), ObjectHolderFactory.createForObject("hello"), Arrays.asList("NwtXQ8rdfPsy-ewS","dYqgDl0FthI6_hjb") );
		
		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);
		
		assertEquals("[7,\"event:myevent1\","
						+ "\"hello\","
						+ "[\"NwtXQ8rdfPsy-ewS\",\"dYqgDl0FthI6_hjb\"]]",
				  sw.toString());
	}
	
	@Test
	public void publish4_decode() throws DecodeException, IOException, URISyntaxException{
		WampMessage msg = decoder.decode(
				new StringReader("[7,\"event:myevent1\","
									+ "\"hello\","
									+ "[\"NwtXQ8rdfPsy-ewS\",\"dYqgDl0FthI6_hjb\"]]"));
				
		if( !(msg instanceof WampPublishMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.PUBLISH, msg.getMessageType());
		
		WampPublishMessage pMsg = (WampPublishMessage)msg;
		assertEquals(new URI("event:myevent1"), pMsg.getTopicURI());
		assertEquals("hello", pMsg.getEvent().getAs(String.class));
		
		List<String> excludeList = new ArrayList<String>(pMsg.getExclude());
		assertEquals(2, excludeList.size());
		assertEquals("NwtXQ8rdfPsy-ewS", excludeList.get(0));
		assertEquals("dYqgDl0FthI6_hjb", excludeList.get(1));
	}
	
	/////////////////// 5 //////////////////////
	@Test
	public void publish5_encode() throws EncodeException, IOException, URISyntaxException{
		WampPublishMessage msg = WampPublishMessage.createWithExcludeAndEligible(new URI("event:myevent1"), ObjectHolderFactory.createForObject("hello"), Arrays.asList(new String[0]), Arrays.asList("NwtXQ8rdfPsy-ewS"));
		
		StringWriter sw = new StringWriter();
		encoder.encode(msg, sw);
		
		assertEquals("[7,\"event:myevent1\","
						+ "\"hello\","
						+ "[],"
						+ "[\"NwtXQ8rdfPsy-ewS\"]]",
					sw.toString());
	}
	
	@Test
	public void publish5_decode() throws JsonParseException, DecodeException, IOException, URISyntaxException{
		WampMessage msg = decoder.decode(
				new StringReader("[7,\"event:myevent1\","
									+ "\"hello\","
									+ "[],"
									+ "[\"NwtXQ8rdfPsy-ewS\"]]"));
				
		if( !(msg instanceof WampPublishMessage) )
			fail("Wrong message type");
		assertEquals(MessageType.PUBLISH, msg.getMessageType());
		
		WampPublishMessage pMsg = (WampPublishMessage)msg;
		assertEquals(new URI("event:myevent1"), pMsg.getTopicURI());
		assertEquals("hello", pMsg.getEvent().getAs(String.class));
		
		assertTrue(pMsg.getExclude().isEmpty());
		
		assertEquals(1, pMsg.getEligible().size());
		assertEquals("NwtXQ8rdfPsy-ewS", pMsg.getEligible().get(0));
	}
	
	@JsonPropertyOrder({ "rand", "flag", "num", "name", "created" })
	static public class SomeObject{
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

		public boolean getFlag() {
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
