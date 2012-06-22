package com.github.ghetolay.jwamp.test.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WaitThread extends Thread{
	
	protected static final Logger log = LoggerFactory.getLogger(TestClient.class);
	
	public boolean done = false;
	public long waitFor = 0;
	
	public WaitThread(long waitFor){
		this.waitFor = waitFor;
	}
	
	public synchronized void run(){
		try{
			wait(waitFor);
		}catch(Exception e){
			log.debug("WaitThread Exception",e);
			done = false;
		}
	}
}
