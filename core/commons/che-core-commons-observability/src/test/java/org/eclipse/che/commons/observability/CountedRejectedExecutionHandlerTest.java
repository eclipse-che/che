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

import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CountedRejectedExecutionHandlerTest {

  private SimpleMeterRegistry registry;
  private Iterable<Tag> userTags = Tags.of("userTagKey", "userTagValue");

  @BeforeMethod
  public void setup() {
    registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
  }

  @Test
  public void countRejections() {
    // given
    ThreadPoolExecutor executor =
        new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
    executor.setRejectedExecutionHandler((r, executor1) -> {});

    CountedRejectedExecutionHandler.monitorRejections(
        registry, executor, CountedRejectedExecutionHandler.class.getName(), userTags);
    CountDownLatch runnableTaskComplete = new CountDownLatch(1);
    Runnable stub =
        () -> {
          try {
            runnableTaskComplete.await(10, TimeUnit.SECONDS);
          } catch (InterruptedException e) {
            throw new IllegalStateException("runnable interrupted before completion");
          }
        };
    executor.submit(stub);

    // then
    for (int i = 0; i < 14; i++) {
      executor.submit(
          () -> {
            // do nothing. Task has to be rejected.
          });
    }
    // when
    assertEquals(
        registry
            .get("executor.rejected")
            .tags(userTags)
            .tag("name", CountedRejectedExecutionHandler.class.getName())
            .counter()
            .count(),
        14.0);
    // cleanup
    runnableTaskComplete.countDown();
    executor.shutdownNow();
  }
}
