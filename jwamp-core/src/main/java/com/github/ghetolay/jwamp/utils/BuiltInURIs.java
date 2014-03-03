/**
 * 
 */
package com.github.ghetolay.jwamp.utils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Kevin
 *
 */
public class BuiltInURIs {

	public static class Errors {
		public static URI unknownCall = uri("ws://com.github.ghetolay.jwamp#UnknownCall");
	}
	
	private static URI uri(String uriStr){
		try {
			return new URI(uriStr);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Failed to create URI for '" + uriStr + "' in static intitialization block", e);
		}
	}
	
	private BuiltInURIs() {
	}

}
