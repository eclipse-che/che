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
package org.eclipse.che.api.promises.client.callback;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

/**
 * Helps to combine promises with async requests.
 *
 * @author Artem Zatsarynnyi
 * @see org.eclipse.che.ide.rest.AsyncRequest#send()
 * @see org.eclipse.che.ide.rest.AsyncRequest#send(org.eclipse.che.ide.rest.Unmarshallable)
 * @deprecated since org.eclipse.che.ide.rest.AsyncRequest#send() returns Promise
 */
@Deprecated
public class PromiseHelper {

    /** Not instantiable. */
    private PromiseHelper() {
    }

    /**
     * Creates new {@link Promise} from the given {@code requestCall}.
     * When the promise is rejected - an error will be logged to the browser's console.
     *
     * @see org.eclipse.che.ide.rest.AsyncRequest#send()
     * @see org.eclipse.che.ide.rest.AsyncRequest#send(org.eclipse.che.ide.rest.Unmarshallable)
     * @deprecated since org.eclipse.che.ide.rest.AsyncRequest#send() returns Promise
     */
    @Deprecated
    public static <T> Promise<T> newPromise(AsyncPromiseHelper.RequestCall<T> requestCall) {
        final Promise<T> promise = createFromAsyncRequest(requestCall);
        promise.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                Log.error(PromiseHelper.class, error.toString());
            }
        });
        return promise;
    }

    /**
     * Creates new {@link AsyncRequestCallback} that returns {@code Void} value and invokes the given {@code callback}.
     *
     * @see org.eclipse.che.ide.rest.AsyncRequest#send()
     * @see org.eclipse.che.ide.rest.AsyncRequest#send(org.eclipse.che.ide.rest.Unmarshallable)
     * @deprecated since org.eclipse.che.ide.rest.AsyncRequest#send() returns Promise
     */
    @Deprecated
    public static AsyncRequestCallback<Void> newCallback(final AsyncCallback<Void> callback) {
        return new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        };
    }

    /**
     * Creates new {@link AsyncRequestCallback} that invokes the given {@code callback}.
     *
     * @see org.eclipse.che.ide.rest.AsyncRequest#send()
     * @see org.eclipse.che.ide.rest.AsyncRequest#send(org.eclipse.che.ide.rest.Unmarshallable)
     * @deprecated since org.eclipse.che.ide.rest.AsyncRequest#send() returns Promise
     */
    @Deprecated
    public static <T> AsyncRequestCallback<T> newCallback(final AsyncCallback<T> callback, Unmarshallable<T> unmarshallable) {
        return new AsyncRequestCallback<T>(unmarshallable) {
            @Override
            protected void onSuccess(T result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        };
    }
}
