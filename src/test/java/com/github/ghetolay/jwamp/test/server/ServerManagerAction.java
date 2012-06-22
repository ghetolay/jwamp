package com.github.ghetolay.jwamp.test.server;

import com.github.ghetolay.jwamp.rpc.CallAction;

public class ServerManagerAction implements CallAction{

	public Object execute(Object[] args) {
		try{
			String arg = (String) args[0];
			if("restart".equals(arg)){
					try{
						TestServer.server.stop();
					}catch(Exception e){}
					
					TestServer.server.start();
				
			}else if("shutdown".equals(arg)){
				System.out.println("RECEIVE SHUTDOWN");

				try{
					TestServer.server.stop();
				}catch(Exception e){}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
