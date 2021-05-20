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
package io.micrometer.core.instrument.internal;

import static java.util.stream.Collectors.toList;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.che.commons.schedule.executor.CronExecutorService;
import org.eclipse.che.commons.schedule.executor.CronExpression;

/**
 * A {@link CronExecutorService} that is timed. It provides the same metrics as {@link
 * io.micrometer.core.instrument.internal.TimedScheduledExecutorService} plus adds one extra counter
 * {@code executor.scheduled.cron}. The counter represents the number of invocations of method
 * {@link CronExecutorService#schedule(Runnable, CronExpression)}
 *
 * @author Sergii Kabashniuk
 */
public class TimedCronExecutorService implements CronExecutorService {
  private final CronExecutorService delegate;

  private final Counter scheduledCron;

  private final MeterRegistry registry;
  private final Timer executionTimer;
  private final Timer idleTimer;
  private final Counter scheduledOnce;
  private final Counter scheduledRepetitively;

  public TimedCronExecutorService(
      MeterRegistry registry,
      CronExecutorService delegate,
      String executorServiceName,
      Iterable<Tag> tags) {
    this.registry = registry;
    this.delegate = delegate;
    this.executionTimer =
        registry.timer("executor", Tags.concat(tags, "name", executorServiceName));
    this.idleTimer =
        registry.timer("executor.idle", Tags.concat(tags, "name", executorServiceName));
    this.scheduledOnce =
        registry.counter("executor.scheduled.once", Tags.concat(tags, "name", executorServiceName));
    this.scheduledRepetitively =
        registry.counter(
            "executor.scheduled.repetitively", Tags.concat(tags, "name", executorServiceName));
    this.scheduledCron =
        registry.counter("executor.scheduled.cron", Tags.concat(tags, "name", executorServiceName));
  }

  @Override
  public void shutdown() {
    delegate.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return delegate.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return delegate.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return delegate.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return delegate.awaitTermination(timeout, unit);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return delegate.submit(wrap(task));
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return delegate.submit(wrap(task), result);
  }

  @Override
  public Future<?> submit(Runnable task) {
    return delegate.submit(wrap(task));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    return delegate.invokeAll(wrapAll(tasks));
  }

  @Override
  public <T> List<Future<T>> invokeAll(
      Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException {
    return delegate.invokeAll(wrapAll(tasks), timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    return delegate.invokeAny(wrapAll(tasks));
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return delegate.invokeAny(wrapAll(tasks), timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    delegate.execute(wrap(command));
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    scheduledOnce.increment();
    return delegate.schedule(executionTimer.wrap(command), delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    scheduledOnce.increment();
    return delegate.schedule(executionTimer.wrap(callable), delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(
      Runnable command, long initialDelay, long period, TimeUnit unit) {
    scheduledRepetitively.increment();
    return delegate.scheduleAtFixedRate(executionTimer.wrap(command), initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(
      Runnable command, long initialDelay, long delay, TimeUnit unit) {
    scheduledRepetitively.increment();
    return delegate.scheduleWithFixedDelay(executionTimer.wrap(command), initialDelay, delay, unit);
  }

  private Runnable wrap(Runnable task) {
    return new TimedRunnable(registry, executionTimer, idleTimer, task);
  }

  private <T> Callable<T> wrap(Callable<T> task) {
    return new TimedCallable<>(registry, executionTimer, idleTimer, task);
  }

  private <T> Collection<? extends Callable<T>> wrapAll(Collection<? extends Callable<T>> tasks) {
    return tasks.stream().map(this::wrap).collect(toList());
  }

  @Override
  public Future<?> schedule(Runnable task, CronExpression expression) {
    scheduledCron.increment();
    return delegate.schedule(executionTimer.wrap(task), expression);
  }
}
