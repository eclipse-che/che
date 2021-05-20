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

import static org.eclipse.che.commons.test.AssertRetry.assertWithRetry;

import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.text.ParseException;
import java.util.concurrent.CountDownLatch;
import org.eclipse.che.commons.schedule.executor.CronExpression;
import org.eclipse.che.commons.schedule.executor.CronThreadPoolExecutor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TimedCronExecutorServiceTest {

  private SimpleMeterRegistry registry;

  private Iterable<Tag> userTags = Tags.of("userTagKey", "userTagValue");

  @BeforeMethod
  public void setup() {
    registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
  }

  @Test
  public void executor() throws InterruptedException, ParseException {
    // given
    TimedCronExecutorService executorService =
        new TimedCronExecutorService(
            registry,
            new CronThreadPoolExecutor(1),
            TimedCronExecutorServiceTest.class.getName(),
            userTags);
    CountDownLatch lock = new CountDownLatch(1);
    // when
    executorService.schedule(
        () -> {
          lock.countDown();
        },
        // one time per second
        new CronExpression(" * * * ? * * *"));

    lock.await();
    // then
    assertWithRetry(
        () ->
            registry
                .get("executor.scheduled.cron")
                .tags(userTags)
                .tag("name", TimedCronExecutorServiceTest.class.getName())
                .counter()
                .count(),
        1.0,
        10,
        50);

    assertWithRetry(
        () ->
            registry
                .get("executor")
                .tags(userTags)
                .tag("name", TimedCronExecutorServiceTest.class.getName())
                .timer()
                .count(),
        1L,
        10,
        50);
  }
}
