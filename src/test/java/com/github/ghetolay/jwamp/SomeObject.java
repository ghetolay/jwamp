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
package com.github.ghetolay.jwamp;

/**
 * @author ghetolay
 *
 */
public class SomeObject{
	
	private String fieldOne;
	private int fieldTwo;
	
	public SomeObject(){}
	
	public SomeObject(String fieldOne, int fieldTwo){
		this.fieldOne = fieldOne;
		this.fieldTwo = fieldTwo;
	}
	
	public String getFieldOne() {
		return fieldOne;
	}
	public void setFieldOne(String fieldOne) {
		this.fieldOne = fieldOne;
	}
	public int getFieldTwo() {
		return fieldTwo;
	}
	public void setFieldTwo(int fieldTwo) {
		this.fieldTwo = fieldTwo;
	}
	
	public boolean equals(Object o){
		if(o!= null && o instanceof SomeObject){
			SomeObject obj = (SomeObject)o;
			
			if( obj.fieldTwo == fieldTwo ){
				if(obj.fieldOne != null )
					return obj.fieldOne.equals(fieldOne);
				else 
					return fieldOne == null;
			}
		}
		return false;
	}
	
	public String toString(){
		return "SomeObject { " + fieldOne + ", " + fieldTwo + " }";
	}
}
