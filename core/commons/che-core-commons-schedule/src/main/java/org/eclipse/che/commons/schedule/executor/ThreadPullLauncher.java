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
package org.eclipse.che.commons.schedule.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.schedule.Launcher;
import org.eclipse.che.inject.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute method marked with @ScheduleCron @ScheduleDelay and @ScheduleRate annotations using
 * CronThreadPoolExecutor.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class ThreadPullLauncher implements Launcher {
  private static final Logger LOG = LoggerFactory.getLogger(CronThreadPoolExecutor.class);
  private final CronExecutorService service;

  /**
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     {@code allowCoreThreadTimeOut} is set
   */
  @Inject
  public ThreadPullLauncher(@Named("schedule.core_pool_size") Integer corePoolSize) {
    this(
        new CronThreadPoolExecutor(
            corePoolSize,
            new ThreadFactoryBuilder()
                .setNameFormat("Annotated-scheduler-%d")
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .setDaemon(false)
                .build()));
  }

  protected ThreadPullLauncher(CronExecutorService service) {
    this.service = service;
  }

  @PreDestroy
  public void shutdown() throws InterruptedException {
    // Tell threads to finish off.
    service.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
        service.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!service.awaitTermination(60, TimeUnit.SECONDS)) LOG.warn("Pool did not terminate");
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      service.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void scheduleCron(Runnable runnable, String cron) {
    if (cron == null || cron.isEmpty()) {
      throw new ConfigurationException("Cron parameter can't be null");
    }
    try {
      CronExpression expression = new CronExpression(cron);
      service.schedule(runnable, expression);
      LOG.debug("Schedule method {} with cron  {} schedule", runnable, cron);
    } catch (ParseException e) {
      LOG.error(e.getLocalizedMessage(), e);
      throw new ConfigurationException(e.getLocalizedMessage());
    }
  }

  @Override
  public void scheduleWithFixedDelay(
      Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
    if (delay <= 0) {
      LOG.debug(
          "Method {} has not been scheduled (delay <= 0). Initial delay {} delay {} unit {}",
          runnable,
          initialDelay,
          delay,
          unit);
      return;
    }

    service.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
    LOG.debug(
        "Schedule method {} with fixed initial delay {} delay {} unit {}",
        runnable,
        initialDelay,
        delay,
        unit);
  }

  @Override
  public void scheduleAtFixedRate(
      Runnable runnable, long initialDelay, long period, TimeUnit unit) {
    if (period <= 0) {
      LOG.debug(
          "Method {} with fixed rate has not been scheduled (period <= 0). Initial delay {} period {} unit {}",
          runnable,
          initialDelay,
          period,
          unit);
      return;
    }

    service.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    LOG.debug(
        "Schedule method {} with fixed rate. Initial delay {} period {} unit {}",
        runnable,
        initialDelay,
        period,
        unit);
  }
}
