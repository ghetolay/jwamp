package com.github.ghetolay.jwamp.utils;

import java.util.Map;

public interface TimeoutMap<K,V> extends Map<K,V>{

	public void put(K key, V element, long timeout);

	public static interface TimedOutListener<K,V>{
		public void timedOut(K key, V value);
	}
}
