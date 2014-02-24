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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * A cache that allows specifying a timeout for entries.  Entries are evicted at some point after their timeout expires.
 * There is no guarantee that eviction will happen exactly at the timeout, or even somewhat near the timeout. 
 * 
 * This class is threadsafe.
 *
 * @param <K> the type of the key for the cache
 * @param <V> the type of value that will be stored in the cache
 */
public class TimeoutHashMap<K,V>{

	/**
	 * Stores the values and other information needed to maintain the cache entries
	 */
	private final ConcurrentHashMap<K, TimeoutElement<K,V>> map = new ConcurrentHashMap<K, TimeoutElement<K,V>>();
	
	/**
	 * Manages items that need to be timedout
	 */
	private final DelayQueue<TimeoutElement<K,V>> delayQueue = new DelayQueue<TimeoutElement<K,V>>();
	
	/**
	 * A timeout listener that does nothing (used if the caller doesn't want to be notified on a cache timeout eviction)
	 */
	private final NoOpTimeoutListener<K,V> noOpListener = new NoOpTimeoutListener<K,V>();
	
	public TimeoutHashMap() {
	}

	/**
	 * Adds a value to the cache
	 * @param key the key to use to retrieve the item from the cache in the future
	 * @param value the value to store in the cache
	 * @param timeoutMillis the time that the value will be in the cache before eviction
	 */
	public void put(K key, V value, long timeoutMillis){
		put(key, value, timeoutMillis, noOpListener);
	}
	
	/**
	 * Adds a value to the cache, and allows registration of a listener that will be called if a timeout eviction of this value occurs
	 * @param key the key to use to retrieve the item from the cache in the future
	 * @param value the value to store in the cache
	 * @param timeoutMillis the time that the value will be in the cache before eviction.  If 0, this entry will not be evicted from the cache because of a timeout.
	 * @param timeoutListener the listener that will be notified if a timeout eviction occurs
	 */
	public void put(K key, V value, long timeoutMillis, TimeoutListener<K, V> timeoutListener){
		TimeoutElement<K, V> element = new TimeoutElement<K,V>(key, value, timeoutListener, timeoutMillis);
		map.put(key, element);
		
		if(timeoutMillis > 0)
			delayQueue.offer(element);
		
		pollTimeouts();
	}

	/**
	 * Retrieves the value from the cache, or null if the value is no longer in the cache
	 * @param key the key identifying the cache entry
	 * @return the value from the cache, or null if the value is not in the cache
	 */
	public V get(K key){
		pollTimeouts();
		
		TimeoutElement<K, V> element = map.get(key);
		if (element == null)
			return null;
		
		return element.getValue();
	}
	
	/**
	 * Removes the specified value from the cache
	 * @param key the key for the value that needs to be removed from the cache
	 * @return the value that was removed, or null if the value was no longer in the cache
	 */
	public V remove(K key){
		TimeoutElement<K, V> element = map.remove(key);
		if (element == null)
			return null;
		
		element.markRemoved();
		
		pollTimeouts();
		
		return element.getValue();
	}


	/**
	 * @return the number of elements in the cache
	 */
	public int size(){
		return map.size();
	}
	
	/**
	 * Used for testing and diagnostics only
	 * @return the size of the delay queue
	 */
	int delayQueueSize(){
		return delayQueue.size();
	}
	
