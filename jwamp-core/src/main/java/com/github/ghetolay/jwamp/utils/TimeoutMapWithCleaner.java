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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.github.ghetolay.jwamp.utils.TimeoutMap.TimedOutListener;

/**
 * A cache that allows specifying a timeout for entries.  Entries are evicted at some point after their timeout expires.
 * There is no guarantee that eviction will happen exactly at the timeout, or even somewhat near the timeout. 
 * 
 * This class is threadsafe.
 *
 * @param <K> the type of the key for the cache
 * @param <V> the type of value that will be stored in the cache
 */
public class TimeoutMapWithCleaner<K,V> implements TestableTimeoutMap<K, V>{

	/**
	 * Stores the values and other information needed to maintain the cache entries
	 */
	private final ConcurrentHashMap<K, TimeoutElement<K,V>> map = new ConcurrentHashMap<K, TimeoutElement<K,V>>();
	
	
	/**
	 * A timeout listener that does nothing (used if the caller doesn't want to be notified on a cache timeout eviction)
	 */
	private final NoOpTimeoutListener<K,V> noOpListener = new NoOpTimeoutListener<K,V>();
	
	private final CleanerThread timeoutCleanerThread;
	
	public TimeoutMapWithCleaner() {
		timeoutCleanerThread = new CleanerThread();
		timeoutCleanerThread.setDaemon(true);
		timeoutCleanerThread.start();
	}


	
	@Override
	protected void finalize() throws Throwable {
		// be a good citizen - no purpose in keeping the thread running if the TimeoutHashMap is released
		timeoutCleanerThread.interrupt();
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
	public void put(K key, V value, long timeoutMillis, TimedOutListener<K, V> timeoutListener){
		TimeoutElement<K, V> element = new TimeoutElement<K,V>(key, value, timeoutListener, timeoutMillis);
		map.put(key, element);
		
		if(timeoutMillis > 0)
			timeoutCleanerThread.addTimeoutElement(element);
		
	}

	/**
	 * <strong>IMPORTANT</strong> Unlike other calls in this class, this call is not strictly thread safe - it is possible that an element returned from this call has already timed out and timeout listeners notified
	 * Retrieves the value from the cache, or null if the value is no longer in the cache
	 * @param key the key identifying the cache entry
	 * @return the value from the cache, or null if the value is not in the cache - note that a non-null return does not necessarily mean that the element didn't time-out
	 */
	public V get(K key){
		
		TimeoutElement<K, V> element = map.get(key);
		if (element == null)
			return null;
		
		// Race Condition: it's possible that the element has isRemoved() == true - this could result in a timeout notification happening, even though the value was returned successfuly from get()
		// The only way to resolve this race is with synchronization on the TimeoutElement itself.
		// Given that get() is not actually used by jWAMP, I have opted to just add a note indicating the race condition 
		
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
		
		return element.getValue();
	}


	/**
	 * @return the number of elements in the cache
	 */
	public int size(){
		return map.size();
	}
	
	@Override
	public String getMapDescription() {
		// TODO Auto-generated method stub
		return this.getClass().getName() + ", size: " + size() + ", timeout size: " + timeoutCleanerThread.size();
	}
	
	private class CleanerThread extends Thread{
		
		/**
		 * Manages items that need to be timedout
		 */
		private final DelayQueue<TimeoutElement<K,V>> delayQueue = new DelayQueue<TimeoutElement<K,V>>();

		public void addTimeoutElement(TimeoutElement<K,V> element){
			delayQueue.offer(element);
		}
		
		public int size(){
			return delayQueue.size();
		}
		
		@Override
		public void run() {
			try{
				while(!Thread.interrupted()){
					TimeoutElement<K, V> element = delayQueue.take();
					if (!element.isRemoved()){ // Concurrency note: there is a race condition on 'removed' - there is a small chance that it will return false even if it has been removed.  No big deal, the map will return null
						TimeoutElement<K, V> removed = map.remove(element.getKey());
						if (removed != null){
							removed.markRemoved();
							try{
								removed.notifyTimeoutListener();
							} catch (Throwable t){
								// don't let problems in the listener callback take down our thread
							}
						}
					}
				}
			} catch(InterruptedException ignore){
			}
		}
	}
	
	/**
	 * A {@link TimeoutListener} that does nothing
	 * @param <K> the type of the key associated with the value in the cache
	 * @param <V> the type of the value stored in the cache
	 */
	private static class NoOpTimeoutListener<K, V> implements TimedOutListener<K,V>{

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
		private final TimedOutListener<K, V> timeoutListener;
		/**
		 * State variable that tracks whether the entry has been removed from the cache (i.e. via the {@link TimeoutHashMap#remove(Object)} method)
		 */
		private boolean removed = false;
		
		public TimeoutElement(K key, V value, TimedOutListener<K, V> timeoutListener, long delayMillis) {
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
