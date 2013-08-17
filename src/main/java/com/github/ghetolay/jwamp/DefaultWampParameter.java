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


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.event.ClientEventManager;
import com.github.ghetolay.jwamp.event.DefaultEventSubscriber;
import com.github.ghetolay.jwamp.event.EventResult;
import com.github.ghetolay.jwamp.event.EventAction;
import com.github.ghetolay.jwamp.event.ServerEventManager;
import com.github.ghetolay.jwamp.event.WampSubscription;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.DefaultRPCSender;
import com.github.ghetolay.jwamp.rpc.MappingRPCManager;
import com.github.ghetolay.jwamp.utils.ActionMapping;
import com.github.ghetolay.jwamp.utils.MapActionMapping;
import com.github.ghetolay.jwamp.utils.ResultListener;

public class DefaultWampParameter{ 
	
	private static abstract class FileMappingParameter implements WampParameter{
		
		protected final Logger log = LoggerFactory.getLogger(getClass());
		
		protected Set<WampSubscription> topics = new HashSet<WampSubscription>();
		
		protected ActionMapping<CallAction> actionMapping;
		protected ActionMapping<EventAction> eventMapping;	
		
		public FileMappingParameter() {}
		
		public FileMappingParameter(InputStream is) throws Exception{
			reload(is);
		}
		
		public void reload(InputStream is) throws Exception{
			mapFromFile(is);
		}
		
		//TODO update topic element to add arguments
		private void mapFromFile(InputStream is) throws Exception{
			
			topics.clear();
			
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLEventReader eventReader = inputFactory.createXMLEventReader(is);
			
			QName id = new QName("id");
			QName clazz = new QName("class");
			
			//false-> event / true -> rpc
			boolean rpc = true;
			Map<String, CallAction> actions = null;
			Map<String, EventAction> events = null;
			
			if(!eventReader.hasNext())
				return; 
						
			XMLEvent event = eventReader.nextEvent();
			
			while (event != null) {		
				if(event.isStartElement()){					
					StartElement s  = event.asStartElement();
					String name = s.getName().getLocalPart();
					if(name.equals("rpc")){
						actions = new HashMap<String, CallAction>();
						rpc = true;
					}else if(name.equals("event")){
						events = new HashMap<String, EventAction>();
						rpc = false;
					}else if(name.equals("action"))
						try{
							if( (rpc && actions == null) || (!rpc && events == null) )
								throw new Exception("action element must be inside <rpc> or <event> element");
							else{
							
								Attribute att = s.getAttributeByName(id);
								
								String actionId;
								if(att == null || (actionId=att.getValue()) == null)
									throw new Exception("Missing Id attribute on element <action>");				
								
								att = s.getAttributeByName(clazz);
								
								String actionClass;
								if(att == null || (actionClass=att.getValue()) == null)
									throw new Exception("Missing class attribute on element <action> with Id " + actionId);
					
								
								//Add Action
								Class<?> c = Class.forName(actionClass);
								Object action = c.newInstance();
								
								if(rpc){
									if(action instanceof CallAction)
										actions.put(actionId, (CallAction)action);
									else throw new ClassCastException("class " + actionClass + " does not implements CallAction");
								}else{
									if(action instanceof EventAction){
										events.put(actionId, (EventAction)action);
										((EventAction) action).setEventId(actionId);
									}else throw new ClassCastException("class " + actionClass + " does not implements EventAction");
								}
							}
						}catch(Exception e){
							if(log.isErrorEnabled())
								log.error("Unable to add action : " + e.getMessage());
						} 
					else if(name.equals("subscribe")){
						if(!rpc){
							if(eventReader.hasNext()){
								event = eventReader.nextEvent();
								if(event.isCharacters()){
									String list = event.asCharacters().getData();
									if( list != null)
										for(String sub :list.split(","))
											topics.add(new WampSubscription.Impl(sub));
								}else
									continue;
							}
						}else if(log.isWarnEnabled())
							log.warn("subscribe element must be inside <event> element");
					}
				}else if(event.isEndElement()){
					String name = event.asEndElement().getName().getLocalPart();
					if(name.equals("rpc"))
						actionMapping = new MapActionMapping<CallAction>(actions);
					else if(name.equals("event"))
						eventMapping = new MapActionMapping<EventAction>(events);
				}
				
				if(eventReader.hasNext())
					event = eventReader.nextEvent();
				else
					event = null;
			}
		}
	}
	
	public static class SimpleClientParameter extends FileMappingParameter{

		private ResultListener<EventResult> eventListener;
		
		public SimpleClientParameter() {
			super();
		}
		
		public SimpleClientParameter(InputStream is) throws Exception {
			super(is);
		}
		
		public SimpleClientParameter(InputStream is, ResultListener<EventResult> eventListener) throws Exception {
			super(is);
			
			this.eventListener = eventListener;
		}
		
		public void setGlobalEventListener(ResultListener<EventResult> listener){
			eventListener = listener;
		}
		
		public Collection<WampMessageHandler> getHandlers(){
			ArrayList<WampMessageHandler> handlers = new ArrayList<WampMessageHandler>(2);
			
			handlers.add(new DefaultRPCSender());
			handlers.add(new DefaultEventSubscriber(topics, eventListener));
			
			return handlers; 
		}				
	}
	
	public static class FullClientParameter extends SimpleClientParameter{
		
		public FullClientParameter(InputStream is) throws Exception {
			super(is);
		}

		@Override
		public Collection<WampMessageHandler> getHandlers(){
			Collection<WampMessageHandler> handlers = super.getHandlers();
		
			handlers.add(new MappingRPCManager(actionMapping));
			handlers.add(new ClientEventManager(eventMapping));
			
			return handlers;
		}
	}
	
	public static class SimpleServerParameter extends FileMappingParameter{ 
		
		private WampMessageHandler eventManager;
		
		public SimpleServerParameter(InputStream is) throws Exception{
			super(is);
			
			eventManager = new ServerEventManager(eventMapping);
		}
		
		public Collection<WampMessageHandler> getHandlers(){
			ArrayList<WampMessageHandler> handlers = new ArrayList<WampMessageHandler>(2);
			
			handlers.add(new MappingRPCManager(actionMapping));
			handlers.add(eventManager);
			
			return handlers; 
		}
	}
	
	public static class FullServerParameter extends SimpleServerParameter{

		private ResultListener<EventResult> eventListener;
		
		public FullServerParameter(InputStream is) throws Exception{
			super(is);	
		}
		
		public FullServerParameter(InputStream is, ResultListener<EventResult> eventListener) throws Exception {
			super(is);
			
			this.eventListener = eventListener;
		}
		
		public void setGlobalEventListener(ResultListener<EventResult> listener){
			eventListener = listener;
		}

		@Override
		public Collection<WampMessageHandler> getHandlers(){
			Collection<WampMessageHandler> handlers = super.getHandlers();
			
			handlers.add(new DefaultRPCSender());
			handlers.add(new DefaultEventSubscriber(topics, eventListener));
			
			return handlers;
		}
	}
}
