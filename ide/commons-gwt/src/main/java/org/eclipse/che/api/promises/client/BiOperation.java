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
package org.eclipse.che.api.promises.client;

/**
 * Interface for an 'operation', as a binary function without a return value, only side-effects, but
 * without the burden of having a callback with <code>
 * Void</code> parameter.
 *
 * @param <A1> the type of the first argument
 * @param <A2> the type of the second argument
 */
public interface BiOperation<A1, A2> {

  /** Apply this operation to the given arguments. */
  void apply(A1 arg1, A2 arg2);
}
