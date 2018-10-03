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

@Singleton
public class ServerSideRequestProcessor implements RequestProcessor {
  private ExecutorService executorService;
  private final Integer poolSize;

  @Inject
  public ServerSideRequestProcessor(
      @Named("che.server.jsonrpc.processor_pool_size") String poolSize) {
    this.poolSize = Integer.parseInt(poolSize);
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
            0, poolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), factory);
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
