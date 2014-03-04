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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class OldTimeoutMap<K,V> extends ConcurrentHashMap<K,V> implements TimeoutMap<K,V>, TestableTimeoutMap<K, V>{

	private static final long serialVersionUID = 3164054746684312958L;

	private TimeoutThread cleaner = new TimeoutThread();

	Set<TimedOutListener<K,V>> listeners = new HashSet<TimedOutListener<K,V>>();
	

	public OldTimeoutMap() {
		cleaner.start();
	}

	public void addListener(TimedOutListener<K,V> listener){
		listeners.add(listener);
	}

	public void removeListener(TimedOutListener<K,V> listener){
		listeners.remove(listener);
	}

	public void put(K key, V value, long timeout, TimedOutListener<K, V> listener){
		put(key, value, timeout);
	}
	
	public void put(K key, V value, long timeout){
		super.put(key, value);
		
		if(timeout > 0)
			cleaner.add(key,timeout);	
	}

	@Override
	public String getMapDescription() {
		return getClass().getName() + " size: " + size();
	}
	

	@SuppressWarnings("unchecked")
	protected V timedOut(Object key){
		V result = super.remove(key);

		for(TimedOutListener<K,V> l : listeners)
			l.timedOut((K)key, result);

		return result;
	}

	@Override
	public V remove(Object key){
		V result = super.remove(key);
		
		cleaner.remove(key);
		
		return result;
	}

	@Override
	public void finalize(){
		cleaner.interrupt();
	}

	private class TimeoutThread extends Thread{

		LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		SortedSet<ToRemove> toRemoves = new TreeSet<ToRemove>();
		
		public void add(final K key, long timeout){

			final long expireDate = System.currentTimeMillis() + timeout;
			queue.offer(new Runnable(){				
				@Override
				public void run() {
					toRemoves.add(new ToRemove(key,expireDate));	
				}
			});
		}

		public void remove(final Object key){
			queue.offer(new Runnable(){			
				public void run() {
					//SortedSet do not use equals() but compareTo() == 0 on remove() so we can't use it :/ 
					//It's kinda ok to have unoptimized code on the cleaner thread. What's important is to speed up the thread manipulating the TimeoutHashMap
					for(Iterator<ToRemove> it = toRemoves.iterator(); it.hasNext();){
						if(it.next().equals(key)){
							it.remove();
							return;
						}
					}
				}
			});
		}

		public synchronized void run() {
			try{
				long waitTime = 0;
				while(!isInterrupted()){
					//System.out.println("while");
					Runnable r;
					if(waitTime > 0)
						r = queue.poll(waitTime, TimeUnit.MILLISECONDS);
					else
						r = queue.take();
					//System.out.println("poll " + r);
					if(r != null)
						//add or remove elements to the SortedSet
						r.run();
					
					waitTime = 0;
					//should we invoke System.currentTimeMillis() at each iteration ?
					long currentTime = System.currentTimeMillis();
					
					//System.out.println("ToRemoves size : " + toRemoves.size());
					//this must iterate on ascending order
					for(Iterator<ToRemove> it = toRemoves.iterator(); it.hasNext();){
						
						ToRemove tr = it.next();
						long timeleft = tr.timeLeft(currentTime);
						
						//System.out.println("ToRemove : " + tr.key + " timeleft : " + timeleft);
						
						if(timeleft <= 0){
							timedOut(tr.key);
							it.remove();
							//System.out.println("expired " + tr.expireDate);
						}else{
							//System.out.println("wait " + timeleft);
							waitTime = timeleft;
							break;
						}
					}
					
				}
				
			}catch(InterruptedException e){}
		}
	}

	//This class isn't consistent as SortedSet expect it to be
	private class ToRemove implements Comparable<ToRemove>{

		long expireDate;
		K key;
				
		private ToRemove(K key, long expireDate){
			this.expireDate = expireDate;
			this.key = key;
		}

		private long timeLeft(long currentTime){
			return expireDate - currentTime;
		}

		@Override
		public int compareTo(ToRemove o) {
			return (int) (expireDate - o.expireDate);
		}
	}
}

