package org.eclipse.che.workspace.infrastructure.docker.server;

import org.eclipse.che.api.core.websocket.commons.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketEndpoint;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;
import org.eclipse.che.api.core.websocket.impl.MessagesReSender;
import org.eclipse.che.api.core.websocket.impl.WebSocketSessionRegistry;

import javax.inject.Inject;
import javax.websocket.server.ServerEndpoint;

/**
 * JSON-RPC endpoint for agent installers.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */

@ServerEndpoint(value = "/agent/websocket/{endpoint-id}", configurator = GuiceInjectorEndpointConfigurator.class)
public class InstallerEndpoint extends BasicWebSocketEndpoint {

    @Inject
    public InstallerEndpoint(WebSocketSessionRegistry registry,
                             MessagesReSender reSender,
                             WebSocketMessageReceiver receiver) {
        super(registry, reSender, receiver);
    }
}
