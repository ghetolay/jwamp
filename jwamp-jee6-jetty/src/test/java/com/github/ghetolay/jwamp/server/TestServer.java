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
package com.github.ghetolay.jwamp.server;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.DefaultWampParameter;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.WampWebSocket;
import com.github.ghetolay.jwamp.rpc.DefinedMethodRPCManager;
import com.github.ghetolay.jwamp.server.rpc.TestDefinedAction;

/**
 * Jetty Server, with JWAMP enabled, suitable for test case use.
 */
public class TestServer implements JettyWebSocketListener
{
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private Server server;
    private ServerConnector connector;
    private URI serverUri;
    private Map<String, WampWebSocket> connections = new HashMap<String, WampWebSocket>();
    
    public void start() throws Exception
    {
        server = new Server();

        connector = new ServerConnector(server);
        connector.setPort(0);
        server.addConnector(connector);

        WampJettyFactory wampFact = WampJettyFactory.getInstance();

        try (InputStream is = getClass().getResourceAsStream("/wamp-server.xml"))
        {
            WampWebSocketHandler wampHandler = wampFact.newWebsocketHandler(new Parameters(is),this);
            // wampFact.getSerializer().setDesiredFormat(WampSerializer.format.BINARY);
            server.setHandler(wampHandler);
        }
        
        // Start server
        server.start();
        
        // Pick up real port, prepare serverUri for test cases
        String host = connector.getHost();
        if(host == null) {
            host = "localhost";
        }
        serverUri = new URI("ws",null,host,connector.getLocalPort(),"/",null,null);
    }
    
    /**
     * Restart server on same port as before
     */
    public void restart() throws Exception
    {
        int port = connector.getLocalPort();
        server.stop();
        connector.setPort(port);
        server.start();
    }
    
    public URI getServerURI()
    {
        return serverUri;
    }

    public void stopConnections()
    {
        for (Entry<String, WampWebSocket> entry : connections.entrySet())
            entry.getValue().getConnection().close(StatusCode.SHUTDOWN,"");
    }

    public void stop() throws Exception
    {
        server.stop();
    }

    public void newWebSocket(UpgradeRequest request, WampWebSocket wws)
    {
        connections.put(wws.getConnection().getSessionId(),wws);
    }

    public void closedWebSocket(String sessionId)
    {
        connections.remove(sessionId);
    }

    private class Parameters extends DefaultWampParameter.SimpleServerParameter
    {
        public Parameters(InputStream is) throws Exception
        {
            super(is);
        }

        @Override
        public Collection<WampMessageHandler> getHandlers()
        {
            Collection<WampMessageHandler> handlers = super.getHandlers();

            handlers.add(new DefinedMethodRPCManager(new TestDefinedAction()));

            return handlers;
        }
    }
}
