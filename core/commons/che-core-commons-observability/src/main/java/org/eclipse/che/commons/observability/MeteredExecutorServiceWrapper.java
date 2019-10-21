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
package org.eclipse.che.commons.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.core.lang.Nullable;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import javax.inject.Inject;
import org.eclipse.che.commons.schedule.executor.CronExecutorService;

/**
 * Implementation of {@code ExecutorServiceWrapper} that add all sort of monitoring capabilities
 * from {@code ExecutorServiceMetrics}.
 *
 * <p>Also in case if provided executor is instance of {@code ThreadPoolExecutor} it will add
 * metrics provided by {@code CountedThreadFactory} and {@code CountedRejectedExecutionHandler}.
 */
public class MeteredExecutorServiceWrapper implements ExecutorServiceWrapper {
  private final MeterRegistry meterRegistry;

  @Inject
  public MeteredExecutorServiceWrapper(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public ScheduledExecutorService wrap(
      ScheduledExecutorService executor, String name, String... tags) {

    monitorThreadPoolExecutor(executor, name, tags);
    return ExecutorServiceMetrics.monitor(meterRegistry, executor, name, Tags.of(tags));
  }

  @Override
  public ExecutorService wrap(ExecutorService executor, String name, String... tags) {
    monitorThreadPoolExecutor(executor, name, tags);
    return ExecutorServiceMetrics.monitor(meterRegistry, executor, name, Tags.of(tags));
  }

  @Override
  public CronExecutorService wrap(CronExecutorService executor, String name, String... tags) {
    monitorThreadPoolExecutor(executor, name, tags);
    new io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics(
            executor, name, Tags.of(tags))
        .bindTo(meterRegistry);

    return new TimedCronExecutorService(meterRegistry, executor, name, Tags.of(tags));
  }

  private void monitorThreadPoolExecutor(ExecutorService executor, String name, String... tags) {
    String className = executor.getClass().getName();
    if (executor instanceof ThreadPoolExecutor) {
      CountedThreadFactory.monitorThreads(
          meterRegistry, (ThreadPoolExecutor) executor, name, Tags.of(tags));
      CountedRejectedExecutionHandler.monitorRejections(
          meterRegistry, (ThreadPoolExecutor) executor, name, Tags.of(tags));
    } else if (className.equals(
        "java.util.concurrent.Executors$DelegatedScheduledExecutorService")) {
      ThreadPoolExecutor unwrappedExecutor =
          unwrapThreadPoolExecutor(executor, executor.getClass());
      CountedThreadFactory.monitorThreads(meterRegistry, unwrappedExecutor, name, Tags.of(tags));
      CountedRejectedExecutionHandler.monitorRejections(
          meterRegistry, unwrappedExecutor, name, Tags.of(tags));
    } else if (className.equals(
        "java.util.concurrent.Executors$FinalizableDelegatedExecutorService")) {
      ThreadPoolExecutor unwrappedExecutor =
          unwrapThreadPoolExecutor(executor, executor.getClass().getSuperclass());
      CountedThreadFactory.monitorThreads(meterRegistry, unwrappedExecutor, name, Tags.of(tags));
      CountedRejectedExecutionHandler.monitorRejections(
          meterRegistry, unwrappedExecutor, name, Tags.of(tags));
    }
  }
  /**
   * Every ScheduledThreadPoolExecutor created by {@link Executors} is wrapped. Also, {@link
   * Executors#newSingleThreadExecutor()} wrap a regular {@link ThreadPoolExecutor}.
   */
  @Nullable
  private ThreadPoolExecutor unwrapThreadPoolExecutor(ExecutorService executor, Class<?> wrapper) {
    try {
      Field e = wrapper.getDeclaredField("e");
      e.setAccessible(true);
      return (ThreadPoolExecutor) e.get(executor);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      // Do nothing. We simply can't get to the underlying ThreadPoolExecutor.
    }
    return null;
  }
}
