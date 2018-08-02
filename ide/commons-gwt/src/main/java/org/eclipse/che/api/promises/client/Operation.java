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
 * Interface for an 'operation', as a function without a return value, only side-effects, but
 * without the burden of having a callback with Void parameter.
 *
 * @param <A> the type of the argument
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface Operation<A> {

  /** Apply this operation to the given argument. */
  void apply(A arg) throws OperationException;
}
