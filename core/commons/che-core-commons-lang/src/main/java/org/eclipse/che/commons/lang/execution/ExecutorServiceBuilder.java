/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.lang.execution;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Helper class to build {@link ExecutorService} from parts like corePoolSize, maxPoolSize,
 * threadFactory, etc.
 *
 * @author Sergii Kabashniuk
 */
public class ExecutorServiceBuilder {

  private int corePoolSize;
  private int maxPoolSize;
  private boolean allowCoreThreadTimeOut;
  private Duration keepAliveTime;
  private BlockingQueue<Runnable> workQueue;
  private ThreadFactory threadFactory;
  private RejectedExecutionHandler handler;

  public ExecutorServiceBuilder() {
    this.corePoolSize = 0;
    this.maxPoolSize = 1;
    this.allowCoreThreadTimeOut = false;
    this.keepAliveTime = Duration.ofSeconds(60);
    this.workQueue = new LinkedBlockingQueue<>();
    this.threadFactory = Executors.defaultThreadFactory();
    this.handler = new ThreadPoolExecutor.AbortPolicy();
  }

  /**
   * @param corePoolSize - configure corePoolSize the number of threads to keep in the pool, even if
   *     they are idle, unless {@code allowCoreThreadTimeOut} is set.
   */
  public ExecutorServiceBuilder corePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
    return this;
  }

  /** @param maxPoolSize - configure the maximum number of threads to allow in the pool. */
  public ExecutorServiceBuilder maxPoolSize(int maxPoolSize) {
    if (maxPoolSize < corePoolSize) {
      throw new IllegalArgumentException("maxPoolSize must be greater than corePoolSize");
    }
    this.maxPoolSize = maxPoolSize;
    return this;
  }

  /** @param allowCoreThreadTimeOut - allow core threads to time out an terminate. */
  public ExecutorServiceBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
    this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    return this;
  }

  /** @param time - configure keepAliveTime parameter of {@code ThreadPoolExecutor} */
  public ExecutorServiceBuilder keepAliveTime(Duration time) {
    this.keepAliveTime = time;
    return this;
  }

  /**
   * @param workQueue - configure the type of the task queue that would be used in {@code
   *     ThreadPoolExecutor}
   */
  public ExecutorServiceBuilder workQueue(BlockingQueue<Runnable> workQueue) {
    this.workQueue = workQueue;
    return this;
  }

  /**
   * @param queueCapacity - configure {@code LinkedBlockingQueue} with queueCapacity capacity to be
   *     used in {@code ThreadPoolExecutor}
   */
  public ExecutorServiceBuilder queueCapacity(int queueCapacity) {
    this.workQueue = new LinkedBlockingQueue<>(queueCapacity);
    return this;
  }

  /**
   * @param handler - configure instance of {@code RejectedExecutionHandler} to be used in {@code
   *     ThreadPoolExecutor}
   */
  public ExecutorServiceBuilder rejectedExecutionHandler(RejectedExecutionHandler handler) {
    this.handler = handler;
    return this;
  }

  /**
   * @param threadFactory - configure instance of {@code ThreadFactory} to be used in {@code
   *     ThreadPoolExecutor}
   */
  public ExecutorServiceBuilder threadFactory(ThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
    return this;
  }

  public ExecutorService build() {

    final ThreadPoolExecutor executor =
        new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            keepAliveTime.toMillis(),
            TimeUnit.MILLISECONDS,
            workQueue,
            threadFactory,
            handler);
    executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);

    return executor;
  }
}
