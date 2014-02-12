package com.github.ghetolay.jwamp.endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;

import com.github.ghetolay.jwamp.WampFactory;
import com.github.ghetolay.jwamp.message.WampMessageDecoder;
import com.github.ghetolay.jwamp.message.output.WampMessageStreamEncoder;

public class WampServerEndpointConfig implements ServerEndpointConfig{

	private WampDispatcher dispatcher;
	private String path;
	
	private Configurator configurator = new Configurator();
	
	public WampServerEndpointConfig(WampDispatcher dispatcher, String path){
		this.dispatcher = dispatcher;
		
		//TODO check path
		this.path = path;
	}
	
	@Override
	public List<Class<? extends Encoder>> getEncoders() {
		//can't use Collections.singletonList() :'(
		List<Class<? extends Encoder>> encoders = new ArrayList<Class<? extends Encoder>>(1);
		encoders.add(WampMessageStreamEncoder.class);
		return encoders;
	}

	@Override
	public List<Class<? extends Decoder>> getDecoders() {
		//can't use Collections.singletonList() :'(
		List<Class<? extends Decoder>> decoders = new ArrayList<Class<? extends Decoder>>(1);
		
		//TODO @Jetty when using a stream decoder onMessage() is called with a Reader instead of a WampMessage as parameter
		//it work well for WampClientEndpoint...
		//decoders.add(WampMessageStreamDecoder.class);
		decoders.add(WampMessageDecoder.class);
		
		return decoders;
	}

	@Override
	public Map<String, Object> getUserProperties() {
		Map<String, Object> userProperties = new HashMap<String, Object>(1);
		userProperties.put("jwamp.dispatcher", dispatcher);
		return userProperties;
	}

	@Override
	public Class<?> getEndpointClass() {
		return WampServerEndpoint.class;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public List<String> getSubprotocols() {
		return Collections.singletonList(WampFactory.getProtocolName());
	}

	@Override
	public List<Extension> getExtensions() {
		return Collections.emptyList();
	}

	@Override
	public Configurator getConfigurator() {
		return configurator;
	}

}
