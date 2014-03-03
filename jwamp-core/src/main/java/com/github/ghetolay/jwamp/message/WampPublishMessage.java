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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.ghetolay.jwamp.utils.JsonBackedObject;

public class WampPublishMessage extends WampMessage{

	private final URI topicURI;
	private final JsonBackedObject event;
	private final Set<String> exclude;
	private final List<String> eligible;
	private final boolean excludeMe;
	
	public static WampPublishMessage createSimple(URI topicURI, JsonBackedObject event){
		return new WampPublishMessage(topicURI, event, false, null, null);
	}

	public static WampPublishMessage createExcludeMe(URI topicURI, JsonBackedObject event){
		return new WampPublishMessage(topicURI, event, true, null, null);
	}

	public static WampPublishMessage createExcludeMe(URI topicURI, JsonBackedObject event, boolean excludeMe){
		return new WampPublishMessage(topicURI, event, excludeMe, null, null);
	}

	public static WampPublishMessage createWithExclude(URI topicURI, JsonBackedObject event, Collection<String> exclude){
		if (exclude == null)
			throw new IllegalArgumentException("Exclude cannot be null");
		return new WampPublishMessage(topicURI, event, false, exclude, null);
	}

	public static WampPublishMessage createWithExcludeAndEligible(URI topicURI, JsonBackedObject event, Collection<String> exclude, Collection<String> eligible){
		if (exclude == null)
			throw new IllegalArgumentException("Exclude cannot be null");
		if (eligible == null)
			throw new IllegalArgumentException("Eligible cannot be null");
		return new WampPublishMessage(topicURI, event, false, exclude, eligible);
	}
	
	private WampPublishMessage(URI topicUri, JsonBackedObject event, boolean excludeMe, Collection<String> exclude, Collection<String> eligible){
		super(MessageType.PUBLISH);
		this.topicURI = topicUri;
		this.event = event;
		this.excludeMe = excludeMe;
		this.exclude = exclude != null ? Collections.unmodifiableSet(new HashSet<String>(exclude)) : null;
		this.eligible = eligible != null ? Collections.unmodifiableList(new ArrayList<String>(eligible)) : null;
	}
	
	public URI getTopicURI() {
		return topicURI;
	}

	public JsonBackedObject getEvent() {
		return event;
	}
	
	public boolean isExcludeSpecified(){
		return exclude != null;
	}
	
	public Set<String> getExclude() {
		if (exclude == null) throw new IllegalStateException("exclude not specified");
		return exclude;
	}

	public boolean isEligibleSpecified(){
		return eligible != null;
	}
	
	public List<String> getEligible() {
		if (eligible == null) throw new IllegalStateException("eligible not specified");
		return eligible;
	}

	public boolean isExcludeMe() {
		return excludeMe;
	}
	
	@Override
	public String toString(){
		return " WampPublishMessage { "+ excludeMe + " , " + event + " , exclude : " + exclude + " , eligible : " + eligible + " } ";
	}
}
