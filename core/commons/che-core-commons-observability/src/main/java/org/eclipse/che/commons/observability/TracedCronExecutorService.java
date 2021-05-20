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
import io.opentracing.contrib.concurrent.TracedRunnable;
import io.opentracing.contrib.concurrent.TracedScheduledExecutorService;
import java.util.concurrent.Future;
import javax.inject.Inject;
import org.eclipse.che.commons.schedule.executor.CronExecutorService;
import org.eclipse.che.commons.schedule.executor.CronExpression;

/**
 * Executor which propagates span from parent thread to submitted. Optionally it creates parent span
 * if traceWithActiveSpanOnly = false.
 */
public class TracedCronExecutorService extends TracedScheduledExecutorService
    implements CronExecutorService {

  private final CronExecutorService delegate;

  @Inject
  public TracedCronExecutorService(CronExecutorService delegate, Tracer tracer) {
    super(delegate, tracer);
    this.delegate = delegate;
  }

  @Override
  public Future<?> schedule(Runnable task, CronExpression expression) {
    return delegate.schedule(
        tracer.activeSpan() == null ? task : new TracedRunnable(task, tracer), expression);
  }
}
