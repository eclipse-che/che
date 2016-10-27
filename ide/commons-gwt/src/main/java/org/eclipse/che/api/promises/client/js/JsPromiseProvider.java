/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.promises.client.js;

import elemental.js.util.JsArrayOf;
import elemental.util.ArrayOf;
import elemental.util.Collections;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;

/**
 * Default implementation of {@link PromiseProvider}.
 *
 * @author Vlad Zhukovskyi
 * @see PromiseProvider
 */
public class JsPromiseProvider implements PromiseProvider {

    /** {@inheritDoc} */
    @Override
    public native <V> Promise<V> create(Executor<V> executor) /*-{
        return new Promise(executor);
    }-*/;

    /** {@inheritDoc} */
    @Override
    public native Promise<JsArrayMixed> all(ArrayOf<Promise<?>> promises) /*-{
        return Promise.all(promises);
    }-*/;

    /** {@inheritDoc} */
    @Override
    public Promise<JsArrayMixed> all(Promise<?>... promises) {
        final JsArrayOf<Promise<?>> promisesArray = JavaScriptObject.createArray().cast();
        for (final Promise<?> promise : promises) {
            promisesArray.push(promise);
        }
        return all(promisesArray);
    }

    @Override
    public Promise<ArrayOf<?>> all2(ArrayOf<Promise<?>> promises) {
        return internalAll(promises);
    }

    @Override
    public Promise<ArrayOf<?>> all2(Promise<?>... promises) {
        ArrayOf<Promise<?>> arrayOf = Collections.arrayOf();
        for (Promise<?> promise : promises) {
            arrayOf.push(promise);
        }
        return internalAll(arrayOf);
    }

    private native Promise<ArrayOf<?>> internalAll(ArrayOf<Promise<?>> promises) /*-{
        return Promise.all(promises);
    }-*/;

    /** {@inheritDoc} */
    @Override
    public native <U> Promise<U> reject(String message) /*-{
        return Promise.reject(new Error(message));
    }-*/;

    /** {@inheritDoc} */
    @Override
    public native <U> Promise<U> resolve(U value) /*-{
        return Promise.resolve(value);
    }-*/;

    /** {@inheritDoc} */
    @Override
    public <U> Promise<U> reject(Throwable reason) {
        return reject(JsPromiseError.create(reason));
    }

    /** {@inheritDoc} */
    public final native <U> JsPromise<U> reject(PromiseError reason) /*-{
        return Promise.reject(reason);
    }-*/;
}
