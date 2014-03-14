/**
 * 
 */
package com.github.ghetolay.jwamp.utils;

import java.util.LinkedList;
import java.util.Random;

import com.github.ghetolay.jwamp.utils.TimeoutHashMap.TimeoutListener;

/**
 * @author Kevin
 *
 */
public class TimeoutHashMapLoadTest {

	public static void main(String[] args) {
		final TimeoutHashMap<String, String> map = new TimeoutHashMap<String, String>();
		
		for(int i = 0; i < 10; i++){
			AbuseThread t = new AbuseThread(map);
			t.start();
		}
		
		try{
			while(true){
				Thread.sleep(1000L);
				System.out.println("Map/Queue size is " + map.size() + "/" + map.delayQueueSize());
			}
		} catch (InterruptedException e){
			// expected
		}
	}
		
	
	
	private static class AbuseThread extends Thread {
		private final LinkedList<String> myKeys = new LinkedList<String>();
		private final TimeoutHashMap<String, String> map;
		private final Random r;
		private TimeoutListener<String, String> listener = new TimeoutListener<String, String>(){

			@Override
			public void timedOut(String key, String value) {
				//System.out.println(key + " evicted");
			}
			
		};
		
		public AbuseThread(TimeoutHashMap<String, String> map) {
			this.map = map;
			r = new Random();
		}

		public void run() {
			try{
				while(true){
					
					if (r.nextBoolean()){ // add
						int timeout = 1 + r.nextInt(30000);
						String key = "key" + r.nextInt();
						myKeys.offer(key);
						map.put(key, "value", timeout, listener);
					} else { // remove
						String key = myKeys.poll();
						if (key != null)
							map.remove(key);
					}
					
					int delay = r.nextInt(15);
					if (delay > 7)
						Thread.sleep(delay);
					//System.out.println(this + " - Put key");
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
		

}
