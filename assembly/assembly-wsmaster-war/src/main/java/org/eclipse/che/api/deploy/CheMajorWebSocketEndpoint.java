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
package org.eclipse.che.api.deploy;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.websocket.server.ServerEndpoint;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurator;
import org.eclipse.che.api.core.jsonrpc.impl.ServerSideRequestProcessor;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketEndpoint;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;
import org.eclipse.che.api.core.websocket.impl.MessagesReSender;
import org.eclipse.che.api.core.websocket.impl.WebSocketSessionRegistry;
import org.eclipse.che.api.core.websocket.impl.WebsocketIdService;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;

/**
 * Implementation of {@link BasicWebSocketEndpoint} for Che packaging. Add only mapping
 * "/websocket".
 */
@ServerEndpoint(value = "/websocket", configurator = GuiceInjectorEndpointConfigurator.class)
public class CheMajorWebSocketEndpoint extends BasicWebSocketEndpoint {
  private static final Logger LOG = getLogger(CheMajorWebSocketEndpoint.class);

  private final int maxPoolSize;
  private final RequestProcessorConfigurator requestProcessorConfigurator;

  private ThreadPoolExecutor executor;

  @Inject
  public CheMajorWebSocketEndpoint(
      WebSocketSessionRegistry registry,
      MessagesReSender reSender,
      WebSocketMessageReceiver receiver,
      WebsocketIdService websocketIdService,
      RequestProcessorConfigurator requestProcessorConfigurator,
      @Named("che.core.jsonrpc.processor_max_pool_size") int maxPoolSize) {
    super(registry, reSender, receiver, websocketIdService);
    this.maxPoolSize = maxPoolSize;
    this.requestProcessorConfigurator = requestProcessorConfigurator;
  }

  @Override
  protected String getEndpointId() {
    return "master-websocket-major-endpoint";
  }

  @PostConstruct
  private void postConstruct() {
    ThreadFactory factory =
        new ThreadFactoryBuilder()
            .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
            .setNameFormat(ServerSideRequestProcessor.class.getSimpleName() + "-%d")
            .setDaemon(true)
            .build();

    executor =
        new ThreadPoolExecutor(0, maxPoolSize, 60L, SECONDS, new SynchronousQueue<>(), factory);
    executor.setRejectedExecutionHandler(
        (r, __) -> LOG.warn("Message {} rejected for execution", r));
    requestProcessorConfigurator.put(getEndpointId(), () -> executor);
  }

  @PreDestroy
  private void preDestroy() {
    executor.shutdown();
    try {
      if (executor.awaitTermination(3, SECONDS)) {
        executor.shutdownNow();
        executor.awaitTermination(3, SECONDS);
      }
    } catch (InterruptedException ie) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
