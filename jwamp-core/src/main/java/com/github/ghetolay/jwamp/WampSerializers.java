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
package com.github.ghetolay.jwamp;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MappingJsonFactory;

/**
 * Will also include msgPack configuration on Wamp v2
 * 
 * @author ghetolay
 *
 */
public class WampSerializers {
	
	private JsonFactory jsonFactory;
	
	public WampSerializers(){
		jsonFactory = new MappingJsonFactory();
	}
	
	public JsonFactory getJsonFactory(){		
		return jsonFactory;
	}

	/**
	 * /!\ Must be a MappingJsonFactory in order to provide object binding 
	 * 
	 * @param jsonFactory
	 */
	public void setMapper(JsonFactory jsonFactory) {
		this.jsonFactory = jsonFactory;
	}
}
