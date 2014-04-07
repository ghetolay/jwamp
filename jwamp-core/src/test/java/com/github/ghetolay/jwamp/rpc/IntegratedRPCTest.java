/**
 * 
 */
package com.github.ghetolay.jwamp.rpc;

import static org.junit.Assert.assertEquals;

import java.net.URI;

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
import com.github.ghetolay.jwamp.session.WampSession;
import com.github.ghetolay.jwamp.utils.ObjectHolder;
import com.github.ghetolay.jwamp.utils.URIBuilder;
import com.github.ghetolay.testutils.EchoAction;

/**
 * @author Kevin
 *
 */
public class IntegratedRPCTest {
	
	private static final Logger log = LoggerFactory.getLogger(IntegratedRPCTest.class);
	
	static Server server;
	static WampSession serverSession;
	static URI serverUri;
	
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
		
		serverUri = URIBuilder.uri("ws://localhost:" + connector.getLocalPort() + "/test");
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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEcho() throws Throwable {
		WampSession clientSession = WampBuilder.create().forClient().connectToServer(serverUri);
		ObjectHolder rslt = clientSession.getRpcSender().callSynchronously(EchoAction.uri, 5000, "A test");
		assertEquals("A test", rslt.getAs(String.class));
		
		clientSession.close(new CloseReason(CloseCodes.GOING_AWAY, "test finished"));
	}

}
