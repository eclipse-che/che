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
package org.eclipse.che.api.promises.client;

/**
 * Interface for a 'function'.
 *
 * @param <A> the argument type
 * @param <R> the result type
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface Function<A, R> {

  /** Returns the result of applying this function to the given argument. */
  R apply(A arg) throws FunctionException;
}
