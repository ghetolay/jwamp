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

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * @author ghetolay
 *
 */
public class RegExpActionMapping<T> implements ActionMapping<T>{

	private HashMap<Pattern,T> actions = new HashMap<Pattern, T>();
	
	/* (non-Javadoc)
	 * @see com.github.ghetolay.jwamp.utils.ActionMapping#getAction(java.lang.String)
	 */
	//TODO first match may not be the best choice
	public T getAction(URI actionURI) {
		for(Pattern regExp : actions.keySet())
			if(regExp.matcher(actionURI.toString()).matches())
				return actions.get(regExp);
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.github.ghetolay.jwamp.utils.ActionMapping#getActionsIterator()
	 */
	public Iterator<T> getActionsIterator() {
		return actions.values().iterator();
	}

	public void addAction(URI pattern, T action){
		actions.put(Pattern.compile(pattern.toString()), action);
	}
	
	public void addPattern(String pattern, T action){
		actions.put(Pattern.compile(pattern), action);
	}
}
