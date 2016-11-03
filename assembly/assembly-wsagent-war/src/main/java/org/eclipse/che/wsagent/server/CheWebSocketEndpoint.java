package org.eclipse.che.wsagent.server;

import org.eclipse.che.api.core.websocket.impl.BasicWebSocketEndpoint;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;
import org.eclipse.che.api.core.websocket.impl.PendingMessagesReSender;
import org.eclipse.che.api.core.websocket.impl.WebSocketSessionRegistry;
import org.eclipse.che.api.core.websocket.impl.WebSocketTransmissionDispatcher;

import javax.inject.Inject;
import javax.websocket.server.ServerEndpoint;

/**
 * @author Vitalii Parfonov
 */

@ServerEndpoint(value = "/websocket/{endpoint-id}", configurator = GuiceInjectorEndpointConfigurator.class)
public class CheWebSocketEndpoint extends BasicWebSocketEndpoint {

    @Inject
    public CheWebSocketEndpoint(WebSocketSessionRegistry registry,
                                PendingMessagesReSender reSender,
                                WebSocketTransmissionDispatcher dispatcher) {
        super(registry, reSender, dispatcher);
    }
}
