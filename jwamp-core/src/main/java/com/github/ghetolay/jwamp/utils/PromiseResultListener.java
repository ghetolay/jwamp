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



/**
 * A {@link ResultListener} that blocks until the result is returned
 *
 * @param <T> the type of the result
 */
public class PromiseResultListener<T> implements ResultListener<T>{

	private final Promise<T> promise;
	
	/**
	 * Creates a WaitResponse that will wait indefinitely for the result 
	 */
	public PromiseResultListener(Promise<T> promise){
		this.promise = promise;
	}
	
	/**
	 * Part of the {@link ResultListener} interface - called by the framework when the result is available
	 */
	public void onResult(T result) {
		promise.setValue(result);
	}
	
}
