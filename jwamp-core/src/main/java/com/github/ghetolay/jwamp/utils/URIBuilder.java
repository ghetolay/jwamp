package com.github.ghetolay.jwamp.utils;

import java.net.URI;
import java.net.URISyntaxException;

public final class URIBuilder {
	private URIBuilder(){}
	
	public static URI uri(String uriStr){
		try {
			return new URI(uriStr);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Failed to create URI for '" + uriStr + "'", e);
		}
	}

}
