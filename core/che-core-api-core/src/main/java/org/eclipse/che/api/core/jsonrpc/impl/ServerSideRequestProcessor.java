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

import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessor;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ServerSideRequestProcessor implements RequestProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(ServerSideRequestProcessor.class);

  private ExecutorService executorService;
  private final int maxPoolSize;

  @Inject
  public ServerSideRequestProcessor(
      @Named("che.core.jsonrpc.processor_max_pool_size") int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
    LOG.info(" che.core.jsonrpc.processor_max_pool_siz {}  ", maxPoolSize);
  }

  @PostConstruct
  private void postConstruct() {
    ThreadFactory factory =
        new ThreadFactoryBuilder()
            .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
            .setNameFormat(ServerSideRequestProcessor.class.getSimpleName())
            .setDaemon(true)
            .build();

    executorService =
        new ThreadPoolExecutor(
            0, maxPoolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), factory);
    ((ThreadPoolExecutor) executorService)
        .setRejectedExecutionHandler(
            (r, executor) -> LOG.warn("Message {} rejected for execution in {}", r, executor));
  }

  @PreDestroy
  private void preDestroy() {
    executorService.shutdown();
    try {
      if (executorService.awaitTermination(5, SECONDS)) {
        executorService.shutdownNow();
        executorService.awaitTermination(5, SECONDS);
      }
    } catch (InterruptedException ie) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void process(Runnable runnable) {
    executorService.execute(ThreadLocalPropagateContext.wrap(runnable));
  }
}
