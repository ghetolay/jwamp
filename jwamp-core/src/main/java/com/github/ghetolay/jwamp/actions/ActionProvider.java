/**
 * 
 */
package com.github.ghetolay.jwamp.actions;

import java.net.URI;

/**
 * @author Kevin
 *
 */
public interface ActionProvider<T> {
	public T getAction(URI uri);
}
