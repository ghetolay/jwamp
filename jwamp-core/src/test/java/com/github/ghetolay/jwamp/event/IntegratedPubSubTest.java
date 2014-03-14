/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampBuilder;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.session.WampSession;
import com.github.ghetolay.jwamp.utils.Promise;
import com.github.ghetolay.jwamp.utils.URIBuilder;
import com.github.ghetolay.testutils.EchoAction;

/**
 * @author Kevin
 *
 */
public class IntegratedPubSubTest {
	
	private static final Logger log = LoggerFactory.getLogger(IntegratedPubSubTest.class);
	
	static Server server;
	static URI serverUri;
	
	TestEventAction actionA = new TestEventAction();
	TestEventAction actionB = new TestEventAction();
	TestEventAction actionC = new TestEventAction();
	WampSession sessionA;
	WampSession sessionB;
	WampSession sessionC;;
	
	private static final CloseReason endOfTestReason = new CloseReason(CloseCodes.GOING_AWAY, "End of test");
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		server = new Server(0);
		ServerConnector connector = new ServerConnector(server);
		server.setConnectors(new Connector[]{connector});
		
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/");
		webAppContext.setWar("./");
		
		webAppContext.setServer(server);
		server.setHandler(webAppContext);

		ServerContainer serverContainer = WebSocketServerContainerInitializer.configureContext(webAppContext);
		try {
			ServerEndpointConfig c = WampBuilder.create()
					.withCallAction(EchoAction.uri, new EchoAction())
					.forServer("/test")
					.createEndpointConfig();
			
			serverContainer.addEndpoint(c);
			
		} catch (DeploymentException e1) {
			e1.printStackTrace();
		}

		server.setStopAtShutdown(true);

		try{
			server.start();
			
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}
		
		serverUri = URIBuilder.newURI("ws://localhost:" + connector.getLocalPort() + "/test");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		server.stop();
		try {
			server.join();
		} catch (InterruptedException e) {
			log.warn(e.getMessage(), e);
		}
	}

	@Before
	public void setUp() throws Exception {
		if (sessionA != null) sessionA.close(endOfTestReason);
		if (sessionB != null) sessionB.close(endOfTestReason);
		if (sessionC != null) sessionC.close(endOfTestReason);
		
		actionA.reset();
		actionB.reset();
		actionC.reset();

		sessionA = WampBuilder.create()
				.withEventSubscription(TestEventAction.testUri, actionA)
				.forClient()
				.connectToServer(serverUri);
		
		sessionB = WampBuilder.create()
				.withEventSubscription(TestEventAction.testUri, actionB)
				.forClient()
				.connectToServer(serverUri);
		
		sessionC = WampBuilder.create()
				.withEventSubscription(TestEventAction.testUri, actionC)
				.forClient()
				.connectToServer(serverUri);
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSimplePublish() throws Throwable {
		String testValue = "A test value";
		
		sessionA.getEventPublisher().publishEvent(TestEventAction.testUri, testValue);

		assertEquals(testValue, actionA.getResult());
		
	}

	@Test
	public void testFullBroadcast() throws Throwable {

		String testValue = "A test value";
		
		sessionA.getEventPublisher().publishEvent(TestEventAction.testUri, testValue);

		assertEquals(testValue, actionA.getResult());
		assertEquals(testValue, actionB.getResult());
		assertEquals(testValue, actionC.getResult());
	}
	
	@Test
	public void testExplicitExclude() throws Throwable {

		String testValue = "A test value";
		
		sessionA.getEventPublisher().publishEvent(TestEventAction.testUri, testValue, Arrays.asList(sessionB.getWampSessionId()));

		assertEquals(testValue, actionA.getResult());
		assertEquals(null, actionB.getResult());
		assertEquals(testValue, actionC.getResult());
	}

	@Test
	public void testExcludeMe() throws Throwable {

		String testValue = "A test value";
		
		sessionA.getEventPublisher().publishEvent(TestEventAction.testUri, testValue, true);

		assertEquals(null, actionA.getResult());
		assertEquals(testValue, actionB.getResult());
		assertEquals(testValue, actionC.getResult());
	}	

	@Test
	public void testEligible() throws Throwable {

		String testValue = "A test value";
		
		sessionA.getEventPublisher().publishEvent(TestEventAction.testUri, testValue, Arrays.<String>asList(),Arrays.asList(sessionB.getWampSessionId()));

		assertEquals(null, actionA.getResult());
		assertEquals(testValue, actionB.getResult());
		assertEquals(null, actionC.getResult());
	}	
	
	
	
	private static class TestEventAction implements EventAction {

		static public final URI testUri = URIBuilder.newURI("event://testevent");		
		
		Promise<String> response = new Promise<String>();
		
		@Override
		public void handleEvent(WampSession session, WampEventMessage msg) {
			response.setValue(msg.getEvent().getAs(String.class));
		}
		
		public String getResult() throws InterruptedException{
			try {
				return response.get(100);
			} catch (TimeoutException e) {
				return null;
			}
		}
		
		public void reset(){
			response = new Promise<String>();
		}
	}
	
}
