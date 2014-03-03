package com.github.ghetolay.test.jwamp.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

import com.github.ghetolay.jwamp.WampBuilder;

public class ApplicationConfig implements ServerApplicationConfig{

	@Override
	public Set<ServerEndpointConfig> getEndpointConfigs(
			Set<Class<? extends Endpoint>> endpointClasses) {
		
		try{
			
			ServerEndpointConfig endpointConfig = WampBuilder.create()
					.withCallAction(new URI("http://example.com/echo"), new EchoAction())
					.forServer("wamp")
					.createEndpointConfig();
					
			return new HashSet<ServerEndpointConfig>(Arrays.asList(endpointConfig));
		

		} catch (URISyntaxException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
	}

	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
		return Collections.emptySet();
	}

}
