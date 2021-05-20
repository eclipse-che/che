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

import io.opentracing.Tracer;
import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.opentracing.contrib.concurrent.TracedScheduledExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.commons.schedule.executor.CronExecutorService;

/**
 * Implementation of {@code ExecutorServiceWrapper} that add all sort of tracing capabilities with
 * help of traced implementation.
 */
@Singleton
public class TracedExecutorServiceWrapper implements ExecutorServiceWrapper {

  private final Tracer tracer;

  @Inject
  public TracedExecutorServiceWrapper(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public ExecutorService wrap(ExecutorService executor, String name, String... tags) {
    return new TracedExecutorService(executor, tracer);
  }

  @Override
  public ScheduledExecutorService wrap(
      ScheduledExecutorService executor, String name, String... tags) {
    return new TracedScheduledExecutorService(executor, tracer);
  }

  @Override
  public CronExecutorService wrap(CronExecutorService executor, String name, String... tags) {
    return new TracedCronExecutorService(executor, tracer);
  }
}
