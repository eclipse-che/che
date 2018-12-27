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
package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurator;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ServerSideRequestProcessorConfigurator implements RequestProcessorConfigurator {

  private static final Logger LOG =
      LoggerFactory.getLogger(ServerSideRequestProcessorConfigurator.class);

  private ExecutorService defaultExecutorService;
  private final int maxPoolSize;
  private final Map<String, Configuration> configurations = new ConcurrentHashMap<>();

  @Inject
  public ServerSideRequestProcessorConfigurator(
      @Named("che.core.jsonrpc.processor_max_pool_size") int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
    LOG.debug("che.core.jsonrpc.processor_max_pool_size {}  ", maxPoolSize);
  }

  @PostConstruct
  private void postConstruct() {
    ThreadFactory factory =
        new ThreadFactoryBuilder()
            .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
            .setNameFormat(ServerSideRequestProcessorConfigurator.class.getSimpleName() + "-%d")
            .setDaemon(true)
            .build();

    defaultExecutorService =
        new ThreadPoolExecutor(
            0, maxPoolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), factory);
    ((ThreadPoolExecutor) defaultExecutorService)
        .setRejectedExecutionHandler(
            (r, executor) -> LOG.warn("Message {} rejected for execution", r));
  }

  @Override
  public void put(String endpointId, Configuration configuration) {
    configurations.put(endpointId, configuration);
  }

  @Override
  public Configuration getOrNull(String endpointId) {
    return configurations.get(endpointId);
  }

  @Override
  public Configuration getOrDefault(String endpointId) {
    return configurations.getOrDefault(endpointId, () -> defaultExecutorService);
  }
}
