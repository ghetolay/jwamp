package com.github.ghetolay.test.jwamp.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import javax.websocket.EncodeException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.ghetolay.jwamp.UnsupportedWampActionException;
import com.github.ghetolay.jwamp.WampFactory;
import com.github.ghetolay.jwamp.WampWebSocket;
import com.github.ghetolay.jwamp.endpoint.DefaultSessionManager;
import com.github.ghetolay.jwamp.endpoint.WampBuilder;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.rpc.CallException;

@RunWith(Arquillian.class)
public class SimpleTest {

	WampWebSocket wamp;

	@Deployment(testable = false)
	public static WebArchive deployment(){
		return ShrinkWrap.create(WebArchive.class, "test.war")
				.addAsLibraries(
						ShrinkWrap.create(JavaArchive.class, "jwamp.jar")
							.addPackages(true, WampFactory.class.getPackage()))
				.addPackage("com.github.ghetolay.test.jwamp.server");
	}
	
	@Before
	public void init() throws URISyntaxException, Exception{
		wamp = WampFactory.getInstance().connect(new URI("ws://localhost:8080/test/wamp"), 50000, true, 
													WampBuilder.Dispatcher.newDefaultDispatcher(new DefaultSessionManager())
																			.addRPCSender()
																			.addEventsubscriber()
																			.build()
);
	}
	
	@Test
	@RunAsClient
	public void callTest() throws UnsupportedWampActionException, IOException, TimeoutException, EncodeException, CallException, URISyntaxException{
		String arg = "EchoTest";
		WampArguments result = wamp.simpleCall(new URI("http://example.com/echo"), arg);

		assertNotNull(result);
		assertTrue(result.hasNext());
		assertEquals("EchoTest", result.nextObject(String.class));
	}
}
