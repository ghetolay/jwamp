/**
 * 
 */
package com.github.ghetolay.jwamp.utils;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kevin
 *
 */
public class TimeoutHashMapTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTimeout() throws Exception {
		TimeoutHashMap<String, String> map = new TimeoutHashMap<String, String>();
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
