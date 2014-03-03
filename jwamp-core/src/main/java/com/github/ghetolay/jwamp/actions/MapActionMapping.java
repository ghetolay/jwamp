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
package com.github.ghetolay.jwamp.actions;


import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapActionMapping<T> extends HashMap<URI,T> implements ActionMapping<T> {

	private static final long serialVersionUID = 5706026333888185183L;
	
	public MapActionMapping(){
		super();
	}
	
	public MapActionMapping( Map<URI,T> actions){
		super(actions);
	}
	
	public void addAction(URI actionURI, T action){
		put(actionURI, action);
	}
	
	public T getAction(URI actionURI){
		return get(actionURI);
	}

	public Iterator<T> getActionsIterator(){
		return values().iterator();
	}
}