	/**
	 * Causes the cache to remove any entries that have timed out, and notify a timeout eviction listener for any entries that are removed
	 * This call is made internally, and can also be called by an external thread (i.e. a dedicated cleaner thread) if desired.
	 * TODO: create a blocking version of this so that a cleaner thread can just poll in a loop (or maybe just have the method block itself, so it becomes a take() instead of a poll()).
	 * TODO: the check of the queuesize vs mapsize shouuld probably be broken out into a different private method that we call during put() and remove()
	 */
	public void pollTimeouts(){
		TimeoutElement<K, V> element;
		while((element = delayQueue.poll()) != null){
			if (!element.isRemoved()){ // Concurrency note: there is a race condition on 'removed' - there is a small chance that it will return false even if it has been removed.  No big deal, the map will return null
				TimeoutElement<K, V> removed = map.remove(element.getKey());
				if (removed != null){
					removed.markRemoved();
					removed.notifyTimeoutListener();
				}
			}
		}
		
		// memory optimization - if the queue is too big, let's try to flush anything from it that has already been removed from the map.
		// if this threshold is too small, it will impact performance
		// if this threshold is too big, it will impact memory consumption when under high load
		// we'll say that the queue can't be more than twice the size of the map
		int queueSize = delayQueue.size();
		int mapSize = map.size();
		if (queueSize > 100 && queueSize > (mapSize << 1)){
			Iterator<TimeoutElement<K, V>> it = delayQueue.iterator();
			while (it.hasNext()){
				if (it.next().isRemoved())
					it.remove();
			}
		}
		
	}
	
	/**
	 * Listener interface for timeout cache evictions
	 * 
	 * @param <K> the type of the key associated with the value in the cache
	 * @param <V> the type of the value stored in the cache
	 */
	public static interface TimeoutListener<K,V>{
		public void timedOut(K key, V value);
	}
	
	/**
	 * A {@link TimeoutListener} that does nothing
	 * @param <K> the type of the key associated with the value in the cache
	 * @param <V> the type of the value stored in the cache
	 */
	private static class NoOpTimeoutListener<K, V> implements TimeoutListener<K,V>{

		@Override
		public void timedOut(K key, V value) {
			// do nothing
		}
		
	}
	
	/**
	 *
	 * Tracks state and other important information about values stored in the cache
	 *
	 * @param <K> the type of the key associated with the value in the cache
	 * @param <V> the type of the value stored in the cache
	 */
	private static class TimeoutElement<K,V> implements Delayed {
		/**
		 * The time that the entry will expire
		 */
		private final long expiryTimeMillis;
		/**
		 * The key
		 */
		private final K key;
		/**
		 * The value
		 */
		private final V value;
		/**
		 * The listener that should be notified if a timeout eviction occurs
		 */
		private final TimeoutListener<K, V> timeoutListener;
		/**
		 * State variable that tracks whether the entry has been removed from the cache (i.e. via the {@link TimeoutHashMap#remove(Object)} method)
		 */
		private boolean removed = false;
		
		public TimeoutElement(K key, V value, TimeoutListener<K, V> timeoutListener, long delayMillis) {
			this.key = key;
			this.value = value;
			this.timeoutListener = timeoutListener;
			this.expiryTimeMillis = delayMillis > 0 ? System.currentTimeMillis() + delayMillis : 0;
			
		}
		
		public boolean isRemoved() {
			return removed;
		}
		
		public void markRemoved() {
			this.removed = true;
		}
		
		public K getKey() {
			return key;
		}
		
		public V getValue() {
			return value;
		}
		
		public void notifyTimeoutListener(){
			timeoutListener.timedOut(key, value);
		}

		/**
		 * Compares the relative expiration. This does not call {@link System#currentTimeMillis()}
		 */
		@Override
		public int compareTo(Delayed o) {
			if (o == this) return 0;
			
			if (o instanceof TimeoutElement){
				TimeoutElement<?, ?> other = (TimeoutElement<?, ?>)o;
				long dif = this.expiryTimeMillis - other.expiryTimeMillis;
				return dif == 0 ? 0 : (dif < 0 ? -1 : 1); 
			}
			
			throw new IllegalStateException();
		}

		/**
		 * Returns the amount of time left before eviction should occur.
		 */
		@Override
		public long getDelay(TimeUnit unit) {
			if (!hasDelay())
				throw new IllegalStateException("Doesn't have delay, should not be part of delay queue");
			
			return unit.convert(expiryTimeMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}
		
		private boolean hasDelay(){
			return expiryTimeMillis != 0;
		}
	}	
}
