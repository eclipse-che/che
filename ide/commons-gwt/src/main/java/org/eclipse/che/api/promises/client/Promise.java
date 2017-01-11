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

import org.eclipse.che.commons.annotation.Nullable;

/**
 * A placeholder for a value that will be completed at a later time.
 *
 * @param <V>
 *         the type of the 'promised' value
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface Promise<V> extends Thenable<V> {

    /**
     * Adds an action when the promise is fulfilled.<br>
     * The action is added both to the original promise and the returned value, but the promises are not
     * necessarily the same object.
     *
     * @param onFulfilled
     *         the fulfillment action added
     * @return a promise equivalent to the original promise with the action added
     */
    <B> Promise<B> then(Function<V, B> onFulfilled);

    /**
     * Allows to add another promise as action when this promise is fulfilled.
     *
     * @param onFulfilled
     *         the fulfillment action added. Function should return promise which
     *         will be evaluated to determine the state of the returned promise
     * @return a promise, state which depends on the promise returned by {@code onFulfilled} function
     * @see #then(Function)
     */
    <B> Promise<B> thenPromise(Function<V, Promise<B>> onFulfilled);

    /**
     * Adds actions when the promise is fulfilled and rejected.<br>
     * The actions are added both to the original promise and the returned value, but the promises are not
     * necessarily the same object.
     *
     * @param onFulfilled
     *         the fulfillment action added
     * @param onRejected
     *         the rejection action added
     * @return a promise equivalent to the original promise with the actions added
     */
    <B> Promise<B> then(@Nullable Function<V, B> onFulfilled, Function<PromiseError, B> onRejected);

    /**
     * Adds actions when the promise is rejected.
     * <p>The action is added both to the original promise and the returned value, but the promises are not
     * necessarily the same object.</p>
     * <p>This is shorthand for {@code promise.then(null, onRejected)}.</p>
     * <p>Note: the method name does not match the ES6 specification,
     * obviously because <code>catch</code> can't be used in Java.
     *
     * @param onRejected
     *         the rejection action added
     * @return a promise equivalent to the original promise with the rejection action added
     */
    <B> Promise<B> catchError(Function<PromiseError, B> onRejected);

    /**
     * Allows to add another promise as action when this promise is rejected.
     *
     * @param onRejected
     *         the rejection action added. Function should return promise which
     *         will be evaluated to determine the state of the returned promise.
     * @return a promise, state which depends on the promise returned by {@code onRejected} function
     * @see #catchError(Function)
     */
    <B> Promise<B> catchErrorPromise(Function<PromiseError, Promise<B>> onRejected);

    /**
     * @see #then(Function)
     */
    Promise<V> then(Operation<V> onFulfilled);

    /**
     * @see #then(Function, Function)
     */
    Promise<V> then(Operation<V> onFulfilled, Function<PromiseError, V> onRejected);

    /**
     * @see #then(Function, Function)
     */
    Promise<V> then(Operation<V> onFulfilled, Operation<PromiseError> onRejected);

    /**
     * @see #then(Function)
     */
    <B> Promise<B> then(Thenable<B> thenable);

    /**
     * @see #catchError(Function)
     */
    Promise<V> catchError(Operation<PromiseError> onRejected);
}
