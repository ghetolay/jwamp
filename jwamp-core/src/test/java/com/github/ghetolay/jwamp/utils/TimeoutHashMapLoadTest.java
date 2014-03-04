/**
 * 
 */
package com.github.ghetolay.jwamp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.github.ghetolay.jwamp.utils.TimeoutMap.TimedOutListener;

/**
 * @author Kevin
 *
 */
public class TimeoutHashMapLoadTest {

	public static void main(String[] args) {
//		final TimeoutMap map = new TimeoutHashMapWithPolling();
		
		final InsertionEvictionTracker tracker = new InsertionEvictionTracker();
		
		final OldTimeoutMap<String, String> oldMap = new OldTimeoutMap<String, String>();
		
		final NewTimeoutMap<String, String> newMap = new NewTimeoutMap<String, String>();
		
		final TimeoutMapWithCleaner<String, String> newWithCleanerMap = new TimeoutMapWithCleaner<>();
		
		
		
		TestableTimeoutMap<String, String> mapToTest = oldMap;
		
		if (mapToTest == oldMap){
			oldMap.addListener(tracker);
		}
		
		
		List<AbuseThread> threads = new ArrayList<AbuseThread>();
		for(int i = 0; i < 500; i++){
			AbuseThread t = new AbuseThread(mapToTest, tracker);
			threads.add(t);
			t.start();
		}

		try{
			while(true){
				Thread.sleep(1000L);
				
				Rates currentRates = tracker.resetAndGetRates();
				
				System.out.println("Insertion rate: " + currentRates.insertionRate + " insertions/sec, Eviction rate: " + currentRates.evictionRate + ", " + mapToTest.getMapDescription());
			}
		} catch (InterruptedException e){
			// expected
		}
	}
		
	
	public static class Rates{
		long elapsed = 0;
		long insertionRate = 0;
		long evictionRate = 0;
	}
	
	private static class InsertionEvictionTracker implements TimedOutListener<String, String>{
		
		final AtomicLong totalInsertions = new AtomicLong(0);
		final AtomicLong totalEvictions = new AtomicLong(0);
		private long startMillis = System.currentTimeMillis();

		public void addInsertion(){
			totalInsertions.incrementAndGet();
		}
		
		public void addEviction(){
			totalEvictions.incrementAndGet();
		}
		
		public Rates resetAndGetRates(){
			Rates rslt = new Rates();
			rslt.elapsed = System.currentTimeMillis() - startMillis;
			rslt.insertionRate = totalInsertions.get() * 1000L / rslt.elapsed;
			rslt.evictionRate = totalEvictions.get() * 1000L / rslt.elapsed;
			
			startMillis = System.currentTimeMillis();
			totalInsertions.set(0);
			totalEvictions.set(0);
			
			return rslt;
		}
		
		@Override
		public void timedOut(String key, String value) {
			addEviction();
		}
	}
	
	private static class AbuseThread extends Thread {
		private final TestableTimeoutMap<String, String> map;
		private final InsertionEvictionTracker tracker;
		private final Random r;
		
		public AbuseThread(TestableTimeoutMap<String, String> map, InsertionEvictionTracker tracker) {
			this.map = map;
			this.tracker = tracker;
			r = new Random();
		}

		public void run() {
			try{
				while(true){
					
					//if (r.nextBoolean()){ // add
						long timeout = 1 + r.nextInt(100);
						String key = "key" + r.nextInt();
						map.put(key, "value", timeout, tracker);
						tracker.addInsertion();
					//}
					
					// allow other threads to process
					Thread.sleep(2);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
		

}
