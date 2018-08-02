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
package org.eclipse.che.api.languageserver.util;

import java.util.concurrent.CompletableFuture;

/**
 * An operation to be executed against collections of language servers. See {@link OperationUtil}
 *
 * @author Thomas Mäder
 * @param <C> The type this operation acts upon
 * @param <R> The type this operation produces
 */
public interface LSOperation<C, R> {

  /**
   * Returns whether the operation can be performed on the given element
   *
   * @param element
   * @return true if the operations should be performed
   */
  boolean canDo(C element);

  /**
   * Start the operation on the given element
   *
   * @param element
   * @return a future that produces the result of running the operation on the given element
   */
  CompletableFuture<R> start(C element);

  /**
   * Handle the result of of processing an element.
   *
   * @param result
   * @return true if the result is valid (non-empty, not null)
   */
  boolean handleResult(C element, R result);
}
