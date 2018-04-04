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
package org.eclipse.che.commons.schedule;

import java.util.concurrent.TimeUnit;

/**
 * Launcher of periodic jobs.
 *
 * @author Sergii Kabashniuk
 */
public interface Launcher {
  /**
   * execution periodic action according to the cron expression. See more {@link
   * org.eclipse.che.commons.schedule.executor.CronExpression}
   */
  void scheduleCron(Runnable runnable, String cron);

  /**
   * Execute periodic action that becomes enabled first after the given initial delay, and
   * subsequently with the given delay between the termination of one execution and the commencement
   * of the next.
   *
   * <p>Analogue of {@link
   * java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long,
   * java.util.concurrent.TimeUnit)} }
   */
  void scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit unit);

  /**
   * Execute a periodic action that becomes enabled first after the given initial delay, and
   * subsequently with the given period; that is executions will commence after initialDelay then
   * initialDelay+period, then initialDelay + 2 * period, and so on. If any execution of the task
   * encounters an exception, subsequent executions are suppressed. Otherwise, the task will only
   * terminate via cancellation or termination of the executor. If any execution of this task takes
   * longer than its period, then subsequent executions may start late, but will not concurrently
   * execute.
   *
   * <p>Analogue of {@link
   * java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long,
   * java.util.concurrent.TimeUnit)} }
   */
  void scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit unit);
}
