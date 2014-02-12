package com.github.ghetolay.test.jwamp.serialization;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.websocket.ClientEndpointConfig;

import org.junit.Before;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.ghetolay.jwamp.WampSerializers;
import com.github.ghetolay.jwamp.message.WampMessageStreamDecoder;
import com.github.ghetolay.jwamp.message.output.WampMessageStreamEncoder;

public abstract class AbstractSerializationTest {

	protected WampSerializers wampEncodersImpl = new WampSerializers();
	
	protected WampMessageStreamEncoder encoder = new WampMessageStreamEncoder();
	protected WampMessageStreamDecoder decoder = new WampMessageStreamDecoder();
			
	protected static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	protected Calendar calendar;
	
	@Before
	public void init(){
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		
		ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();
		config.getUserProperties().put("jwamp.jsonfactory", wampEncodersImpl.getJsonFactory());
		
		encoder.init(config);
		decoder.init(config);
	}

	public static class JsonDateSerializer extends JsonSerializer<Date>{
		
		@Override
		public void serialize(Date value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException {
			
			jgen.writeString( dateFormat.format(value) );
		}
	}
}
