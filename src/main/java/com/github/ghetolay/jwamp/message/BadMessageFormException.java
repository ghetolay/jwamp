package com.github.ghetolay.jwamp.message;

public class BadMessageFormException extends Exception {

	private static final long serialVersionUID = 1349080997487336808L;

	public BadMessageFormException(String s){
		super(s);
	}
	
	public BadMessageFormException(Exception e) {
		super(e);
	}

	public static BadMessageFormException notEnoughParameter(String messageType, int nbParameter, int nbParameterNecessary){
		return new BadMessageFormException("Not enough parameter for message type " + messageType + 
								" only " + nbParameter + " on " + nbParameterNecessary + " necessary");
	}
}
