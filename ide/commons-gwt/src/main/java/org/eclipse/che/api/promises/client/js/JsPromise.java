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
package org.eclipse.che.api.promises.client.js;

import com.google.gwt.core.client.JavaScriptObject;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.Thenable;

/**
 * Implementation of {@link Promise} around ES6 promises.
 *
 * @param <V>
 *         the type of the promised value
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class JsPromise<V> extends JavaScriptObject implements Promise<V> {

    /**
     * JSO mandated protected constructor.
     */
    protected JsPromise() {
    }

    private static void applyOperationToError(Operation<PromiseError> onRejected, Object reason) throws OperationException {
        final Throwable cause = getCause(reason);

        if (cause != null) {
            onRejected.apply(JsPromiseError.create(cause));
        } else {
            onRejected.apply(JsPromiseError.create(reason instanceof Throwable ? (Throwable)reason : null));
        }
    }

    private static <V> V applyFunctionToError(Function<PromiseError, V> onRejected, Object reason) throws FunctionException {
        final Throwable cause = getCause(reason);

        if (cause != null) {
            return onRejected.apply(JsPromiseError.create(cause));
        } else {
            return onRejected.apply(JsPromiseError.create(reason instanceof Throwable ? (Throwable)reason : null));
        }
    }

    private final static <B> Thenable<B> staticThen(Thenable<B> thenable, B arg) {
        return thenable.then(arg);
    }

    @Override
    public final native <B> Thenable<B> then(V arg) /*-{
        return this.then(arg);
    }-*/;

    @Override
    public final native <B> Promise<B> then(Function<V, B> onFulfilled) /*-{
        return this.then(function (value) {
            return onFulfilled.@org.eclipse.che.api.promises.client.Function::apply(*)(value);
        });
    }-*/;

    @Override
    public final native <B> Promise<B> thenPromise(Function<V, Promise<B>> onFulfilled) /*-{
        return this.then(function (value) {
            return onFulfilled.@org.eclipse.che.api.promises.client.Function::apply(*)(value);
        });
    }-*/;

    @Override
    public final <B> Promise<B> then(Function<V, B> onFulfilled, Function<PromiseError, B> onRejected) {
        if (onFulfilled != null) {
            return this.internalThen(onFulfilled, onRejected);
        } else {
            return this.catchError(onRejected);
        }
    }

    private final native <B> Promise<B> internalThen(Function<V, B> onFulfilled, Function<PromiseError, B> onRejected) /*-{
        return this.then(function (value) {
            return onFulfilled.@org.eclipse.che.api.promises.client.Function::apply(*)(value);
        }, function (reason) {
            return @org.eclipse.che.api.promises.client.js.JsPromise::applyFunctionToError(*)(onRejected, reason);
        });
    }-*/;

    @Override
    public final native <B> Promise<B> catchError(Function<PromiseError, B> onRejected) /*-{
        return this.then(undefined, function (reason) {
            return @org.eclipse.che.api.promises.client.js.JsPromise::applyFunctionToError(*)(onRejected, reason);
        });
    }-*/;

    @Override
    public final native <B> Promise<B> catchErrorPromise(Function<PromiseError, Promise<B>> onRejected) /*-{
        return this.then(undefined, function (reason) {
            return @org.eclipse.che.api.promises.client.js.JsPromise::applyFunctionToError(*)(onRejected, reason);
        });
    }-*/;

    @Override
    public final native Promise<V> then(Operation<V> onFulfilled) /*-{
        return this.then(function (value) {
            onFulfilled.@org.eclipse.che.api.promises.client.Operation::apply(*)(value);
        });
    }-*/;

    @Override
    public final Promise<V> then(Operation<V> onFulfilled, Function<PromiseError, V> onRejected) {
        if (onFulfilled != null) {
            return this.internalThen(onFulfilled, onRejected);
        } else {
            return this.catchError(onRejected);
        }
    }

    private final native <B> Promise<B> internalThen(Operation<V> onFulfilled, Function<PromiseError, V> onRejected) /*-{
        return this.then(function (value) {
            onFulfilled.@org.eclipse.che.api.promises.client.Operation::apply(*)(value);
        }, function (reason) {
            return @org.eclipse.che.api.promises.client.js.JsPromise::applyFunctionToError(*)(onRejected, reason);
        });
    }-*/;

    @Override
    public final Promise<V> then(Operation<V> onFulfilled, Operation<PromiseError> onRejected) {
        if (onFulfilled != null) {
            return this.internalThen(onFulfilled, onRejected);
        } else {
            return this.catchError(onRejected);
        }
    }

    private final native Promise<V> internalThen(Operation<V> onFulfilled, Operation<PromiseError> onRejected) /*-{
        return this.then(function (value) {
            onFulfilled.@org.eclipse.che.api.promises.client.Operation::apply(*)(value);
        }, function (reason) {
            @org.eclipse.che.api.promises.client.js.JsPromise::applyOperationToError(*)(onRejected, reason);
        });
    }-*/;

    @Override
    public final <B> Promise<B> then(final Thenable<B> thenable) {
        if (thenable instanceof JavaScriptObject) {
            return this.thenJs((JavaScriptObject)thenable);
        } else {
            return this.thenJava(thenable);
        }
    }

    @Override
    public final native Promise<V> catchError(Operation<PromiseError> onRejected) /*-{
        return this.then(undefined, function (reason) {
            @org.eclipse.che.api.promises.client.js.JsPromise::applyOperationToError(*)(onRejected, reason);
        });
    }-*/;

    private final native <B> Promise<B> thenJs(JavaScriptObject thenable) /*-{
        return this.then(thenable);
    }-*/;

    private final native <B> Promise<B> thenJava(Thenable<B> thenable) /*-{
        return this.then(new Object()
        {
            then: function (arg) {
                return @org.eclipse.che.api.promises.client.js.JsPromise::staticThen(*)(thenable, arg);
            }
        }
    )
        ;
    }-*/;

    public static Throwable getCause(Object object) {
        return object instanceof JsPromiseError ? ((JsPromiseError)object).getCause() : null;
    }
}
