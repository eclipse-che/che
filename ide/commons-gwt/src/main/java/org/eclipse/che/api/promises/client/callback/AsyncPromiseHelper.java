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
package org.eclipse.che.api.promises.client.callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;

/**
 * Helps to create a {@link Promise} from a {@link AsyncCallback}.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public final class AsyncPromiseHelper {

  private AsyncPromiseHelper() {}

  public static <V> Promise<V> createFromAsyncRequest(final RequestCall<V> call) {
    final Executor.ExecutorBody<V> body =
        new Executor.ExecutorBody<V>() {
          @Override
          public void apply(final ResolveFunction<V> resolve, final RejectFunction reject) {
            call.makeCall(
                new AsyncCallback<V>() {
                  @Override
                  public void onSuccess(final V result) {
                    resolve.apply(result);
                  }

                  @Override
                  public void onFailure(final Throwable exception) {
                    reject.apply(JsPromiseError.create(exception));
                  }
                });
          }
        };
    final Executor<V> executor = Executor.create(body);
    return Promises.create(executor);
  }

  public interface RequestCall<V> {
    void makeCall(AsyncCallback<V> callback);
  }
}
