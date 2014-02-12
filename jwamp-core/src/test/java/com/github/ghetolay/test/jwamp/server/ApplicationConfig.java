package com.github.ghetolay.test.jwamp.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

import com.github.ghetolay.jwamp.endpoint.DefaultSessionManager;
import com.github.ghetolay.jwamp.endpoint.WampBuilder;
import com.github.ghetolay.jwamp.endpoint.WampServerEndpointConfig;

public class ApplicationConfig implements ServerApplicationConfig{

	@Override
	public Set<ServerEndpointConfig> getEndpointConfigs(
			Set<Class<? extends Endpoint>> endpointClasses) {
		
		HashSet<ServerEndpointConfig> endpoints = new HashSet<ServerEndpointConfig>(1);
		
		try {
			endpoints.add(
			new WampServerEndpointConfig( WampBuilder.Dispatcher.newDefaultDispatcher(new DefaultSessionManager())
																.newRPCManager()
																	.addAction(new URI("http://example.com/echo"), new EchoAction())
																	.build()
																.build(),
										  "wamp"));
			return endpoints;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Collections.emptySet();
		}
	}

	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
		return Collections.emptySet();
	}

}
