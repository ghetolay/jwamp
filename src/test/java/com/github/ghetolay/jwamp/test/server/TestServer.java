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
