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


import java.util.concurrent.TimeoutException;

/**
 * A promise to provide some value in the future.  Users call get() to retrieve the value.
 *
 * @param <T> the type of the result
 */
public class Promise<T> {

	private T value;
	
	/**
	 * Sets the result and notifies any waiting threads that it is available
	 * @param result
	 */
	public synchronized void setValue(T value) {
		this.value = value;
		this.notifyAll();
	}

	/**
	 * Blocks until the result is available, or the timeout has elapsed.
	 * @return the result value, or null if a timeout happened
	 */
	public synchronized T get(long waitfor) throws InterruptedException, TimeoutException {
		wait(waitfor);
		if (value == null) throw new TimeoutException();
		return value;
	}
	
}
