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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.commons.schedule.executor.CronExecutorService;

/**
 * Implementation of {@code ExecutorServiceWrapper} that add all sort of monitoring and tracing
 * capabilities. Monitoring allayed first, tracing second.
 */
@Singleton
public class MeteredAndTracedExecutorServiceWrapper implements ExecutorServiceWrapper {

  private final MeteredExecutorServiceWrapper meteredExecutorServiceWrapper;
  private final TracedExecutorServiceWrapper tracedExecutorServiceWrapper;

  @Inject
  public MeteredAndTracedExecutorServiceWrapper(
      MeteredExecutorServiceWrapper meteredExecutorServiceWrapper,
      TracedExecutorServiceWrapper tracedExecutorServiceWrapper) {

    this.meteredExecutorServiceWrapper = meteredExecutorServiceWrapper;
    this.tracedExecutorServiceWrapper = tracedExecutorServiceWrapper;
  }

  @Override
  public ExecutorService wrap(ExecutorService executor, String name, String... tags) {
    return tracedExecutorServiceWrapper.wrap(
        meteredExecutorServiceWrapper.wrap(executor, name, tags), name, tags);
  }

  @Override
  public ScheduledExecutorService wrap(
      ScheduledExecutorService executor, String name, String... tags) {
    return tracedExecutorServiceWrapper.wrap(
        meteredExecutorServiceWrapper.wrap(executor, name, tags), name, tags);
  }

  @Override
  public CronExecutorService wrap(CronExecutorService executor, String name, String... tags) {
    return tracedExecutorServiceWrapper.wrap(
        meteredExecutorServiceWrapper.wrap(executor, name, tags), name, tags);
  }
}
