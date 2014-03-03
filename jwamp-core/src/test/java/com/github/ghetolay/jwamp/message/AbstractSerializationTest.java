package com.github.ghetolay.jwamp.message;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.ghetolay.jwamp.message.WampMessageDecoder;
import com.github.ghetolay.jwamp.message.WampMessageEncoder;

public abstract class AbstractSerializationTest {

	protected WampMessageDecoder.TextStream decoder = new WampMessageDecoder.TextStream();
	protected WampMessageEncoder.TextStream encoder = new WampMessageEncoder.TextStream();
			
	protected static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	protected Calendar calendar;
	
	@Before
	public void init(){
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	
//		//TODO encoder/decoder init is probably not required anymore
//		ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();
//		config.getUserProperties().put(JsonFactory.class.getName(), wampEncodersImpl.getJsonFactory());
//		
//		encoder.init(config);
//		decoder.init(config);
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
