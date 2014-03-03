/**
 * 
 */
package com.github.ghetolay.jwamp.utils;

import java.util.LinkedList;
import java.util.Random;

import com.github.ghetolay.jwamp.utils.TimeoutMap.TimedOutListener;

/**
 * @author Kevin
 *
 */
public class TimeoutHashMapLoadTest {

	public static void main(String[] args) {
		final TimeoutMap<String,String> map = 
				new OldTimeoutMap<String,String>();
			//	new NewTimeoutMap<String,String>();
		
		for(int i = 0; i < 10; i++){
			AbuseThread t = new AbuseThread(map);
			t.start();
		}
		
		try{
			while(true){
				Thread.sleep(1000L);
				System.out.println("Map/Queue size is " + map.size());
			}
		} catch (InterruptedException e){
			// expected
		}
	}
		
	
	
	private static class AbuseThread extends Thread {
		private final LinkedList<String> myKeys = new LinkedList<String>();
		private final TimeoutMap<String, String> map;
		private final Random r;
		private TimedOutListener<String, String> listener = new TimedOutListener<String, String>(){

			@Override
			public void timedOut(String key, String value) {
				//System.out.println(key + " evicted");
			}
			
		};
		
		public AbuseThread(TimeoutMap<String, String> map) {
			this.map = map;
			r = new Random();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void run() {
			if(map instanceof OldTimeoutMap)
				((OldTimeoutMap)map).addListener(listener);
				
			try{
				while(true){
					
					if (r.nextBoolean()){ // add
						int timeout = 1 + r.nextInt(30000);
						String key = "key" + r.nextInt();
						myKeys.offer(key);
						
						if(map instanceof NewTimeoutMap)
							((NewTimeoutMap)map).put(key, "value", timeout, listener);
						else
							map.put(key, "value", timeout);
						
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
