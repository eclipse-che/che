/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.util;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It controls the time of {@code Cancellable} invocation and if time if greater than timeout it
 * terminates such {@code Cancellable}.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public final class Watchdog implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(Watchdog.class);

  private final String name;
  private final long timeout;

  private boolean watch;
  private Cancellable cancellable;

  /**
   * Create new {@code Watchdog}.
   *
   * @param name name for background {@code Thread}. It helps to identify out threads. This
   *     parameter is optional and may be {@code null}.
   * @param timeout timeout
   * @param unit timeout unit
   */
  public Watchdog(String name, long timeout, TimeUnit unit) {
    this.name = name;
    if (timeout < 1) {
      throw new IllegalArgumentException(String.format("Invalid timeout: %d", timeout));
    }
    this.timeout = unit.toMillis(timeout);
  }

  public Watchdog(long timeout, TimeUnit unit) {
    this(null, timeout, unit);
  }

  /**
   * Start watching {@code Cancellable}.
   *
   * @param cancellable Cancellable
   */
  public synchronized void start(Cancellable cancellable) {
    this.cancellable = cancellable;
    this.watch = true;
    final Thread t = name == null ? new Thread(this) : new Thread(this, name);
    t.setDaemon(true);
    t.start();
  }

  /** Stop watching. */
  public synchronized void stop() {
    watch = false;
    notify();
  }

  /** NOTE: Not expected to call directly by regular users of this class. */
  public synchronized void run() {
    final long end = System.currentTimeMillis() + timeout;
    long now;
    while (watch && (end > (now = System.currentTimeMillis()))) {
      try {
        wait(end - now);
      } catch (InterruptedException ignored) {
        // Not expected to be thrown
      }
    }
    if (watch) {
      try {
        cancellable.cancel();
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
      watch = false;
    }
  }
}
