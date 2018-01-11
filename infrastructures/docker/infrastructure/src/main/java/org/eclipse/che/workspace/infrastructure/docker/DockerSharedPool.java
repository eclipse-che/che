/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.LoggerFactory;

/**
 * Provides a single non-daemon {@link ExecutorService} instance for docker infrastructure
 * components.
 */
@Singleton
public class DockerSharedPool {

  private final ExecutorService executor =
      Executors.newFixedThreadPool(
          Runtime.getRuntime().availableProcessors(), // <- experimental value
          new ThreadFactoryBuilder()
              .setNameFormat("DockerSharedPool-%d")
              .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
              .setDaemon(false)
              .build());

  /**
   * Delegates call to {@link ExecutorService#execute(Runnable)} and propagates thread locals to it
   * like defined by {@link ThreadLocalPropagateContext}.
   */
  public void execute(Runnable runnable) {
    executor.execute(ThreadLocalPropagateContext.wrap(runnable));
  }

  @PreDestroy
  private void terminate() throws InterruptedException {
    if (!executor.isShutdown()) {
      executor.shutdown();
      if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
        executor.shutdownNow();
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
          LoggerFactory.getLogger(DockerSharedPool.class)
              .error("Couldn't terminate docker infrastructure thread pool");
        }
      }
    }
  }
}
