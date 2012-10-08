/**
*Copyright [2012] [Ghetolay]
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package com.github.ghetolay.jwamp.test.client;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.github.ghetolay.jwamp.DefaultWampParameter;
import com.github.ghetolay.jwamp.UnsupportedWampActionException;
import com.github.ghetolay.jwamp.WampConnection.ReconnectPolicy;
import com.github.ghetolay.jwamp.WampWebSocket;
import com.github.ghetolay.jwamp.event.DefaultEventSubscriber.EventResult;
import com.github.ghetolay.jwamp.jetty.WampJettyFactory;
import com.github.ghetolay.jwamp.message.ReadableWampArrayObject;
import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.test.server.SomeObject;
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
			wamp = wampFact.connect(new URI("ws://localhost:8080"), 1000, 1000, ReconnectPolicy.YES);
			
			waitEventResponse.start();
			
		}catch(Exception e){
			log.error("Connection Error", e);
			fail(e.getMessage());
		}
	}
	
	public ResultListener<EventResult> getEventListener(){
		return new ResultListener<EventResult>() {
			
			public void onResult(EventResult result) {				
				if(!resulted && "EventTest".equals(result.getTopicId()) && "EventAction".equals(result.getEvent().nextObject(String.class))){
					synchronized(waitEventResponse){
						waitEventResponse.done = true;
						waitEventResponse.notifyAll();
					}
					resulted = true;
				}else if( disconnected && "EventTest".equals(result.getTopicId()) && "EventAction".equals(result.getEvent().nextObject(String.class))){		
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
	public void callSimple() throws IOException, UnsupportedWampActionException, TimeoutException, SerializationException{
		ReadableWampArrayObject msg = wamp.call("CallTest");

		assertNotNull("Simple Remote Call",msg);
	}
	
	@Test(dependsOnMethods = {"connect"})
	public void callReturnOneList() throws IOException, UnsupportedWampActionException, TimeoutException, SerializationException{
		ReadableWampArrayObject msg = wamp.call("oneList");
		
		List<String> list = new ArrayList<String>();
		list.add("lol");
		list.add("prout");
		list.add("youk");
		
		assertEquals("lol",   msg.nextObject(String.class));
		assertEquals("prout", msg.nextObject(String.class));
		assertEquals("youk",  msg.nextObject(String.class));
		assertNull(msg.nextObject());
	}
	
	@Test(dependsOnMethods = {"connect"})
	public void callSingleReturn() throws IOException, UnsupportedWampActionException, TimeoutException, SerializationException{
		ReadableWampArrayObject msg = wamp.call("singleReturn");
		
		assertEquals(Integer.valueOf(1),msg.nextObject(Integer.class));
		assertNull(msg.nextObject());
	}
	
	
	@Test(dependsOnMethods = {"connect"})
	public void callMultipleArgumentsAndReturnsType() throws IOException, UnsupportedWampActionException, TimeoutException, SerializationException{
		SomeObject obj = new SomeObject();
		obj.setFieldOne("b");
		obj.setFieldTwo(2);
		
		ReadableWampArrayObject msg = wamp.call("echo", "a", 45, obj, "a");

		assertEquals(  "a" ,  msg.nextObject(String.class)             );
		assertEquals(  45  ,  msg.nextObject(Integer.class).intValue() );
		assertEquals(  obj ,  msg.nextObject(SomeObject.class)         );
		assertEquals(  "a" ,  msg.nextObject(String.class)             );

		assertNull(msg.nextObject());
	}
	
	@AfterClass()
	public void shutdownServer() throws IOException, UnsupportedWampActionException, SerializationException{		
		try {
			wamp.call("Manage", 1,"shutdown");
		} catch (TimeoutException e){}
		
		wamp.getConnection().close(10000, "");
	}
	
	@Test(dependsOnMethods = {"testAutoSubscribeResponse"})
	public void testAutoReconnectAutoResubscribe() throws IOException, UnsupportedWampActionException, InterruptedException, SerializationException{
		wamp.call("Manage", 1, null, "restart");
		
		disconnected = true;
		waitAfterRestart.start();
		waitAfterRestart.join();
		
		assertTrue("Automatic Event Subscription on reconnect",waitAfterRestart.done);
	}
}
