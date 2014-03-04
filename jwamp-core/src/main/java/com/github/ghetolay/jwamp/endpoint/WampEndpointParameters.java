/**
 * 
 */
package com.github.ghetolay.jwamp.endpoint;

import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.github.ghetolay.jwamp.actions.ActionRegistration;
import com.github.ghetolay.jwamp.event.EventAction;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.CallIdTimeoutKey;
import com.github.ghetolay.jwamp.rpc.CallResultListener;
import com.github.ghetolay.jwamp.session.SessionRegistry;
import com.github.ghetolay.jwamp.session.WampLifeCycleListener;
import com.github.ghetolay.jwamp.session.WampSessionConfigFactory;
import com.github.ghetolay.jwamp.utils.TimeoutHashMap;

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
	private final WampSessionConfigFactory wampSessionConfigFactory;
	private final TimeoutHashMap<CallIdTimeoutKey, CallResultListener> rpcTimeoutManager;
	
	public WampEndpointParameters(SessionRegistry sessionRegistry, JsonFactory jsonFactory, List<ActionRegistration<CallAction>> initialCallActionRegistrations, List<ActionRegistration<EventAction>> initialEventActionRegistrations, List<WampLifeCycleListener> lifecycleListeners, WampSessionConfigFactory wampSessionConfigFactory, TimeoutHashMap<CallIdTimeoutKey, CallResultListener> rpcTimeoutManager) {
		this.sessionRegistry = sessionRegistry;
		this.jsonFactory = jsonFactory;
		this.initialCallActionRegistrations = initialCallActionRegistrations;
		this.initialEventActionRegistrations = initialEventActionRegistrations;
		this.lifecycleListeners = lifecycleListeners;
		this.wampSessionConfigFactory = wampSessionConfigFactory;
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
	
	public WampSessionConfigFactory getWampSessionConfigFactory() {
		return wampSessionConfigFactory;
	}
	
	public TimeoutHashMap<CallIdTimeoutKey, CallResultListener> getRpcTimeoutManager() {
		return rpcTimeoutManager;
	}
	
}
