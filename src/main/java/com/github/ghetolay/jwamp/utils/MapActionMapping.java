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
