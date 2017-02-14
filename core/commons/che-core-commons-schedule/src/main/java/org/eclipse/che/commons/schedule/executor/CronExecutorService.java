/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.schedule.executor;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Executor service that schedules a task for execution via a cron expression.
 */
public interface CronExecutorService extends ScheduledExecutorService {
    /**
     * Schedules the specified task to execute according to the specified cron expression.
     *
     * @param task       the Runnable task to schedule
     * @param expression a cron expression
     */
    Future<?> schedule(Runnable task, CronExpression expression);
}
