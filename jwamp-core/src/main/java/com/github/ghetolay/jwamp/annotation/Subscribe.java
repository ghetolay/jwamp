package com.github.ghetolay.jwamp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Subscribe {
	/**
	 * TopicURI
	 * 
	 * @return
	 */
	String[] value();
}
