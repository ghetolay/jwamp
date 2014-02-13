/**
 * 
 */
package com.github.ghetolay.jwamp.utils;

import java.util.Random;

import com.github.ghetolay.jwamp.utils.TimeoutHashMap.TimeoutListener;

/**
 * @author Kevin
 *
 */
public class TimeoutHashMapLoadTest {

	public static void main(String[] args) {
		final TimeoutHashMap map = new TimeoutHashMap();
		
		map.addListener(new TimeoutListener(){

			@Override
			public void timedOut(Object key, Object value) {
				System.out.println(key + " evicted");
			}
			
		});
		
		for(int i = 0; i < 10; i++){
			AbuseThread t = new AbuseThread(map);
			t.start();
		}
	}
		
	private static class AbuseThread extends Thread {
		private final TimeoutHashMap map;
		private final Random r;
		
		public AbuseThread(TimeoutHashMap map) {
			this.map = map;
			r = new Random();
		}

		public void run() {
			try{
				while(true){
					String key = "key" + r.nextInt();
					map.put(key, "value", 10);
//					System.out.println(this + " - Put key");
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
		

}
