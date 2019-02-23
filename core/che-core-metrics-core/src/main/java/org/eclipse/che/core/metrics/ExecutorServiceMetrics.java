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
package org.eclipse.che.core.metrics;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that is adding monitoring rejected messages ability to standard {@link
 * io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics} metrics
 */
public class ExecutorServiceMetrics implements MeterBinder {

  private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceMetrics.class);

  private final Tags tags;

  private final RejectedExecutionHandlerWrapper rejectedExecutionHandlerWrapper;

  public ExecutorServiceMetrics(
      RejectedExecutionHandlerWrapper rejectedExecutionHandlerWrapper,
      String executorServiceName,
      Iterable<Tag> tags) {
    this.rejectedExecutionHandlerWrapper = rejectedExecutionHandlerWrapper;
    this.tags = Tags.concat(tags, "name", executorServiceName);
  }

  public static ExecutorService monitor(
      MeterRegistry registry, ExecutorService executor, String executorName, Iterable<Tag> tags) {

    if (executor instanceof ThreadPoolExecutor) {
      LOG.debug("Adding rejection monitoring for {} {}", executor, executorName);
      monitorRejections(registry, (ThreadPoolExecutor) executor, executorName, tags);
    }
    return io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics.monitor(
        registry, executor, executorName, tags);
  }

  public static ExecutorService monitorRejections(
      MeterRegistry registry,
      ThreadPoolExecutor executor,
      String executorName,
      Iterable<Tag> tags) {

    RejectedExecutionHandlerWrapper rejectedExecutionHandler =
        new RejectedExecutionHandlerWrapper(executor.getRejectedExecutionHandler());
    executor.setRejectedExecutionHandler(rejectedExecutionHandler);
    new ExecutorServiceMetrics(rejectedExecutionHandler, executorName, tags).bindTo(registry);
    return io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics.monitor(
        registry, executor, executorName, tags);
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    FunctionCounter.builder(
            "executor.rejected",
            rejectedExecutionHandlerWrapper,
            RejectedExecutionHandlerWrapper::getCounter)
        .tags(tags)
        .description("The total number of tasks that have been rejected for execution")
        .baseUnit("tasks")
        .register(registry);
  }

  private static class RejectedExecutionHandlerWrapper
      implements java.util.concurrent.RejectedExecutionHandler {

    private final RejectedExecutionHandler handler;
    private volatile long counter;

    private RejectedExecutionHandlerWrapper(RejectedExecutionHandler handler) {
      this.handler = handler;
    }

    @Override
    public String toString() {
      return "RejectedExecutionHandlerWrapper{"
          + "handler="
          + handler
          + ", counter="
          + counter
          + '}';
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      counter++;
      if (handler != null) {
        handler.rejectedExecution(r, executor);
      }
    }

    public long getCounter() {
      return this.counter;
    }
  }
}
