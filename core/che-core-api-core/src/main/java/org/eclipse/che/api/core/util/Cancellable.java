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
package org.eclipse.che.api.core.util;

/**
 * Implementation of this interface may be used with {@link Watchdog} to make possible terminate
 * task by timeout.
 *
 * @author andrew00x
 */
public interface Cancellable {
  interface Callback {
    /**
     * Notified when Cancellable cancelled.
     *
     * @param cancellable Cancellable
     */
    void cancelled(Cancellable cancellable);
  }

  /**
   * Attempts to cancel execution of this {@code Cancellable}.
   *
   * @throws Exception if cancellation is failed
   */
  void cancel() throws Exception;
}
