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
 * Interface for a binary function: function that takes two arguments.
 *
 * @param <A1> the argument one type
 * @param <A2> the argument two type
 * @param <R> the result type
 */
public interface BiFunction<A1, A2, R> {

  /** Returns the result of applying this function to the given arguments. */
  R apply(A1 arg1, A2 arg2);
}
