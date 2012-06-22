package com.github.ghetolay.jwamp.test.client;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.github.ghetolay.jwamp.DefaultWampParameter;
import com.github.ghetolay.jwamp.UnsupportedWampActionException;
import com.github.ghetolay.jwamp.WampWebSocket;
import com.github.ghetolay.jwamp.event.DefaultEventSubscriber.EventResult;
import com.github.ghetolay.jwamp.jetty.WampJettyFactory;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class TestClient {
	
	protected static final Logger log = LoggerFactory.getLogger(TestClient.class);
	
	static boolean disconnected = false;
	static boolean resulted = false;
	
	static int waitEventResponseTimeout = 10000;
	static WaitThread waitEventResponse = new WaitThread(waitEventResponseTimeout);
	
	static int waitAfterRestartTimeout = 20000;
	static WaitThread waitAfterRestart = new WaitThread(waitAfterRestartTimeout);
	
	static WampWebSocket wamp;
	
	@Test
	public void connect(){
		try{
			WampJettyFactory wampFact = WampJettyFactory.getInstance();
			
			wampFact.setWampParameter(new DefaultWampParameter.SimpleClientParameter(getClass().getResourceAsStream("/wamp-client.xml"), getEventListener()));
			wamp = wampFact.connect(new URI("ws://localhost:8080"));
			
			waitEventResponse.start();
			
		}catch(Exception e){
			fail(e.getMessage());
		}
	}
	
	public ResultListener<EventResult> getEventListener(){
		return new ResultListener<EventResult>() {
			
			public void onResult(EventResult result) {
				if(!resulted && "EventTest".equals(result.getTopicId()) && "EventAction".equals(result.getEvent())){
					synchronized(waitEventResponse){
						waitEventResponse.done = true;
						waitEventResponse.notifyAll();
					}
					resulted = true;
				}else if( disconnected && "EventTest".equals(result.getTopicId()) && "EventAction".equals(result.getEvent())){		
					try{
						synchronized(waitAfterRestart){
							waitAfterRestart.done = true;	
							waitAfterRestart.notifyAll();
						}
					}catch(Exception e){
						e.printStackTrace();
					}
					disconnected = false;
				}
			}
		};
	}
	
	@Test(dependsOnMethods = {"connect"})
	public void testAutoSubscribeResponse() throws InterruptedException{
		waitEventResponse.join();
		
		assertTrue("Automatic Event Subscription",waitEventResponse.done);
	}
	
	@Test(dependsOnMethods = {"connect"})
	public void testSimpleCall() throws IOException, UnsupportedWampActionException{
		WampCallResultMessage msg = wamp.call("CallTest", null, 10000);

		boolean succeed = msg.messageType != WampMessage.CALLERROR && "SUCCEED".equals(msg.getResult());
		assertTrue("Simple Remote Call",succeed);
	}
	
	@AfterClass()
	public void shutdownServer() throws IOException, UnsupportedWampActionException{		
		//wamp.call("Manage", "shutdown", 1, null);
		wamp.getConnection().close(10000, "");
	}
	
	@Test(dependsOnMethods = {"testAutoSubscribeResponse"})
	public void testAutoReconnectAutoResubscribe() throws IOException, UnsupportedWampActionException, InterruptedException{
		wamp.call("Manage", "restart", 1, null);
		
		disconnected = true;
		waitAfterRestart.start();
		waitAfterRestart.join();
		
		assertTrue("Automatic Event Subscription on reconnect",waitAfterRestart.done);
	}
}
