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

import java.util.List;
import java.util.Map;

/**
 * @author ghetolay
 *
 */
public interface DynamicValue {

	boolean isBoolean();
	boolean isNumber();
	boolean isString();
	boolean isList();
	boolean isMap();
	boolean maybeObject();
	
	Object asObject();
	boolean asBoolean();
	double asNumber();
	String asString();
	List<?> asList();
	Map<?,?> asMap();

}
