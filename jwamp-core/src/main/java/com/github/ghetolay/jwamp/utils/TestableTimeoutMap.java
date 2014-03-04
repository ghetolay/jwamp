/**
 * 
 */
package com.github.ghetolay.jwamp.utils;

import com.github.ghetolay.jwamp.utils.TimeoutMap.TimedOutListener;

/**
 * @author Kevin
 *
 */
public interface TestableTimeoutMap<K, V> {
	public void put(K key, V element, long timeout, TimedOutListener<K, V> listener);

	public String getMapDescription();

}
