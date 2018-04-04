/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.schedule.executor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduled thread-pool executor implementation that leverages a CronExpression to calculate future
 * execution times for scheduled tasks.
 */
public class CronThreadPoolExecutor extends ScheduledThreadPoolExecutor
    implements CronExecutorService {

  private static final Logger LOG = LoggerFactory.getLogger(CronThreadPoolExecutor.class);

  private final List<CountDownLatch> cronJobWatchDogs;

  /**
   * Constructs a new CronThreadPoolExecutor.
   *
   * @param corePoolSize the pool size
   */
  public CronThreadPoolExecutor(int corePoolSize) {
    super(corePoolSize);
    this.cronJobWatchDogs = new ArrayList<>();
    this.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
  }

  /**
   * Constructs a new CronThreadPoolExecutor.
   *
   * @param corePoolSize the pool size
   * @param threadFactory the thread factory
   */
  public CronThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
    super(corePoolSize, threadFactory);
    this.cronJobWatchDogs = new ArrayList<>();
    this.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
  }

  /**
   * Constructs a new CronThreadPoolExecutor.
   *
   * @param corePoolSize the pool size
   * @param handler the handler for rejected executions
   */
  public CronThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
    super(corePoolSize, handler);
    this.cronJobWatchDogs = new ArrayList<>();
    this.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
  }

  /**
   * Constructs a new CronThreadPoolExecutor.
   *
   * @param corePoolSize the pool size
   * @param handler the handler for rejecting executions
   * @param threadFactory the thread factory
   */
  public CronThreadPoolExecutor(
      int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(corePoolSize, threadFactory, handler);
    this.cronJobWatchDogs = new ArrayList<>();
  }

  @Override
  public Future<?> schedule(final Runnable task, final CronExpression expression) {
    if (task == null) {
      throw new NullPointerException();
    }
    setCorePoolSize(getCorePoolSize() + 1);
    Runnable scheduleTask =
        new Runnable() {
          @Override
          public void run() {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            cronJobWatchDogs.add(countDownLatch);
            Date now = new Date();
            Date time = expression.getNextValidTimeAfter(now);
            try {
              while (time != null) {
                CronThreadPoolExecutor.this.schedule(
                    task, time.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
                while (now.before(time)) {
                  LOG.debug("Cron watch dog wait {} ", time.getTime() - now.getTime());
                  if (countDownLatch.await(time.getTime() - now.getTime(), TimeUnit.MILLISECONDS)) {
                    LOG.debug("Stopping cron watch dog.");
                    return;
                  }
                  now = new Date();
                }
                time = expression.getNextValidTimeAfter(now);
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } catch (RejectedExecutionException | CancellationException e) {
              LOG.error(e.getMessage(), e);
            }
          }
        };
    return this.submit(scheduleTask);
  }

  @Override
  public void shutdown() {

    for (CountDownLatch cronJobWatchDog : cronJobWatchDogs) {
      cronJobWatchDog.countDown();
    }
    cronJobWatchDogs.clear();
    super.shutdown();
    LOG.debug(
        "Active {} Pool {}, CEPTAS {} ,  EEDTAS {} , Task count {} , queue size {}",
        getActiveCount(),
        getPoolSize(),
        getContinueExistingPeriodicTasksAfterShutdownPolicy(),
        getExecuteExistingDelayedTasksAfterShutdownPolicy(),
        getTaskCount(),
        getQueue().size());
  }

  @Override
  public List<Runnable> shutdownNow() {
    for (CountDownLatch cronJobWatchDog : cronJobWatchDogs) {
      cronJobWatchDog.countDown();
    }
    cronJobWatchDogs.clear();
    LOG.debug(
        "Active {} Pool {}, CEPTAS {} ,  EEDTAS {} , Task count {} , queue size {}",
        getActiveCount(),
        getPoolSize(),
        getContinueExistingPeriodicTasksAfterShutdownPolicy(),
        getExecuteExistingDelayedTasksAfterShutdownPolicy(),
        getTaskCount(),
        getQueue().size());
    return super.shutdownNow();
  }
}
