/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import javax.inject.Inject;
import javax.websocket.server.ServerEndpoint;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketEndpoint;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;
import org.eclipse.che.api.core.websocket.impl.MessagesReSender;
import org.eclipse.che.api.core.websocket.impl.WebSocketSessionRegistry;
import org.eclipse.che.api.core.websocket.impl.WebsocketIdService;

/**
 * JSON-RPC endpoint for output.
 *
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
@ServerEndpoint(
  value = OutputEndpoint.OUTPUT_WEBSOCKET_ENDPOINT_BASE,
  configurator = GuiceInjectorEndpointConfigurator.class
)
public class OutputEndpoint extends BasicWebSocketEndpoint {

  public static final String OUTPUT_WEBSOCKET_ENDPOINT_BASE = "/output/websocket";

  @Inject
  public OutputEndpoint(
      WebSocketSessionRegistry registry,
      MessagesReSender reSender,
      WebSocketMessageReceiver receiver,
      WebsocketIdService websocketIdService) {
    super(registry, reSender, receiver, websocketIdService);
  }

  @Override
  protected String getEndpointId() {
    return "output-websocket-endpoint";
  }
}
