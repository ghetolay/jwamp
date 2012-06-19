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
