/**
 * 
 */
package com.github.ghetolay.jwamp.endpoint;

import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.github.ghetolay.jwamp.actions.ActionRegistration;
import com.github.ghetolay.jwamp.event.EventAction;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.RPCTimeoutManager;
import com.github.ghetolay.jwamp.session.SessionRegistry;
import com.github.ghetolay.jwamp.session.WampLifeCycleListener;
import com.github.ghetolay.jwamp.session.WampSessionContextFactory;

/**
 * A set of objects that are useful to WampEndPoints.
 * These are implemented as a separate class to make it easier to configure them in one place and use them with either ServerEndpointConfig or ClientEndpointConfig implementations
 */
public class WampEndpointParameters {

	private final SessionRegistry sessionRegistry;
	private final JsonFactory jsonFactory;
	private final List<ActionRegistration<CallAction>> initialCallActionRegistrations;
	private final List<ActionRegistration<EventAction>> initialEventActionRegistrations;
	private final List<WampLifeCycleListener> lifecycleListeners;
	private final WampSessionContextFactory wampSessionContextFactory;
	private final RPCTimeoutManager rpcTimeoutManager;
	
	public WampEndpointParameters(SessionRegistry sessionRegistry, JsonFactory jsonFactory, List<ActionRegistration<CallAction>> initialCallActionRegistrations, List<ActionRegistration<EventAction>> initialEventActionRegistrations, List<WampLifeCycleListener> lifecycleListeners, WampSessionContextFactory wampSessionContextFactory, RPCTimeoutManager rpcTimeoutManager) {
		this.sessionRegistry = sessionRegistry;
		this.jsonFactory = jsonFactory;
		this.initialCallActionRegistrations = initialCallActionRegistrations;
		this.initialEventActionRegistrations = initialEventActionRegistrations;
		this.lifecycleListeners = lifecycleListeners;
		this.wampSessionContextFactory = wampSessionContextFactory;
		this.rpcTimeoutManager = rpcTimeoutManager;
	}


	
	public SessionRegistry getSessionRegistry() {
		return sessionRegistry;
	}

	public JsonFactory getJsonFactory() {
		return jsonFactory;
	}

	public List<ActionRegistration<CallAction>> getInitialCallActionRegistrations() {
		return initialCallActionRegistrations;
	}
	
	public List<ActionRegistration<EventAction>> getInitialEventActionRegistrations() {
		return initialEventActionRegistrations;
	}

	public List<WampLifeCycleListener> getLifecycleListeners() {
		return lifecycleListeners;
	}
	
	public WampSessionContextFactory getWampSessionContextFactory() {
		return wampSessionContextFactory;
	}
	
	public RPCTimeoutManager getRpcTimeoutManager() {
		return rpcTimeoutManager;
	}
	
}
