/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.wsagent.server.jsonrpc;

import static org.slf4j.LoggerFactory.getLogger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.websocket.server.ServerEndpoint;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketEndpoint;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;
import org.eclipse.che.api.core.websocket.impl.MessagesReSender;
import org.eclipse.che.api.core.websocket.impl.WebSocketSessionRegistry;
import org.eclipse.che.api.core.websocket.impl.WebsocketIdService;
import org.eclipse.che.commons.lang.execution.ExecutorServiceProvider;
import org.slf4j.Logger;

/**
 * Implementation of BasicWebSocketEndpoint for Che packaging. Add only mapping "/wsagent".
 *
 * @author Vitalii Parfonov
 */
@ServerEndpoint(value = "/wsagent", configurator = GuiceInjectorEndpointConfigurator.class)
public class WsAgentWebSocketEndpoint extends BasicWebSocketEndpoint {

  private static final Logger LOG = getLogger(WsAgentWebSocketEndpoint.class);

  public static final String ENDPOINT_ID = "ws-agent-websocket-endpoint";

  @Inject
  public WsAgentWebSocketEndpoint(
      WebSocketSessionRegistry registry,
      MessagesReSender reSender,
      WebSocketMessageReceiver receiver,
      WebsocketIdService websocketIdService) {
    super(registry, reSender, receiver, websocketIdService);
  }

  @Override
  protected String getEndpointId() {
    return ENDPOINT_ID;
  }

  @Singleton
  public static class CheWebSocketEndpointExecutorServiceProvider extends ExecutorServiceProvider {

    @Inject
    public CheWebSocketEndpointExecutorServiceProvider(
        @Named("che.core.jsonrpc.processor_core_pool_size") int corePoolSize,
        @Named("che.core.jsonrpc.processor_max_pool_size") int maxPoolSize,
        @Named("che.core.jsonrpc.processor_queue_capacity") int queueCapacity) {
      super(corePoolSize, maxPoolSize, queueCapacity);
    }
  }
}
