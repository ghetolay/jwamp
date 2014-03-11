package com.github.ghetolay.jwamp.endpoint;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.actions.ActionRegistration;
import com.github.ghetolay.jwamp.event.EventAction;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.session.WampLifeCycleListener;
import com.github.ghetolay.jwamp.session.WampSession;
import com.github.ghetolay.jwamp.session.WampSessionContext;

/**
 * 
 * 
 * @author ghetolay
 *
 */
public abstract class AbstractWampEndpoint extends Endpoint{
	
	private static final Logger log = LoggerFactory.getLogger(AbstractWampEndpoint.class);
	
	private final WampEndpointParameters parameters;
	
	public AbstractWampEndpoint(WampEndpointParameters parameters){
		this.parameters = parameters;
	}
	
	@Override
	public void onOpen(final Session session, EndpointConfig config) {
		
		notifyLifeCycleListenersOfBeforeOpen(session);
	}
	
	@Override
	public void onClose(Session session, CloseReason closeReason) {
		super.onClose(session, closeReason);
		
		WampSessionContext wampSessionConfig = parameters.getSessionRegistry().getWampSessionConfigForWebSocketSession(session);
		
		if (wampSessionConfig != null){
			parameters.getSessionRegistry().deregister(wampSessionConfig);
		
			notifyLifeCycleListenersOfClose(wampSessionConfig.getWampSession(), closeReason);
		}
	}

	protected WampSessionContext createAndRegisterWampSessionConfig(final Session session, String wampSessionId){
		
		WampSessionContext wampSessionConfig = parameters.getWampSessionContextFactory().createContext(session, parameters, wampSessionId);
		
		parameters.getSessionRegistry().register(wampSessionConfig);
		
		for (ActionRegistration<CallAction> actionRegistration : parameters.getInitialCallActionRegistrations()) {
			wampSessionConfig.getWampSession().getCallActionRegistry().registerAction(actionRegistration.getUri(), actionRegistration.getAction());
		}
				
		for (ActionRegistration<EventAction> actionRegistration : parameters.getInitialEventActionRegistrations()) {
			wampSessionConfig.getWampSession().getEventSubscriptionManager().subscribe(actionRegistration.getUri(), actionRegistration.getAction());
		}		
		
		notifyLifeCycleListenersOfOpen(wampSessionConfig.getWampSession());
			
		return wampSessionConfig;
	}
	
	private void notifyLifeCycleListenersOfBeforeOpen(Session session) {
		for(WampLifeCycleListener listener : parameters.getLifecycleListeners()){
			try{
				listener.beforeOpen(parameters, session);
			} catch (Throwable t){
				log.warn("Exception thrown during lifecycle onBeforeOpen listener notification - it will be ignored", t);
			}
		}
	}
	
	private void notifyLifeCycleListenersOfOpen(WampSession session) {
		for(WampLifeCycleListener listener : parameters.getLifecycleListeners()){
			try{
				listener.onOpen(session);
			} catch (Throwable t){
				log.warn("Exception thrown during lifecycle listener onOpen notification - it will be ignored", t);
			}
		}
	}
	
	private void notifyLifeCycleListenersOfClose(WampSession session, CloseReason closeReason) {
		for(WampLifeCycleListener listener : parameters.getLifecycleListeners()){
			try{
				listener.onClose(session, closeReason);
			} catch (Throwable t){
				log.warn("Exception thrown during lifecycle listener onClose notification - it will be ignored", t);
			}
		}
	}

}
