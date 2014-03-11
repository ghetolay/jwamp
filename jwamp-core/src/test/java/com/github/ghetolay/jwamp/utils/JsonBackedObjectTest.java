/**
 * 
 */
package com.github.ghetolay.jwamp.utils;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.github.ghetolay.jwamp.utils.ObjectHolder;
import com.github.ghetolay.jwamp.utils.ObjectHolderFactory;

/**
 * @author Kevin
 *
 */
public class JsonBackedObjectTest {
	private final JsonFactory fac = new MappingJsonFactory();

	private final String inString = "in";
	private final Map<String, String> inMap = Collections.unmodifiableMap(new HashMap<String, String>(){
		{
			put("key1", "val1");
			put("key2", "val2");
		}
	});
	
	private JsonParser createJsonFor(Object o) throws Exception{
		StringWriter sw = new StringWriter();
		JsonGenerator gen = fac.createGenerator(sw);
		gen.writeObject(o);
		String json = sw.toString();
		
		JsonParser parser = fac.createParser(new StringReader(json));
		parser.nextValue();

		return parser;
	}
	
	
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
	public void testGetAsClassOfT() {
		ObjectHolder obj = ObjectHolderFactory.createForObject(inString);
		assertEquals(inString, obj.getAs(String.class));
	}

	@Test(expected=ClassCastException.class)
	public void testGetAsClassOfTWrongClass() {
		ObjectHolder obj = ObjectHolderFactory.createForObject(inString);
		assertEquals(inString, obj.getAs(Map.class));
	}
	
	@Test
	public void testGetAsTypeReferenceOfT() {
		ObjectHolder obj = ObjectHolderFactory.createForObject(inMap);
		assertEquals(inMap, obj.getAs(new TypeReference<Map<String, String>>(){}));
	}

	@Test(expected=ClassCastException.class)
	public void testGetAsTypeReferenceOfTWrongTypeReference() {
		ObjectHolder obj = ObjectHolderFactory.createForObject(inMap);
		Set<String> out = obj.getAs(new TypeReference<Set<String>>(){});
		// we shouldn't get to here - should get a class cast exception in previous line
		assertEquals(inMap, out);
	}
	

	
	@Test
	public void testJsonGetAsClossOfT() throws Exception{
		String in = "test";
		
		ObjectHolder obj = ObjectHolderFactory.readNextObject(createJsonFor(in));

		assertEquals(in, obj.getAs(String.class));
	}
	
	@Test(expected=ClassCastException.class)
	public void testJsonGetAsClossOfTWrongClass() throws Exception{
		String in = "test";
		
		ObjectHolder obj = ObjectHolderFactory.readNextObject(createJsonFor(in));

		assertEquals(in, obj.getAs(Map.class));
	}
	
	@Test
	public void testJsonGetAsTypeReferenceOfT() throws Exception{
		ObjectHolder obj = ObjectHolderFactory.readNextObject(createJsonFor(inMap));

		assertEquals(inMap, obj.getAs(Map.class));
	}
	
	@Test(expected=ClassCastException.class)
	public void testJsonGetAsTypeReferenceOfTWrongClass() throws Exception{
		ObjectHolder obj = ObjectHolderFactory.readNextObject(createJsonFor(inMap));

		Set<String> out = obj.getAs(new TypeReference<Set<String>>(){});
		// we shouldn't get to here - should get a class cast exception in previous line
		assertEquals(inMap, out);
	}
}
