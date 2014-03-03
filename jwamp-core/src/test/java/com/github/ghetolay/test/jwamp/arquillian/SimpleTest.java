package com.github.ghetolay.test.jwamp.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.ghetolay.jwamp.WampBuilder;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.CallException;
import com.github.ghetolay.jwamp.session.WampSession;
import com.github.ghetolay.jwamp.utils.JsonBackedObject;

@RunWith(Arquillian.class)
public class SimpleTest {

	WampSession session;

	@Deployment(testable = false)
	public static WebArchive deployment(){
		return ShrinkWrap.create(WebArchive.class, "test.war")
				.addAsLibraries(
						ShrinkWrap.create(JavaArchive.class, "jwamp.jar")
							.addPackages(true, WampBuilder.class.getPackage()))
				.addPackage("com.github.ghetolay.test.jwamp.server");
	}
	
	@Before
	public void init() throws URISyntaxException, Exception{
		
		URI serverUri = new URI("ws://localhost:8080/test/wamp");

		session = WampBuilder.create()
				.forClient()
				.withConnectionTimeout(3000)
				.connectToServer(serverUri);
		
//		wamp = WampFactory.getInstance().connect(new URI("ws://localhost:8080/test/wamp"), 50000, true, 
//													WampBuilder.Dispatcher.newDefaultDispatcher(new DefaultSessionManager())
//																			.addRPCSender()
//																			.addEventsubscriber()
//																			.build()
//);
	}
	
	@Test
	@RunAsClient
	public void callTest() throws Exception, CallException{
		String arg = "EchoTest";
		JsonBackedObject result = session.getRpcSender().callSynchronously(new URI("http://example.com/echo"), 1000, arg);

		assertNotNull(result);
		assertEquals("EchoTest", result.getAs(String.class));
	}
	
	private static class EchoAction implements CallAction{

		@Override
		public WampCallResultMessage handleCall(WampSession session, WampCallMessage msg) throws CallException {
			return WampCallResultMessage.create(msg.getCallId(), msg.getArgs().get(0));
		}
		
	}
}
