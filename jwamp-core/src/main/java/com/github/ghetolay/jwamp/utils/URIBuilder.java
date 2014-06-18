package com.github.ghetolay.jwamp.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class URIBuilder {
	
	public static URI newURI(String uri){
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
