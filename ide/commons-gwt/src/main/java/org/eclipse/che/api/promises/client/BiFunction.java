/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.promises.client;

/**
 * Interface for a binary function: function that takes two arguments.
 *
 * @param <A1>
 *         the argument one type
 * @param <A2>
 *         the argument two type
 * @param <R>
 *         the result type
 */
public interface BiFunction<A1, A2, R> {

    /**
     * Returns the result of applying this function to the given arguments.
     */
    R apply(A1 arg1, A2 arg2);
}
