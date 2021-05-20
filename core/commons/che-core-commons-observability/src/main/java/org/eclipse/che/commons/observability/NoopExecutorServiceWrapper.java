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
package org.eclipse.che.commons.observability;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Singleton;
import org.eclipse.che.commons.schedule.executor.CronExecutorService;

/**
 * Implementation of {@link ExecutorServiceWrapper} that adds nothing. All wrap methods return the
 * same instance as result.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class NoopExecutorServiceWrapper implements ExecutorServiceWrapper {

  @Override
  public ExecutorService wrap(ExecutorService executor, String name, String... tags) {
    return executor;
  }

  @Override
  public ScheduledExecutorService wrap(
      ScheduledExecutorService executor, String name, String... tags) {
    return executor;
  }

  @Override
  public CronExecutorService wrap(CronExecutorService executor, String name, String... tags) {
    return executor;
  }
}
