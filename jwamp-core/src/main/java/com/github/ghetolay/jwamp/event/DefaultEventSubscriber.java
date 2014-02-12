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
package com.github.ghetolay.jwamp.event;


import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.endpoint.SessionManager;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampPublishMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampSubscribeMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampUnsubscribeMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class DefaultEventSubscriber implements WampEventSubscriber {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private SessionManager sessionManager;
	private String sessionId;
	
	private Set<SubSet> topics = new HashSet<SubSet>();

	private Map<URI, ResultListener<WampArguments>> eventListeners = new HashMap<URI, ResultListener<WampArguments>>();
	//private ResultListener<EventResult> globalListener;

	public DefaultEventSubscriber(){
	}
	
	public void addSubscription(EventSubscription topic){
		topics.add(new SubSet(topic));
	}
	
	@Override
	public void init(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	public void onOpen(String sessionId) {
		synchronized(this){
			this.sessionId = sessionId;
			
			for(SubSet s : topics)
				try {
					subscribe(s);
				} catch (Exception e) {
					if(log.isWarnEnabled())
						log.warn("Unable to auto-subscribe : " + e.getMessage());
					if(log.isTraceEnabled())
						log.trace("Warning verbose : ", e);
				}
		}
	}

	public void onClose(String sessionId, CloseReason closeReason) {
		synchronized(this){
			sessionId = null;
		}
	}

	public void onMessage(String sessionId, WampEventMessage msg){
		//TODO: ThreadPool shared with RPC
		if(eventListeners.containsKey(msg.getTopicURI())){
			try{
				 eventListeners.get(msg.getTopicURI()).onResult(msg.getEvents());
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		/*else if(globalListener != null)
			globalListener.onResult(new SimpleEventResult(msg.getTopicURI(), msg.getEvents()));
		*/

	}

	private void subscribe(SubSet s) throws IOException, EncodeException{
		OutputWampSubscribeMessage msg = new OutputWampSubscribeMessage(s.sub.getTopicURI());

		sessionManager.sendMessageTo(sessionId, msg);
	}

	public void subscribe(URI topicURI) throws IOException, EncodeException{
		try {
			subscribe(new EventSubscription.Impl(topicURI));
		} catch (EncodeException e) {
			log.error("Serialization error on unsubscribe message");
			throw e;
		}
	}
	
	public void subscribe(URI topicURI, ResultListener<WampArguments> eventListener) throws IOException, EncodeException{
		try{
			subscribe(topicURI);
		}finally{
			if(eventListener != null)
				eventListeners.put(topicURI, eventListener);
		}
	}

	public void subscribe(EventSubscription subscription) throws IOException, EncodeException {
		
		SubSet s = new SubSet(subscription);
		synchronized(this){
			if(!topics.contains(s)){
				try {
					if(sessionId != null)
						subscribe(s);
				} finally{
					topics.add(s);
					if(log.isDebugEnabled())
						log.debug("Add Subscription " + subscription.getTopicURI() );
				}
			}else if(log.isTraceEnabled())
				log.trace("Already subscribed to the topic : " + subscription.getTopicURI());
		}
	}

	public void subscribe(EventSubscription subscription, ResultListener<WampArguments> eventListener) throws IOException, EncodeException {

		try{
			subscribe(subscription);
		}finally{
			if(eventListener != null)
				eventListeners.put(subscription.getTopicURI(), eventListener);
		}
	}

	public void unsubscribe(URI topicURI) throws IOException {
		if(topics.contains(topicURI)){
			try {
				sessionManager.sendMessageTo(sessionId, new OutputWampUnsubscribeMessage(topicURI));
			} catch (EncodeException e) {
				log.error("Serialization error on unsubscribe message");
			}

			topics.remove(topicURI);
		}
		if(eventListeners.containsKey(topicURI))
			eventListeners.remove(topicURI);
	}

	public void unsubscribeAll() throws IOException{
		for(Iterator<SubSet> it = topics.iterator(); it.hasNext();){
			try {
				sessionManager.sendMessageTo(sessionId, new OutputWampUnsubscribeMessage( it.next().sub.getTopicURI() ));
			} catch (EncodeException e) {
				log.error("Serialization error on unsubscribe message");
			}
			it.remove();
		}
	}
	
	public void publish(URI topicURI, Object event) throws IOException, EncodeException {
		publish(topicURI, event, true);
	}

	public void publish(URI topicURI, Object event, boolean excludeMe) throws IOException, EncodeException {
		publish(topicURI, event, true, null, null );
	}

	public void publish(URI topicURI, Object event, boolean excludeMe, List<String> eligible) throws IOException, EncodeException {
		if(eligible != null){
			List<String> excludes = new ArrayList<String>();
			if(excludeMe)
				excludes.add(sessionId);

			publish(topicURI, event, false, excludes, eligible);
		}else
			publish(topicURI, event, excludeMe, null , null);
	}

	public void publish(URI topicURI, Object event, List<String> exclude, List<String> eligible) throws IOException, EncodeException {
		if(exclude.size() == 1 && exclude.get(0).equals(sessionId) && eligible == null)
			publish(topicURI, event, true, null, null);
		else
			publish(topicURI, event, false, exclude, eligible);
	}

	private void publish(URI topicURI, Object event, boolean excludeMe, List<String> exclude, List<String> eligible) throws IOException, EncodeException{

		if(!topics.contains(topicURI))
			subscribe(topicURI);

		OutputWampPublishMessage msg = new OutputWampPublishMessage();
		msg.setTopicURI(topicURI);
		msg.setEvent(event);

		if(excludeMe)
			msg.setExcludeMe(true);
		else if(exclude != null || eligible != null){
			msg.setExclude(exclude != null ? exclude : new ArrayList<String>());
			msg.setEligible(eligible != null ? exclude : new ArrayList<String>());
		}

		sessionManager.sendMessageTo(sessionId, msg);
	}

	/*
	public ResultListener<EventResult> getGlobalListener(){
		return globalListener;
	}

	public void setGlobalListener(ResultListener<EventResult> listener){
		globalListener = listener;
	}
	 */
	
	public class SimpleEventResult implements EventResult{
		private String topicId;
		private WampArguments event;

		private SimpleEventResult(String topicId, WampArguments event){
			this.topicId = topicId;
			this.event = event;
		}

		public String getTopicId(){
			return topicId;
		}

		public WampArguments getEvent(){
			return event;
		}
	}
	
	private class SubSet {

		EventSubscription sub;

		public SubSet(EventSubscription sub) {
			this.sub = sub;
		}

		@Override
		public boolean equals(Object o){
			if( o == null || sub == null)
				return false;

			if(o instanceof EventSubscription)
				return ((EventSubscription) o).getTopicURI().equals(sub.getTopicURI());

			if(o instanceof SubSet)
				return ((SubSet) o).sub.getTopicURI().equals(sub.getTopicURI());

			return false;
		}

		@Override
		public int hashCode(){
			return sub.getTopicURI().hashCode();
		}
	}
}
