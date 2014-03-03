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
package com.github.ghetolay.jwamp.utils;

/**
 * @author ghetolay
 *
 */
//TODO: KD - do we need this?
public class Return {
	
	public static class Double<A,B>{
		A a;
		B b;
		
		public Double(A a,B b){
			this.a = a;
			this.b = b;
		}
		
		public A getFirst(){
			return a;
		}
		
		public B getSecond(){
			return b;
		}
	}
	 
	public static class Triple<A,B,C> extends Double<A,B>{
		
		C c;
		
		public Triple(A a, B b, C c){
			super(a,b);
			this.c = c;
		}
		
		public C getThird(){
			return c;
		}
	}
	
	public static class Quad<A,B,C,D> extends Triple<A,B,C>{
		D d;
		
		public Quad(A a, B b,C c,D d){
			super(a,b,c);
			this.d = d;
		}
		
		public D getFourth(){
			return d;
		}
	}
}
