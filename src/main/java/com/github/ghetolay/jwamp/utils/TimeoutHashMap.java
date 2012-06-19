package com.github.ghetolay.jwamp.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TimeoutHashMap<K,V> extends HashMap<K,V>{

	private static final long serialVersionUID = 3164054746684312958L;

	private TimeoutThread updater;
	
	public TimeoutHashMap() {
		updater = new TimeoutThread();
		updater.start();
	}

	public void put(K key, V value, long timeout){
		if(timeout > 0)
			updater.add(key,timeout);
		
		super.put(key, value);
	}
	
	protected V remove(Object key, boolean deleteToRemove){
		V result = super.remove(key);
		
		if(deleteToRemove)
			updater.removeFromSet(key);
		
		return result;
	}
	
	@Override
	public V remove(Object key){
		return remove(key,true);
	}
	
	@Override
	public void finalize(){
		updater.interrupt();
	}
	
	private class TimeoutThread extends Thread{
		
		Set<ToRemove> toRemove = new HashSet<ToRemove>();
		long minimalWait = -1;
		long sleepUntil = 0;
		
		public synchronized void add(K key, long timeout){
			
			if(toRemove.isEmpty())
				notify();
				
			if(System.currentTimeMillis() + timeout < sleepUntil)
				minimalWait = timeout;
				notify();
			
			toRemove.add(new ToRemove(key,timeout));
		}
		
		public synchronized void removeFromSet(Object key){
			toRemove.remove(key);
		}
		
		public synchronized void run() {
			 try{
				 while(!isInterrupted()){
					 if(toRemove.isEmpty())
						 wait();
						 
					 //if minimalWait is >0 it means it has been set by add(key,timeout)
					 if(minimalWait < 0){
						 long currentTime = System.currentTimeMillis();
						 minimalWait = Long.MAX_VALUE;
						 sleepUntil = 0;
						 
						 for(Iterator<ToRemove> it = toRemove.iterator(); it.hasNext();){
							 ToRemove tr = it.next();
							 long timeleft = tr.timeLeft(currentTime);
							 
							 if(timeleft <= 0){
								 it.remove();
								 remove(tr.key, false);
							 }else
								 if(timeleft < minimalWait)
									 minimalWait = timeleft;
						 }
					 }
					 
					 if(minimalWait != Long.MAX_VALUE){
						 //we reset minimalWait before the wait
						 long gowait = minimalWait;
						 minimalWait = -1;
						 
						 sleepUntil = System.currentTimeMillis() + gowait;
						 wait(gowait);
					 }
				 }
			 }catch(InterruptedException e){}
		 }
	}
	
	private class ToRemove{
		
		long timeout;
		long startTime;
		K key;
		
		private ToRemove(K key, long timeout){
			startTime = System.currentTimeMillis();
			this.timeout = timeout;
			this.key = key;
		}
		
		private long timeLeft(long currentTime){
			return timeout - (currentTime - startTime);
		}
	}
}
