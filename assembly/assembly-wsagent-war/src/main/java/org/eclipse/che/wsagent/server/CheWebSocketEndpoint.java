/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.wsagent.server;

import org.eclipse.che.api.core.websocket.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketEndpoint;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;
import org.eclipse.che.api.core.websocket.impl.MessagesReSender;
import org.eclipse.che.api.core.websocket.impl.WebSocketSessionRegistry;

import javax.inject.Inject;
import javax.websocket.server.ServerEndpoint;

/**
 * Implementation of BasicWebSocketEndpoint for Che packaging.
 * Add only mapping "/websocket/{endpoint-id}".
 *
 * @author Vitalii Parfonov
 */

@ServerEndpoint(value = "/websocket/{endpoint-id}", configurator = GuiceInjectorEndpointConfigurator.class)
public class CheWebSocketEndpoint extends BasicWebSocketEndpoint {

    @Inject
    public CheWebSocketEndpoint(WebSocketSessionRegistry registry,
                                MessagesReSender reSender,
                                WebSocketMessageReceiver receiver) {
        super(registry, reSender, receiver);
    }
}
