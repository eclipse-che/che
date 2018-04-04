/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
