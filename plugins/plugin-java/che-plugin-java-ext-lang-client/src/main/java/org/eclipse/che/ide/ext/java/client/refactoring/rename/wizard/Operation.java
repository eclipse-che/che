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

package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard;

import org.eclipse.che.api.promises.client.Promise;

/**
 * Contract for a cancellable operation.
 *
 * @param <T> the type of the argument
 * @author Roman Nikitenko
 */
interface Operation<T> {
  enum Status {
    INITIAL,
    IN_PROGRESS,
    SUCCESS,
    FAIL,
    CANCELLED
  }

  /** Provides ability to handle case when operation was cancelled */
  interface CancelOperationHandler {
    void onCancelled();
  }

  /** Returns current state of the operation. See {@link Status} */
  Status getStatus();

  /**
   * Performs the operation. Use {{@link #perform(CancelOperationHandler)}} to have ability to
   * process case when operation is cancelled
   */
  default Promise<T> perform() {
    return perform(() -> {});
  }

  /**
   * Performs the operation and returns result of execution.
   *
   * @param handler use this handler to process case when operation is cancelled
   */
  Promise<T> perform(CancelOperationHandler handler);

  /**
   * Cancel the operation if it's possible.
   *
   * @return resolves the promise if operation is cancelled, reject the promise otherwise
   */
  Promise<Void> cancel();
}
