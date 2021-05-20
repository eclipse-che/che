/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.commons.observability.ExecutorServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a single non-daemon {@link ExecutorService} instance for workspace components.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceSharedPool {

  private final ExecutorService executor;

  @Inject
  public WorkspaceSharedPool(
      @Named("che.workspace.pool.type") String poolType,
      @Named("che.workspace.pool.exact_size") @Nullable String exactSizeProp,
      @Named("che.workspace.pool.cores_multiplier") @Nullable String coresMultiplierProp,
      ExecutorServiceWrapper wrapper) {

    ThreadFactory factory =
        new ThreadFactoryBuilder()
            .setNameFormat("WorkspaceSharedPool-%d")
            .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
            .setDaemon(false)
            .build();
    switch (poolType.toLowerCase()) {
      case "cached":
        executor =
            wrapper.wrap(
                Executors.newCachedThreadPool(factory), WorkspaceSharedPool.class.getName());
        break;
      case "fixed":
        Integer exactSize = exactSizeProp == null ? null : Ints.tryParse(exactSizeProp);
        int size;
        if (exactSize != null && exactSize > 0) {
          size = exactSize;
        } else {
          size = Runtime.getRuntime().availableProcessors();
          Integer coresMultiplier =
              coresMultiplierProp == null ? null : Ints.tryParse(coresMultiplierProp);
          if (coresMultiplier != null && coresMultiplier > 0) {
            size *= coresMultiplier;
          }
        }
        executor =
            wrapper.wrap(
                Executors.newFixedThreadPool(size, factory), WorkspaceSharedPool.class.getName());
        break;
      default:
        throw new IllegalArgumentException(
            "The type of the pool '" + poolType + "' is not supported");
    }
  }

  /**
   * Returns an {@link ExecutorService} managed by this pool instance. The executor service is
   * tracing aware and will propagate the active tracing span, if any, to the submitted tasks.
   */
  public ExecutorService getExecutor() {
    return executor;
  }

  /**
   * Delegates call to {@link ExecutorService#execute(Runnable)} and propagates thread locals to it
   * like defined by {@link ThreadLocalPropagateContext}.
   */
  public void execute(Runnable runnable) {
    executor.execute(ThreadLocalPropagateContext.wrap(runnable));
  }

  /**
   * Delegates call to {@link ExecutorService#submit(Callable)} and propagates thread locals to it
   * like defined by {@link ThreadLocalPropagateContext}.
   */
  public <T> Future<T> submit(Callable<T> callable) {
    return executor.submit(ThreadLocalPropagateContext.wrap(callable));
  }

  /**
   * Asynchronously runs the given task wrapping it with {@link
   * ThreadLocalPropagateContext#wrap(Runnable)}
   *
   * @param runnable task to run
   * @return completable future bounded to the task
   */
  public CompletableFuture<Void> runAsync(Runnable runnable) {
    return CompletableFuture.runAsync(ThreadLocalPropagateContext.wrap(runnable), executor);
  }

  /** Terminates this pool if it's not terminated yet. */
  void shutdown() {
    if (!executor.isShutdown()) {
      Logger logger = LoggerFactory.getLogger(getClass());
      executor.shutdown();
      try {
        logger.info("Shutdown workspace threads pool, wait 30s to stop normally");
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
          executor.shutdownNow();
          logger.info("Interrupt workspace threads pool, wait 60s to stop");
          if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.error("Couldn't shutdown workspace threads pool");
          }
        }
      } catch (InterruptedException x) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
      logger.info("Workspace threads pool is terminated");
    }
  }
}
