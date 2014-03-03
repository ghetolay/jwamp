/**
 * 
 */
package com.github.ghetolay.jwamp.actions;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kevin
 * @param <T>
 *
 */
public class ActionRegistry<T> implements ActionProvider<T> {

	private final ConcurrentHashMap<URI, T> actions = new ConcurrentHashMap<URI, T>();
	
	public ActionRegistry() {
	}

	@Override
	public T getAction(URI topicUri) {
		return actions.get(topicUri);
	}

	/**
	 * Registers the action as the new handler for the specified topic. If there is already a registered action, this will replace the existing action with the new action
	 * @param topicUri the topic to register a handler for
	 * @param action the action to register
	 * @return the old action, or null if there wasn't already an action registered for the topic
	 */
	public T registerAction(URI topicUri, T action){
		return actions.put(topicUri, action);
	}
	
	public T unregisterAction(URI topicUri){
		return actions.remove(topicUri);
	}
}
