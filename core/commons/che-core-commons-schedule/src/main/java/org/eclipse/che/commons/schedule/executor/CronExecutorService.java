/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.schedule.executor;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/** Executor service that schedules a task for execution via a cron expression. */
public interface CronExecutorService extends ScheduledExecutorService {
  /**
   * Schedules the specified task to execute according to the specified cron expression.
   *
   * @param task the Runnable task to schedule
   * @param expression a cron expression
   */
  Future<?> schedule(Runnable task, CronExpression expression);
}
