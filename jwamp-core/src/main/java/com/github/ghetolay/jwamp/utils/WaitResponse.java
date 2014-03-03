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

/**
 * A {@link ResultListener} that blocks until the result is returned
 *
 * @param <T> the type of the result
 */
public class WaitResponse<T> implements Callable<T>, ResultListener<T>{

	private T result;
	private long waitfor = 0;
	
	/**
	 * Creates a WaitResponse that will wait indefinitely for the result 
	 */
	public WaitResponse(){}
	
	/**
	 * Creates a WaitResponse with the specified timeout duration
	 * @param timeout
	 */
	public WaitResponse(long timeout){
		this.waitfor = timeout;
	}
	
	/**
	 * Part of the {@link ResultListener} interface - called by the framework when the result is available
	 */
	public void onResult(T result) {
		setResult(result);
	}
	
	/**
	 * Sets the result and notifies any waiting threads that it is available
	 * @param result
	 */
	private synchronized void setResult(T result) {
		this.result = result;
		this.notifyAll();
	}

	/**
	 * Blocks until the result is available, or the timeout has elapsed.
	 * @return the result value, or null if a timeout happened
	 */
	public synchronized T call() throws InterruptedException {
		wait(waitfor);		
		return result;
	}
	
}
