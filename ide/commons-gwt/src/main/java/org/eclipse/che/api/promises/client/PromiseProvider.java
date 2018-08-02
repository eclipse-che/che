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

import com.google.common.annotations.Beta;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;
import elemental.util.ArrayOf;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.JsPromiseProvider;

/**
 * A smattering of useful methods to work with Promises.
 *
 * @author Vlad Zhukovskyi
 * @since 4.3.0
 */
@Beta
@ImplementedBy(JsPromiseProvider.class)
public interface PromiseProvider {

  /**
   * Creates a new promise using the provided executor.
   *
   * @param executor the executor
   * @param <V> the type of the promised value
   * @return a promise
   */
  <V> Promise<V> create(Executor<V> executor);

  /**
   * Creates a new promise using the {@link AsyncCallback}.
   *
   * @param call the request caller
   * @param <V> the type of the promised value
   * @return a promise
   */
  <V> Promise<V> create(AsyncPromiseHelper.RequestCall<V> call);

  /**
   * Creates a promise that resolves as soon as all the promises used as parameters are resolved or
   * rejected as soon as the first rejection happens on one of the included promises. This is useful
   * for aggregating results of multiple promises together.
   *
   * @param promises the included promises
   * @return a promise with an array of unit values as fulfillment value
   * @deprecated use {@link #all2(ArrayOf)}
   */
  @Deprecated
  Promise<JsArrayMixed> all(ArrayOf<Promise<?>> promises);

  /**
   * @see {@link #all(ArrayOf)}
   * @deprecated use {@link #all2(Promise[])}
   */
  @Deprecated
  Promise<JsArrayMixed> all(final Promise<?>... promises);

  /**
   * Creates a promise that resolves as soon as all the promises used as parameters are resolved or
   * rejected as soon as the first rejection happens on one of the included promises. This is useful
   * for aggregating results of multiple promises together.
   *
   * @param promises the included promises
   * @return a promise with an array of unit values as fulfillment value
   */
  Promise<ArrayOf<?>> all2(ArrayOf<Promise<?>> promises);

  /** @see {@link #all(ArrayOf)} */
  Promise<ArrayOf<?>> all2(final Promise<?>... promises);

  /**
   * Returns a promise that is rejected with the given reason.
   *
   * @param reason the reason of promise rejection
   * @param <U> the type of the returned promise
   * @return a promise
   */
  <U> Promise<U> reject(PromiseError reason);

  /**
   * Returns a promise that is rejected with the given reason.
   *
   * @param message the reason of promise rejection
   * @param <U> the type of the returned promise
   * @return a promise
   */
  <U> Promise<U> reject(String message);

  /**
   * Returns a promise that is rejected with the given reason.
   *
   * @param reason the reason of promise rejection
   * @param <U> the type of the returned promise
   * @return a promise
   */
  <U> Promise<U> reject(Throwable reason);

  /**
   * Returns a promise that is resolved with the given {@code value}.
   *
   * @param value the 'promised' value
   * @param <U> the type of the returned promise
   * @return a promise that is resolved with the specified value
   */
  <U> Promise<U> resolve(U value);
}
