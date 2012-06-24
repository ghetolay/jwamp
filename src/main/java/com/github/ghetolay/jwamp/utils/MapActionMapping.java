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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.github.ghetolay.jwamp.event.EventAction;

public class MapActionMapping<T> implements ActionMapping<T> {

	Map<String,T> actions;
	
	public MapActionMapping(){
		actions = new HashMap<String,T>();
	}
	
	public MapActionMapping( Map<String,T> actions){
		this.actions = actions;
	}
	
	public T getAction(String actionId){
		return actions.get(actionId);
	}

	public Iterator<T> getActionsIterator(){
		return actions.values().iterator();
	}

	public String getActionId(EventAction action) {
		 for (Entry<String, T> entry : actions.entrySet())
			 if (entry.getValue().equals(action))
		            return entry.getKey();
	
		 return null;
	}
}
