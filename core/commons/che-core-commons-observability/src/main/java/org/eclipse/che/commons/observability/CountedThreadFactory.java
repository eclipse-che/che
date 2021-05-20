/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.BaseUnits;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import javax.validation.constraints.NotNull;

/** A {@link ThreadFactory} that monitors the number of threads created, running and terminated. */
public class CountedThreadFactory implements ThreadFactory {

  private final ThreadFactory delegate;
  private final Counter created;
  private final AtomicInteger running = new AtomicInteger(0);
  private final Counter terminated;

  /**
   * Wraps a {@link ThreadFactory} with an explicit name and records the number of created, running
   * and terminated threads.
   *
   * @param delegate {@link ThreadFactory} to wrap.
   * @param registry {@link MeterRegistry} that will contain the metrics.
   * @param name name for this delegate.
   * @param tags tags that can provide additional context.
   */
  public CountedThreadFactory(
      ThreadFactory delegate, MeterRegistry registry, String name, Iterable<Tag> tags) {
    this.delegate = delegate;
    this.created =
        Counter.builder("thread.factory.created")
            .tags(Tags.concat(tags, "name", name))
            .description(
                "The approximate number of threads which were created with a thread factory")
            .baseUnit(BaseUnits.THREADS)
            .register(registry);
    this.terminated =
        Counter.builder("thread.factory.terminated")
            .tags(Tags.concat(tags, "name", name))
            .description("The approximate number of threads which have finished execution")
            .baseUnit(BaseUnits.THREADS)
            .register(registry);
    Gauge.builder("thread.factory.running", running, AtomicInteger::get)
        .tags(Tags.concat(tags, "name", name))
        .description(
            "The approximate number of threads which have started to execute, but have not terminated")
        .baseUnit(BaseUnits.THREADS)
        .register(registry);
  }

  /** {@inheritDoc} */
  @Override
  public Thread newThread(@NotNull Runnable runnable) {

    Thread thread =
        delegate.newThread(
            () -> {
              running.incrementAndGet();
              try {
                runnable.run();
              } finally {
                running.decrementAndGet();
                terminated.increment();
              }
            });
    created.increment();
    return thread;
  }

  public static void monitorThreads(
      MeterRegistry registry, ThreadPoolExecutor executor, String name, Iterable<Tag> tags) {
    executor.setThreadFactory(
        new CountedThreadFactory(executor.getThreadFactory(), registry, name, tags));
  }
}
