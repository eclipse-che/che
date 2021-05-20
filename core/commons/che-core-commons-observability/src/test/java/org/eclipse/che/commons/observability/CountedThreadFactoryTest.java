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
package org.eclipse.che.commons.observability;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CountedThreadFactoryTest {

  private SimpleMeterRegistry registry;
  private Iterable<Tag> userTags = Tags.of("userTagKey", "userTagValue");

  @BeforeMethod
  public void setup() {
    registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
  }

  @Test
  public void shouldCountCreatedThreads() {
    // given
    ThreadFactory factory =
        new CountedThreadFactory(
            Executors.defaultThreadFactory(),
            registry,
            CountedThreadFactoryTest.class.getName(),
            userTags);
    // when
    factory.newThread(() -> {});

    // then
    assertEquals(
        registry
            .get("thread.factory.created")
            .tags(userTags)
            .tag("name", CountedThreadFactoryTest.class.getName())
            .counter()
            .count(),
        1.0);
  }

  @Test
  public void shouldCountRunningThreads() throws InterruptedException {
    // given
    ThreadFactory factory =
        new CountedThreadFactory(
            Executors.defaultThreadFactory(),
            registry,
            CountedThreadFactoryTest.class.getName(),
            userTags);

    CountDownLatch runnableTaskStart = new CountDownLatch(1);
    CountDownLatch runnableTaskComplete = new CountDownLatch(1);
    Runnable task =
        () -> {
          runnableTaskStart.countDown();
          try {
            Assert.assertTrue(runnableTaskComplete.await(10, TimeUnit.SECONDS));
          } catch (InterruptedException e) {
            throw new IllegalStateException("runnable interrupted before completion");
          }
        };

    // when
    Thread thread = factory.newThread(task);
    thread.start();

    // then
    runnableTaskStart.await();
    assertEquals(
        registry
            .get("thread.factory.running")
            .tags(userTags)
            .tag("name", CountedThreadFactoryTest.class.getName())
            .gauge()
            .value(),
        1.0);

    runnableTaskComplete.countDown();
    thread.join();
    assertEquals(
        registry
            .get("thread.factory.running")
            .tags(userTags)
            .tag("name", CountedThreadFactoryTest.class.getName())
            .gauge()
            .value(),
        0.0);

    // put here to ensure that thread are not GCd
    assertFalse(thread.isAlive());
    // put here to ensure that factory are not GCd
    assertNotNull(factory);
  }

  @Test
  public void shouldCountRunningAndTerminatedThreadsInExecutorPool()
      throws InterruptedException, TimeoutException, ExecutionException {
    // given
    JoinableThreadFactory factory =
        new JoinableThreadFactory(
            new CountedThreadFactory(
                Executors.defaultThreadFactory(),
                registry,
                CountedThreadFactoryTest.class.getName(),
                userTags));
    ExecutorService executor = Executors.newCachedThreadPool(factory);

    CountDownLatch runnableTaskStart = new CountDownLatch(10);
    CountDownLatch runnableTaskComplete = new CountDownLatch(1);

    Runnable task =
        () -> {
          runnableTaskStart.countDown();
          try {
            Assert.assertTrue(runnableTaskComplete.await(10, TimeUnit.SECONDS));
          } catch (InterruptedException e) {
            throw new IllegalStateException("runnable interrupted before completion");
          }
        };
    List<Future> futures = new ArrayList<>(10);
    for (int i = 0; i < 10; i++) {
      futures.add(executor.submit(task));
    }
    runnableTaskStart.await();
    assertEquals(
        registry
            .get("thread.factory.running")
            .tags(userTags)
            .tag("name", CountedThreadFactoryTest.class.getName())
            .gauge()
            .value(),
        10.0);
    assertEquals(
        registry
            .get("thread.factory.terminated")
            .tags(userTags)
            .tag("name", CountedThreadFactoryTest.class.getName())
            .counter()
            .count(),
        0.0);

    runnableTaskComplete.countDown();

    for (Future future : futures) {
      future.get(1, TimeUnit.MINUTES);
    }
    executor.shutdownNow();
    Assert.assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    factory.joinAll();
    assertEquals(
        registry
            .get("thread.factory.running")
            .tags(userTags)
            .tag("name", CountedThreadFactoryTest.class.getName())
            .gauge()
            .value(),
        0.0);
    assertEquals(
        registry
            .get("thread.factory.terminated")
            .tags(userTags)
            .tag("name", CountedThreadFactoryTest.class.getName())
            .counter()
            .count(),
        10.0);
    // put here to ensure that factory are not GCd
    assertNotNull(factory);
  }

  public static class JoinableThreadFactory implements ThreadFactory {

    private final ThreadFactory delegate;
    private final List<Thread> threads;

    public JoinableThreadFactory(ThreadFactory delegate) {
      this.delegate = delegate;
      this.threads = new ArrayList<>();
    }

    @Override
    public Thread newThread(Runnable r) {
      Thread result = delegate.newThread(r);
      threads.add(result);
      return result;
    }

    public void joinAll() throws InterruptedException {
      for (Thread thread : threads) {
        if (thread.isAlive()) {
          thread.join();
        }
      }
    }
  }
}
