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
		this.notifyAll();
	}

	public synchronized T call() throws Exception {
		wait(waitfor);		
		return result;
	}
	
}
