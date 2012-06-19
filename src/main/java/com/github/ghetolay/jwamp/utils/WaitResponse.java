package com.github.ghetolay.jwamp.utils;


import java.util.concurrent.Callable;

public class WaitResponse<T> implements Callable<T>, ResultListener<T>{

	private T result;
	private long waitfor = 0;
	
	public WaitResponse(){}
	
	public WaitResponse(long timeout){
		this.waitfor = timeout;
	}
	
	public void onResult(T result) {
		setResult(result);
	}
	
	public synchronized void setResult(T result) {
		this.result = result;
		this.notify();
	}

	public synchronized T call() throws Exception {
		wait(waitfor);		
		return result;
	}
	
}
