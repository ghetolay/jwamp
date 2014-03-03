/**
 * 
 */
package com.github.ghetolay.jwamp.actions;

import java.net.URI;

/**
 * @author Kevin
 *
 */
public class ActionRegistration<T> {

	private final URI uri;
	private final T action;
	/**
	 * 
	 */
	public ActionRegistration(URI uri, T action) {
		this.uri = uri;
		this.action = action;
	}

	public URI getUri() {
		return uri;
	}
	
	public T getAction() {
		return action;
	}
}
