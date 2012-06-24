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
package com.github.ghetolay.jwamp.test.server;

import java.io.InputStream;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.github.ghetolay.jwamp.DefaultWampParameter;
import com.github.ghetolay.jwamp.jetty.WampJettyFactory;
import com.github.ghetolay.jwamp.jetty.WampJettyHandler;

public class TestServer {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	public static Server server;
	
	@Test
	public void test(){

		server = new Server(8080);
		
		WampJettyFactory wampFact = WampJettyFactory.getInstance();
		
		try{
			InputStream is = getClass().getResourceAsStream("/wamp-server.xml");
			WampJettyHandler wampHandler = wampFact.newJettyHandler(new DefaultWampParameter.SimpleServerParameter(is));
			
			server.setHandler(wampHandler);
	
			server.start();
			//server.join();
		}catch(InterruptedException e){
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
