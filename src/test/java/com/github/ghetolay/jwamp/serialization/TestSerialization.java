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
package com.github.ghetolay.jwamp.serialization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.testng.annotations.Test;

import com.github.ghetolay.jwamp.WampSerializer;
import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampMessageDeserializer;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampPublishMessage;
import com.github.ghetolay.jwamp.message.output.WampMessageSerializer;



/**
 * @author ghetolay
 *
 */
public class TestSerialization {

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
	
	WampSerializer serializer = new WampSerializer();
	
	//Tests are from wamp specification
	@Test
	public void serialize1() throws SerializationException{
		OutputWampPublishMessage msg = new OutputWampPublishMessage();
		msg.setTopicId("http://example.com/simple");
		msg.setEvent("Hello, world!");
		
		assertEquals("[7,\"http://example.com/simple\",\"Hello, world!\"]",
				WampMessageSerializer.serialize(msg, serializer.getObjectMapper()));
	}
	
	@Test
	public void deserialize1() throws JsonParseException, SerializationException, IOException{
		WampMessage msg = WampMessageDeserializer.deserialize(serializer.getObjectMapper().getJsonFactory().createJsonParser(
				"[7,\"http://example.com/simple\",\"Hello, world!\"]"));
				
		if( !(msg instanceof WampPublishMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.PUBLISH, msg.getMessageType());
		
		WampPublishMessage pMsg = (WampPublishMessage)msg;
		assertEquals("http://example.com/simple", pMsg.getTopicId());
		assertEquals("Hello, world!", pMsg.getEvent());
	}
	
	//in the example event is null
	//does empty event is permit instead ?
	@Test
	public void serialize2() throws SerializationException{
		OutputWampPublishMessage msg = new OutputWampPublishMessage();
		msg.setTopicId("http://example.com/simple");
		
		assertEquals("[7,\"http://example.com/simple\"]",
				WampMessageSerializer.serialize(msg, serializer.getObjectMapper()));
	}
	
	@Test
	public void deserialize2() throws JsonParseException, SerializationException, IOException{
		WampMessage msg = WampMessageDeserializer.deserialize(serializer.getObjectMapper().getJsonFactory().createJsonParser(
				"[7,\"http://example.com/simple\", null]"));
				
		if( !(msg instanceof WampPublishMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.PUBLISH, msg.getMessageType());
		
		WampPublishMessage pMsg = (WampPublishMessage)msg;
		assertEquals("http://example.com/simple", pMsg.getTopicId());
		assertNull(pMsg.getEvent());
	}
		
	@Test
	public void serialize3() throws SerializationException{
		
		//it's set to NON_DEFAULT in WampSerialize to reducing data size. Maybe it shouldn"t
		serializer.getObjectMapper().getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);
		
		OutputWampPublishMessage msg = new OutputWampPublishMessage();
		msg.setTopicId("http://example.com/event#myevent2");
		
		SomeObject obj = new SomeObject();
		obj.setRand(0.09187032734575862);
		obj.setFlag(false);
		obj.setNum(23);
		obj.setName("Kross");
		
		Calendar cal = Calendar.getInstance();
		//Seriously month 0-based !!! ?
		cal.set(2012, 2, 29, 10, 41, 9);
		cal.set(Calendar.MILLISECOND, 864);

		obj.setCreated(cal.getTime()); 
		
		msg.setEvent(obj);
		
		assertEquals("[7,\"http://example.com/event#myevent2\","
						+	"{" 
		                +	  "\"rand\":0.09187032734575862,"
		                +	  "\"flag\":false,"
		                +	  "\"num\":23,"
		                +	  "\"name\":\"Kross\","
		                +	  "\"created\":\"2012-03-29T10:41:09.864Z\""
		               	+	"}]",
		             WampMessageSerializer.serialize(msg, serializer.getObjectMapper())
		);
	}
	
	@Test
	public void deserialize3() throws JsonParseException, SerializationException, IOException, ParseException{
		WampMessage msg = WampMessageDeserializer.deserialize(serializer.getObjectMapper().getJsonFactory().createJsonParser(
				"[7, \"http://example.com/event#myevent2\","
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
		assertEquals(WampMessage.PUBLISH, msg.getMessageType());
		
		WampPublishMessage pMsg = (WampPublishMessage)msg;
		assertEquals("http://example.com/event#myevent2", pMsg.getTopicId());
		
		Object event = pMsg.getEvent();
		if(event == null || !(event instanceof Map))
			fail("wrong event type");
		
		@SuppressWarnings("unchecked")
		Map<String,Object> evtObject = (Map<String,Object>)event;
		
		assertEquals(0.09187032734575862, evtObject.get("rand"));
		assertEquals(false, evtObject.get("flag"));
		assertEquals(23, evtObject.get("num"));
		assertEquals("Kross", evtObject.get("name"));
		
		Calendar cal = Calendar.getInstance();
		cal.set(2012, 2, 29, 10, 41, 9);
		cal.set(Calendar.MILLISECOND, 864);
		
		assertEquals(JsonDateSerializer.format.parse((String)evtObject.get("created")), cal.getTime());
	}
	
	@Test
	public void serialize4() throws SerializationException{
		OutputWampPublishMessage msg = new OutputWampPublishMessage();
		msg.setTopicId("event:myevent1");
		msg.setEvent("hello");
		msg.setExclude( Arrays.asList("NwtXQ8rdfPsy-ewS","dYqgDl0FthI6_hjb") );
		
		assertEquals("[7,\"event:myevent1\","
						+ "\"hello\","
						+ "[\"NwtXQ8rdfPsy-ewS\",\"dYqgDl0FthI6_hjb\"]]",
				WampMessageSerializer.serialize(msg, serializer.getObjectMapper()));
	}
	
	@Test
	public void deserialize4() throws JsonParseException, SerializationException, IOException{
		WampMessage msg = WampMessageDeserializer.deserialize(serializer.getObjectMapper().getJsonFactory().createJsonParser(
				"[7,\"event:myevent1\","
					+ "\"hello\","
					+ "[\"NwtXQ8rdfPsy-ewS\",\"dYqgDl0FthI6_hjb\"]]"));
				
		if( !(msg instanceof WampPublishMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.PUBLISH, msg.getMessageType());
		
		WampPublishMessage pMsg = (WampPublishMessage)msg;
		assertEquals("event:myevent1", pMsg.getTopicId());
		assertEquals("hello", pMsg.getEvent());
		
		List<String> excludeList = pMsg.getExclude();
		assertEquals(2, excludeList.size());
		assertEquals("NwtXQ8rdfPsy-ewS", excludeList.get(0));
		assertEquals("dYqgDl0FthI6_hjb", excludeList.get(1));
	}
	
	@Test
	public void serialize5() throws SerializationException{
		OutputWampPublishMessage msg = new OutputWampPublishMessage();
		msg.setTopicId("event:myevent1");
		msg.setEvent("hello");
		
		msg.setEligible(Arrays.asList("NwtXQ8rdfPsy-ewS"));
		
		assertEquals("[7,\"event:myevent1\","
						+ "\"hello\","
						+ "[],"
						+ "[\"NwtXQ8rdfPsy-ewS\"]]",
				WampMessageSerializer.serialize(msg, serializer.getObjectMapper()));
	}
	
	@Test
	public void deserialize5() throws JsonParseException, SerializationException, IOException{
		WampMessage msg = WampMessageDeserializer.deserialize(serializer.getObjectMapper().getJsonFactory().createJsonParser(
				"[7,\"event:myevent1\","
					+ "\"hello\","
					+ "[],"
					+ "[\"NwtXQ8rdfPsy-ewS\"]]"));
				
		if( !(msg instanceof WampPublishMessage) )
			fail("Wrong message type");
		assertEquals(WampMessage.PUBLISH, msg.getMessageType());
		
		WampPublishMessage pMsg = (WampPublishMessage)msg;
		assertEquals("event:myevent1", pMsg.getTopicId());
		assertEquals("hello", pMsg.getEvent());
		
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
	
	static private class JsonDateSerializer extends JsonSerializer<Date>{
		
		public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
		@Override
		public void serialize(Date value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException {
			
			jgen.writeString( format.format(value) );
		}
	}
}
