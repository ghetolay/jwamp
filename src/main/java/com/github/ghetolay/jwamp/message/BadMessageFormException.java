/**
*Copyright [2012] [Ghetolay]
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package com.github.ghetolay.jwamp.message;

public class BadMessageFormException extends SerializationException {

	private static final long serialVersionUID = 1349080997487336808L;

	public BadMessageFormException(String s){
		super(s);
	}
	
	public BadMessageFormException(String s, Throwable e){
		super(s,e);
	}
	
	public BadMessageFormException(Throwable e) {
		super(e);
	}

	public static BadMessageFormException notEnoughParameter(String messageType, int nbParameter, int nbParameterNecessary){
		return new BadMessageFormException("Not enough parameter for message type " + messageType + 
								" only " + nbParameter + " on " + nbParameterNecessary + " (at least) necessary");
	}
}
