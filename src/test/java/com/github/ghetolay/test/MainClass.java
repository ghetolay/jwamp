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
package com.github.ghetolay.test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.github.ghetolay.jwamp.UnsupportedWampActionException;
import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.test.client.TestClient;
import com.github.ghetolay.jwamp.test.server.TestServer;

/**
 * @author ghetolay
 *
 */
public class MainClass {

	public static void main(String[] args){
		
		new TestServer().test();
		
		TestClient client = new TestClient();
		
		client.connect();
		
		try {
	
			client.callArrayArg();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedWampActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}
