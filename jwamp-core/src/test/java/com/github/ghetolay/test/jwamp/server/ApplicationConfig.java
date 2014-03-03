package com.github.ghetolay.test.jwamp.server;

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
		
		ServerEndpointConfig endpointConfig = WampBuilder.create()
				.withCallAction(EchoAction.uri, new EchoAction())
				.forServer("wamp")
				.createEndpointConfig();
				
		return new HashSet<ServerEndpointConfig>(Arrays.asList(endpointConfig));

	}

	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
		return Collections.emptySet();
	}

}
