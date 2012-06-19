package com.github.ghetolay.jwamp.utils;


import java.util.Iterator;

import com.github.ghetolay.jwamp.event.EventAction;


public interface ActionMapping<T> {
	public T getAction(String actionId);
	public Iterator<T> getActionsIterator();
	public String getActionId(EventAction action);
}
