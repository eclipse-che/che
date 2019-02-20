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
package org.eclipse.che.wsagent.server;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.websocket.server.ServerEndpoint;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurator;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketEndpoint;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;
import org.eclipse.che.api.core.websocket.impl.MessagesReSender;
import org.eclipse.che.api.core.websocket.impl.WebSocketSessionRegistry;
import org.eclipse.che.api.core.websocket.impl.WebsocketIdService;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;

/**
 * Implementation of BasicWebSocketEndpoint for Che packaging. Add only mapping "/wsagent".
 *
 * @author Vitalii Parfonov
 */
@ServerEndpoint(value = "/wsagent", configurator = GuiceInjectorEndpointConfigurator.class)
public class CheWebSocketEndpoint extends BasicWebSocketEndpoint {

  private static final Logger LOG = getLogger(CheWebSocketEndpoint.class);

  public static final String ENDPOINT_ID = "ws-agent-websocket-endpoint";

  @Inject
  public CheWebSocketEndpoint(
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

  public static class CheWebSocketEndpointConfiguration
      implements RequestProcessorConfigurator.Configuration {

    private final ExecutorService executor;

    @Inject
    public CheWebSocketEndpointConfiguration(
        @Named("che.core.jsonrpc.major_executor") ExecutorService executor) {
      this.executor = executor;
    }

    @Override
    public String getEndpointId() {
      return ENDPOINT_ID;
    }

    @Override
    public ExecutorService getExecutorService() {
      return executor;
    }
  }

  @Singleton
  public static class CheWebSocketEndpointExecutorServiceProvider
      implements Provider<ExecutorService> {

    private final ThreadPoolExecutor executor;

    @Inject
    public CheWebSocketEndpointExecutorServiceProvider(
        @Named("che.core.jsonrpc.processor_core_pool_size") int corePoolSize,
        @Named("che.core.jsonrpc.processor_max_pool_size") int maxPoolSize,
        @Named("che.core.jsonrpc.processor_queue_capacity") int queueCapacity) {
      ThreadFactory factory =
          new ThreadFactoryBuilder()
              .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
              .setNameFormat(CheWebSocketEndpoint.class.getSimpleName() + "-%d")
              .setDaemon(true)
              .build();

      executor =
          new ThreadPoolExecutor(
              corePoolSize,
              maxPoolSize,
              60L,
              SECONDS,
              queueCapacity > 0
                  ? new LinkedBlockingQueue<>(queueCapacity)
                  : new SynchronousQueue<>(),
              factory);
      executor.setRejectedExecutionHandler(
          (r, __) -> LOG.error("Message {} rejected for execution", r));
      executor.prestartCoreThread();
    }

    @Override
    public ExecutorService get() {
      return executor;
    }
  }
}
