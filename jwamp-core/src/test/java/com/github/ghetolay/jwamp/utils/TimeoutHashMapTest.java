/**
 * 
 */
package com.github.ghetolay.jwamp.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Kevin
 *
 */
@RunWith(value = Parameterized.class)
public class TimeoutHashMapTest {

	TimeoutMap<String,String> map;
	
	public TimeoutHashMapTest(TimeoutMap<String,String> mapImpl){
		this.map = mapImpl;
	}

	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection<TimeoutMap<String,String>[]> implementations(){
		List<TimeoutMap<String,String>[]> impls = new ArrayList<TimeoutMap<String,String>[]>(2);
				
		impls.add(new TimeoutMap[] {
			new OldTimeoutMap<String,String>()
		});	
		
		impls.add(new TimeoutMap[] {
			new NewTimeoutMap<String,String>()	
		});
		
		return impls;
	}
	
	@Test
	public void testTimeout() throws Exception {
		map.put("A", "AA", 5);
		map.put("B", "BB", 15);
		Assert.assertEquals("AA", map.get("A"));
		Assert.assertEquals("BB", map.get("B"));
		Thread.sleep(10);
		Assert.assertEquals(null, map.get("A"));
		Assert.assertEquals("BB", map.get("B"));
		Thread.sleep(5);
		Assert.assertEquals(null, map.get("A"));
		Assert.assertEquals(null, map.get("B"));
		
	}

}
