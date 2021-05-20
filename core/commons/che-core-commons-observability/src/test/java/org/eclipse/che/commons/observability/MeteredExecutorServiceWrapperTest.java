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

import static org.eclipse.che.commons.test.AssertRetry.assertWithRetry;
import static org.testng.Assert.assertTrue;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.text.ParseException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.che.commons.schedule.executor.CronExecutorService;
import org.eclipse.che.commons.schedule.executor.CronExpression;
import org.eclipse.che.commons.schedule.executor.CronThreadPoolExecutor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MeteredExecutorServiceWrapperTest {

  private MeterRegistry registry;

  private ExecutorServiceWrapper executorServiceWrapper;
  private Iterable<Tag> userTags = Tags.of("userTagKey", "userTagValue");

  private ExecutorService executor;

  @BeforeMethod
  public void setup() {
    registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
    executorServiceWrapper = new MeteredExecutorServiceWrapper(registry);
  }

  @AfterMethod
  public void cleanup() {
    if (executor == null) return;
    // Tell threads to finish off.
    executor.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!executor.awaitTermination(60, TimeUnit.SECONDS))
          System.out.println("Pool did not terminate");
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      executor.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }

  @Test
  public void shouldRecordExecutorServiceMetrics()
      throws InterruptedException, TimeoutException, ExecutionException {
    // given
    executor =
        executorServiceWrapper.wrap(
            Executors.newSingleThreadExecutor(),
            MeteredExecutorServiceWrapperTest.class.getName(),
            "userTagKey",
            "userTagValue");
    CountDownLatch runnableTaskStart = new CountDownLatch(1);
    // when
    Future<?> future = executor.submit(runnableTaskStart::countDown);
    // then
    runnableTaskStart.await(10, TimeUnit.SECONDS);
    future.get(1, TimeUnit.MINUTES);

    assertCounter("thread.factory.terminated", 0.0);
    assertGauge("thread.factory.running", 1.0);
    assertCounter("thread.factory.created", 1.0);
    assertCounter("executor.rejected", 0.0);
    assertGauge("executor.pool.size", 1.0);
    assertFunctionCounter("executor.completed", 1.0);
    assertGauge("executor.queued", 0.0);
    assertTimerCount("executor.idle", 1L);
    assertGauge("executor.queue.remaining", (double) Integer.MAX_VALUE);
    assertTimerCount("executor", 1L);
    assertGauge("executor.active", 0.0);
  }

  @Test
  public void shouldRecordScheduledExecutorServiceMetrics() throws InterruptedException {
    // given
    executor =
        executorServiceWrapper.wrap(
            Executors.newSingleThreadScheduledExecutor(),
            MeteredExecutorServiceWrapperTest.class.getName(),
            "userTagKey",
            "userTagValue");
    CountDownLatch runnableTaskStart = new CountDownLatch(1);
    // when
    ((ScheduledExecutorService) executor)
        .scheduleAtFixedRate(runnableTaskStart::countDown, 0, 100, TimeUnit.SECONDS);
    // then

    runnableTaskStart.await(10, TimeUnit.SECONDS);

    assertCounter("thread.factory.terminated", 0.0);
    assertGauge("thread.factory.running", 1.0);
    assertCounter("thread.factory.created", 1.0);
    assertCounter("executor.rejected", 0.0);
    assertGauge("executor.pool.size", 1.0);
    assertFunctionCounter("executor.completed", 1.0);
    assertGauge("executor.queued", 1.0);
    assertTimerCount("executor.idle", 0L);
    assertGauge("executor.queue.remaining", (double) Integer.MAX_VALUE);
    assertTimerCount("executor", 1L);
    assertGauge("executor.active", 0.0);
    assertCounter("executor.scheduled.once", 0.0);
    assertCounter("executor.scheduled.repetitively", 1.0);
  }

  @Test
  public void shouldRecordCronExecutorServiceMetrics() throws InterruptedException, ParseException {
    // given
    CronExecutorService executor =
        executorServiceWrapper.wrap(
            new CronThreadPoolExecutor(1),
            MeteredExecutorServiceWrapperTest.class.getName(),
            "userTagKey",
            "userTagValue");
    CountDownLatch runnableTaskStart = new CountDownLatch(1);
    // when
    executor.schedule(runnableTaskStart::countDown, new CronExpression(" * * * ? * * *"));
    // then
    runnableTaskStart.await(10, TimeUnit.SECONDS);

    assertCounter("thread.factory.terminated", 0.0);
    assertGauge("thread.factory.running", 2.0);
    assertCounter("thread.factory.created", 2.0);
    assertCounter("executor.rejected", 0.0);
    assertGauge("executor.pool.size", 2.0);
    assertFunctionCounter("executor.completed", 1.0);
    assertGauge("executor.queued", 1.0);
    assertTimerCount("executor.idle", 0L);
    assertTrue(
        registry
                .get("executor.queue.remaining")
                .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
                .tags(userTags)
                .gauge()
                .value()
            > 0.0);

    assertTimerCount("executor", 1L);
    assertGauge("executor.active", 1.0);
    assertCounter("executor.scheduled.once", 0.0);
    assertCounter("executor.scheduled.repetitively", 0.0);
    assertCounter("executor.scheduled.cron", 1.0);
  }

  public void assertCounter(String counterName, Double value) throws InterruptedException {
    assertWithRetry(
        () ->
            registry
                .get(counterName)
                .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
                .tags(userTags)
                .counter()
                .count(),
        value,
        20,
        50);
  }

  public void assertFunctionCounter(String counterName, Double value) throws InterruptedException {
    assertWithRetry(
        () ->
            registry
                .get(counterName)
                .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
                .tags(userTags)
                .functionCounter()
                .count(),
        value,
        20,
        50);
  }

  public void assertGauge(String gaugeName, Double value) throws InterruptedException {
    assertWithRetry(
        () ->
            registry
                .get(gaugeName)
                .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
                .tags(userTags)
                .gauge()
                .value(),
        value,
        20,
        50);
  }

  public void assertTimerCount(String timerName, Long value) throws InterruptedException {
    assertWithRetry(
        () ->
            registry
                .get(timerName)
                .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
                .tags(userTags)
                .timer()
                .count(),
        value,
        20,
        50);
  }
}
