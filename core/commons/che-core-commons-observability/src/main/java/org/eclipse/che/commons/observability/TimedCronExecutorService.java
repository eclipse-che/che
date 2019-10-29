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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.internal.TimedScheduledExecutorService;
import java.lang.reflect.Field;
import java.util.concurrent.Future;
import org.eclipse.che.commons.schedule.executor.CronExecutorService;
import org.eclipse.che.commons.schedule.executor.CronExpression;

/**
 * A {@link CronExecutorService} that is timed. It's inherits all metrics provided by {@link
 * TimedScheduledExecutorService} plus add one extra counter {@code executor.scheduled.cron}. The
 * counter represent number of invocations of method {@link CronExecutorService#schedule(Runnable,
 * CronExpression)}
 *
 * @author Sergii Kabashniuk
 */
public class TimedCronExecutorService extends TimedScheduledExecutorService
    implements CronExecutorService {
  private final CronExecutorService delegate;

  private final Timer executionTimer;

  private final Counter scheduledCron;

  public TimedCronExecutorService(
      MeterRegistry registry,
      CronExecutorService delegate,
      String executorServiceName,
      Iterable<Tag> tags) {
    super(registry, delegate, executorServiceName, tags);
    this.delegate = delegate;
    this.executionTimer = getTimer();
    this.scheduledCron =
        registry.counter("executor.scheduled.cron", Tags.concat(tags, "name", executorServiceName));
  }

  @Override
  public Future<?> schedule(Runnable task, CronExpression expression) {
    scheduledCron.increment();
    return delegate.schedule(executionTimer.wrap(task), expression);
  }

  protected Timer getTimer() {
    try {
      Field e = TimedScheduledExecutorService.class.getDeclaredField("executionTimer");
      e.setAccessible(true);
      return (Timer) e.get(this);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
