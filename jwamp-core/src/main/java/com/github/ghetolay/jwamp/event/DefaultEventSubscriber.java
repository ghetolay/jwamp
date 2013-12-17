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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.message.WampEventMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampPublishMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampSubscribeMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampUnsubscribeMessage;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class DefaultEventSubscriber implements WampEventSubscriber {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private WampConnection conn;

	private Set<SubSet> topics = new HashSet<SubSet>();

	private Map<String, ResultListener<WampArguments>> eventListeners = new HashMap<String, ResultListener<WampArguments>>();
	private ResultListener<EventResult> globalListener;

	public DefaultEventSubscriber(Collection<WampSubscription> topics, ResultListener<EventResult> globalListener){
		
		for( WampSubscription sub : topics)
			this.topics.add(new SubSet(sub));
		
		this.globalListener = globalListener;
	}
	
	public void onConnected(WampConnection connection) {
		conn = connection;

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

	public void onClose(String sessionId, int closeCode) {}

	public boolean onMessage(String sessionId, WampMessage message){

		if(message.getMessageType() == WampMessage.EVENT){
			onEvent((WampEventMessage)message);
			return true;
		}
		
		return false;
	}

	private void onEvent(WampEventMessage msg){	
		//TODO: ThreadPool shared with RPC
		if(eventListeners.containsKey(msg.getTopicId())){
			try{
				 eventListeners.get(msg.getTopicId()).onResult(msg.getEvents());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		else if(globalListener != null)
			globalListener.onResult(new SimpleEventResult(msg.getTopicId(), msg.getEvents()));

	}

	private void subscribe(SubSet s) throws IOException, SerializationException{
		OutputWampSubscribeMessage msg = new OutputWampSubscribeMessage(s.sub.getTopicId());

		conn.sendMessage(msg);
	}

	public void subscribe(String topicId) throws IOException, SerializationException{
		try {
			subscribe(new WampSubscription.Impl(topicId));
		} catch (SerializationException e) {
			log.error("Serialization error on unsubscribe message");
			throw e;
		}
	}
	
	public void subscribe(String topicId, ResultListener<WampArguments> eventListener) throws IOException, SerializationException{
		try{
			subscribe(topicId);
		}finally{
			if(eventListener != null)
				eventListeners.put(topicId, eventListener);
		}
	}

	public void subscribe(WampSubscription subscription) throws IOException, SerializationException {
		
		SubSet s = new SubSet(subscription);
		
		if(!topics.contains(s)){
			try {
				if(conn != null && conn.isConnected())
					subscribe(s);
			} finally{
				topics.add(s);
				if(log.isDebugEnabled())
					log.debug("Add Subscription " + subscription.getTopicId() );
			}
		}else if(log.isTraceEnabled())
			log.trace("Already subscribed to the topic : " + subscription.getTopicId());
	}

	public void subscribe(WampSubscription subscription, ResultListener<WampArguments> eventListener) throws IOException, SerializationException {

		try{
			subscribe(subscription);
		}finally{
			if(eventListener != null)
				eventListeners.put(subscription.getTopicId(), eventListener);
		}
	}

	public void unsubscribe(String topicId) throws IOException {
		if(topics.contains(topicId)){
			try {
				conn.sendMessage(new OutputWampUnsubscribeMessage(topicId));
			} catch (SerializationException e) {
				log.error("Serialization error on unsubscribe message");
			}

			topics.remove(topicId);
		}
		if(eventListeners.containsKey(topicId))
			eventListeners.remove(topicId);
	}

	public void unsubscribeAll() throws IOException{
		for(Iterator<SubSet> it = topics.iterator(); it.hasNext();){
			try {
				conn.sendMessage(new OutputWampUnsubscribeMessage( it.next().sub.getTopicId() ));
			} catch (SerializationException e) {
				log.error("Serialization error on unsubscribe message");
			}
			it.remove();
		}
	}
	
	public void publish(String topicId, Object event) throws IOException, SerializationException {
		publish(topicId, event, true);
	}

	public void publish(String topicId, Object event, boolean excludeMe) throws IOException, SerializationException {
		publish(topicId, event, true, null, null );
	}

	public void publish(String topicId, Object event, boolean excludeMe, List<String> eligible) throws IOException, SerializationException {
		if(eligible != null){
			List<String> excludes = new ArrayList<String>();
			if(excludeMe)
				excludes.add(conn.getSessionId());

			publish(topicId, event, false, excludes, eligible);
		}else
			publish(topicId, event, excludeMe, null , null);
	}

	public void publish(String topicId, Object event, List<String> exclude, List<String> eligible) throws IOException, SerializationException {
		if(exclude.size() == 1 && exclude.get(0).equals(conn.getSessionId()) && eligible == null)
			publish(topicId, event, true, null, null);
		else
			publish(topicId, event, false, exclude, eligible);
	}

	private void publish(String topicId, Object event, boolean excludeMe, List<String> exclude, List<String> eligible) throws IOException, SerializationException{

		if(!topics.contains(topicId))
			subscribe(topicId);

		OutputWampPublishMessage msg = new OutputWampPublishMessage();
		msg.setTopicId(topicId);
		msg.setEvent(event);

		if(excludeMe)
			msg.setExcludeMe(true);
		else if(exclude != null || eligible != null){
			msg.setExclude(exclude != null ? exclude : new ArrayList<String>());
			msg.setEligible(eligible != null ? exclude : new ArrayList<String>());
		}

		conn.sendMessage(msg);
	}

	public ResultListener<EventResult> getGlobalListener(){
		return globalListener;
	}

	public void setGlobalListener(ResultListener<EventResult> listener){
		globalListener = listener;
	}

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

		WampSubscription sub;

		public SubSet(WampSubscription sub) {
			this.sub = sub;
		}

		@Override
		public boolean equals(Object o){
			if( o == null || sub == null)
				return false;

			if(o instanceof WampSubscription)
				return ((WampSubscription) o).getTopicId().equals(sub.getTopicId());

			if(o instanceof SubSet)
				return ((SubSet) o).sub.getTopicId().equals(sub.getTopicId());

			return false;
		}

		@Override
		public int hashCode(){
			return sub.getTopicId().hashCode();
		}
	}
}
