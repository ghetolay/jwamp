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

import java.util.HashMap;
import java.util.Map;

public class SimpleActionMapping<T> {

	private Map<String,T> actions;

	public SimpleActionMapping(){
		actions = new HashMap<String, T>();
	}
	
	public SimpleActionMapping(Map<String,T> actions){
		this.actions = actions;
	}
	
	public void addAction(String actionId, T action){
		actions.put(actionId, action);
	}
	
	public void removeAction(String actionId){
		actions.remove(actionId);
	}

	public T getAction(String actionId) {
		return actions.get(actionId);
	}
}
