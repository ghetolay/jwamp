/**
 * 
 */
package com.github.ghetolay.jwamp.rpc;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kevin
 *
 */
public class CallIdTimeoutKeyTest {

	CallIdTimeoutKey key1 = new CallIdTimeoutKey("somesessionid", "somecallid");
	CallIdTimeoutKey key2 = new CallIdTimeoutKey("somesessionid", "somecallid");
	CallIdTimeoutKey key3 = new CallIdTimeoutKey("somesessionid2", "somecallid");
	CallIdTimeoutKey key4 = new CallIdTimeoutKey("somesessionid", "somecallid2");

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

	/**
	 * Test method for {@link com.github.ghetolay.jwamp.rpc.CallIdTimeoutKey#hashCode()}.
	 */
	@Test
	public void testHashCode() {

		assertEquals(key1.hashCode(), key2.hashCode());
		assertNotEquals(key1, key3);
		assertNotEquals(key1, key4);
	}

	/**
	 * Test method for {@link com.github.ghetolay.jwamp.rpc.CallIdTimeoutKey#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		assertEquals(key1, key2);
		assertNotEquals(key1, key3);
		assertNotEquals(key1, key4);
	}

}
