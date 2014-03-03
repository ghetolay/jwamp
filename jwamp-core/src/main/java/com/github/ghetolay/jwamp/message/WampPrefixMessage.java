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

import java.net.URI;


public class WampPrefixMessage extends WampMessage{

	private final String prefix;
	private final URI uri;
	
	public WampPrefixMessage(String prefix, URI uri){
		super(MessageType.PREFIX);
		this.prefix = prefix;
		this.uri = uri;
	}

	public String getPrefix() {
		return prefix;
	}

	public URI getUri() {
		return uri;
	}

	@Override
	public String toString(){
		return " WampPrefixMessage { "+ prefix+ " , " + uri + " } ";
	}
}
