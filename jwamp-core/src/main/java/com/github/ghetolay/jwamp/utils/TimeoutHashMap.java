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

public class TimeoutHashMap<K,V>{

	private final ConcurrentHashMap<K, TimeoutElement<K,V>> map = new ConcurrentHashMap<K, TimeoutElement<K,V>>();
	private final DelayQueue<TimeoutElement<K,V>> delayQueue = new DelayQueue<TimeoutElement<K,V>>();
	
	private final NoOpTimeoutListener<K,V> noOpListener = new NoOpTimeoutListener<K,V>();
	
	public TimeoutHashMap() {
	}

	public void put(K key, V value, long timeoutMillis){
		put(key, value, timeoutMillis, noOpListener);
	}
	
	public void put(K key, V value, long timeoutMillis, TimeoutListener<K, V> timeoutListener){
		TimeoutElement<K, V> element = new TimeoutElement<K,V>(key, value, timeoutListener, timeoutMillis);
		map.put(key, element);
		
		if(timeoutMillis > 0)
			delayQueue.offer(element);
		
		pollTimeouts();
	}

	public V get(K key){
		pollTimeouts();
		
		TimeoutElement<K, V> element = map.get(key);
		if (element == null)
			return null;
		
		return element.getValue();
	}
	
	public V remove(K key){
		TimeoutElement<K, V> element = map.remove(key);
		if (element == null)
			return null;
		
		element.markRemoved();
		
		pollTimeouts();
		
		return element.getValue();
	}

	public int size(){
		return map.size();
	}
	
	/**
	 * Used for testing only
	 * @return the size of the delay queue
	 */
	int delayQueueSize(){
		return delayQueue.size();
	}
	
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
	
	public static interface TimeoutListener<K,V>{
		public void timedOut(K key, V value);
	}
	
	private static class NoOpTimeoutListener<K, V> implements TimeoutListener<K,V>{

		@Override
		public void timedOut(K key, V value) {
			// do nothing
		}
		
	}
	
	private static class TimeoutElement<K,V> implements Delayed {
		private final long expiryTimeMillis;
		private final K key;
		private final V value;
		private final TimeoutListener<K, V> timeoutListener;
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

		@Override
		public long getDelay(TimeUnit unit) {
			if (!hasDelay())
				throw new IllegalStateException("Doesn't have delay, should not be part of delay queue");
			
			return unit.convert(expiryTimeMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}
		
		public boolean hasDelay(){
			return expiryTimeMillis != 0;
		}
	}	
}
