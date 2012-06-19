package com.github.ghetolay.jwamp;

import javax.servlet.http.HttpServletRequest;

public interface WampWebSocketListener {
	public void newWampWebSocket(HttpServletRequest request, WampWebSocket wws);
}
