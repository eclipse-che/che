/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import com.google.common.annotations.Beta;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import org.eclipse.che.commons.schedule.executor.CronExecutorService;

/**
 * Wrapper of different implemntations of {@link ExecutorService}. At this moment supported: {@link
 * ExecutorService}, {@link ScheduledExecutorService} and {@link CronExecutorService}.
 *
 * <p>Depending on implementation and environment configuration wrapper can add to the original
 * class different capabilities like monitoring or tracing.
 *
 * @author Sergii Kabashniuk
 */
@Beta
public interface ExecutorServiceWrapper {

  /**
   * Creates wrapper for the given executor.
   *
   * @param executor {@link ExecutorService} that has to be wrapped.
   * @param name unique name that can identify concrete instance of executor.
   * @param tags key/value pairs that gives some context about provided executor.
   * @return wrapped instance of given executor.
   */
  ExecutorService wrap(ExecutorService executor, String name, String... tags);

  /**
   * Creates wrapper for the given executor.
   *
   * @param executor {@link ScheduledExecutorService} that has to be wrapped.
   * @param name unique name that can identify concrete instance of executor.
   * @param tags key/value pairs that gives some context about provided executor.
   * @return wrapped instance of given executor.
   */
  ScheduledExecutorService wrap(ScheduledExecutorService executor, String name, String... tags);

  /**
   * Creates wrapper for the given executor.
   *
   * @param executor {@link CronExecutorService} that has to be wrapped.
   * @param name unique name that can identify concrete instance of executor.
   * @param tags key/value pairs that gives some context about provided executor.
   * @return wrapped instance of given executor.
   */
  CronExecutorService wrap(CronExecutorService executor, String name, String... tags);
}
