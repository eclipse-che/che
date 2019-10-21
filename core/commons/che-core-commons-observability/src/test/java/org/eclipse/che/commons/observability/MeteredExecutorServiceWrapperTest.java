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

import static org.testng.Assert.assertEquals;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    Future<?> future =
        executor.submit(
            () -> {
              runnableTaskStart.countDown();
            });
    // then
    runnableTaskStart.await(10, TimeUnit.SECONDS);
    future.get(1, TimeUnit.MINUTES);

    assertEquals(
        registry
            .get("executor.thread.terminated")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .counter()
            .count(),
        0.0);
    assertEquals(
        registry
            .get("executor.thread.running")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .gauge()
            .value(),
        1.0);
    assertEquals(
        registry
            .get("executor.thread.created")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .counter()
            .count(),
        1.0);
    assertEquals(
        registry
            .get("executor.rejected")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .counter()
            .count(),
        0.0);

    assertEquals(
        registry
            .get("executor.pool.size")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .gauge()
            .value(),
        1.0);
    assertEquals(
        registry
            .get("executor.completed")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .functionCounter()
            .count(),
        1.0);
    assertEquals(
        registry
            .get("executor.queued")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .gauge()
            .value(),
        0.0);
    assertEquals(
        registry
            .get("executor.idle")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .timer()
            .count(),
        1);
    assertTrue(
        registry
                .get("executor.queue.remaining")
                .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
                .tags(userTags)
                .gauge()
                .value()
            > 0.0);
    assertEquals(
        registry
            .get("executor")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .timer()
            .count(),
        1);
    assertEquals(
        registry
            .get("executor.active")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .gauge()
            .value(),
        0.0);
  }

  @Test(enabled = false)
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
        .scheduleAtFixedRate(
            () -> {
              runnableTaskStart.countDown();
            },
            0,
            100,
            TimeUnit.SECONDS);
    // then

    runnableTaskStart.await(10, TimeUnit.SECONDS);

    assertEquals(
        registry
            .get("executor.thread.terminated")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .counter()
            .count(),
        0.0);
    assertEquals(
        registry
            .get("executor.thread.running")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .gauge()
            .value(),
        1.0);
    assertEquals(
        registry
            .get("executor.thread.created")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .counter()
            .count(),
        1.0);
    assertEquals(
        registry
            .get("executor.rejected")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .counter()
            .count(),
        0.0);

    assertEquals(
        registry
            .get("executor.pool.size")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .gauge()
            .value(),
        1.0);
    assertEquals(
        registry
            .get("executor.completed")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .functionCounter()
            .count(),
        1.0);
    assertEquals(
        registry
            .get("executor.queued")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .gauge()
            .value(),
        1.0);
    assertEquals(
        registry
            .get("executor.idle")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .timer()
            .count(),
        0);
    assertTrue(
        registry
                .get("executor.queue.remaining")
                .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
                .tags(userTags)
                .gauge()
                .value()
            > 0.0);
    assertEquals(
        registry
            .get("executor")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .timer()
            .count(),
        1);
    assertEquals(
        registry
            .get("executor.active")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .gauge()
            .value(),
        0.0);
    assertEquals(
        registry
            .get("executor.scheduled.once")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .counter()
            .count(),
        0.0);
    assertEquals(
        registry
            .get("executor.scheduled.repetitively")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .counter()
            .count(),
        1.0);
  }

  @Test(enabled = false)
  public void shouldRecordCronExecutorServiceMetrics() throws InterruptedException, ParseException {
    // given
    CronExecutorService executor =
        executorServiceWrapper.wrap(
            new CronThreadPoolExecutor(1),
            MeteredExecutorServiceWrapperTest.class.getName(),
            "userTagKey",
            "userTagValue");
    CountDownLatch runnableTaskStart = new CountDownLatch(1);
    final AtomicInteger i = new AtomicInteger(0);
    // when
    executor.schedule(
        () -> {
          runnableTaskStart.countDown();
          if (i.getAndIncrement() > 0) {
            try {
              Thread.sleep(1000000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        },
        new CronExpression(" * * * ? * * *"));
    // then
    runnableTaskStart.await(10, TimeUnit.SECONDS);
    assertEquals(
        registry
            .get("executor.thread.terminated")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .counter()
            .count(),
        0.0);
    assertEquals(
        registry
            .get("executor.thread.running")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .gauge()
            .value(),
        2.0);
    assertEquals(
        registry
            .get("executor.thread.created")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .counter()
            .count(),
        2.0);
    assertEquals(
        registry
            .get("executor.rejected")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .counter()
            .count(),
        0.0);

    assertEquals(
        registry
            .get("executor.pool.size")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .gauge()
            .value(),
        2.0);
    assertEquals(
        registry
            .get("executor.completed")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .functionCounter()
            .count(),
        1.0);
    assertTrue(
        registry
                .get("executor.queued")
                .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
                .tags(userTags)
                .gauge()
                .value()
            >= 1.0);
    assertEquals(
        registry
            .get("executor.idle")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .timer()
            .count(),
        0);
    assertTrue(
        registry
                .get("executor.queue.remaining")
                .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
                .tags(userTags)
                .gauge()
                .value()
            > 0.0);
    assertEquals(
        registry
            .get("executor")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .timer()
            .count(),
        1);
    assertTrue(
        registry
                .get("executor.active")
                .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
                .tags(userTags)
                .gauge()
                .value()
            >= 1.0);
    assertEquals(
        registry
            .get("executor.scheduled.once")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .counter()
            .count(),
        0.0);
    assertEquals(
        registry
            .get("executor.scheduled.repetitively")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .counter()
            .count(),
        0.0);
    assertEquals(
        registry
            .get("executor.scheduled.cron")
            .tag("name", MeteredExecutorServiceWrapperTest.class.getName())
            .tags(userTags)
            .counter()
            .count(),
        1.0);
  }
}
