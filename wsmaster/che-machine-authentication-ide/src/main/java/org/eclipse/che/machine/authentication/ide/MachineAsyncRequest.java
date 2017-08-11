/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.machine.authentication.ide;

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.RequestBuilder;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.Unmarshallable;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

/**
 * Modify each machine request and add the authorization header with the token value.
 *
 * @author Anton Korneta
 */
public class MachineAsyncRequest extends AsyncRequest {

    private final Promise<String> tokenPromise;

    protected MachineAsyncRequest(RequestBuilder.Method method,
                                  String url,
                                  boolean async,
                                  Promise<String> tokenPromise) {
        super(method, url, async);
        this.tokenPromise = tokenPromise;
    }

    @Override
    public Promise<Void> send() {
        requestBuilder.setIncludeCredentials(true);
        final Executor.ExecutorBody<Void> body = new Executor.ExecutorBody<Void>() {
            @Override
            public void apply(final ResolveFunction<Void> resolve, final RejectFunction reject) {
                tokenPromise.then(new Operation<String>() {
                    @Override
                    public void apply(String machine) throws OperationException {
                        MachineAsyncRequest.this.header(AUTHORIZATION, machine);
                        MachineAsyncRequest.super.send().then(new Operation<Void>() {
                            @Override
                            public void apply(Void arg) throws OperationException {
                                resolve.apply(null);
                            }
                        }).catchError(new Operation<PromiseError>() {
                            @Override
                            public void apply(PromiseError arg) throws OperationException {
                                reject.apply(arg);
                            }
                        });
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError promiseError) throws OperationException {
                        reject.apply(promiseError);
                    }
                });
            }
        };
        final Executor<Void> executor = Executor.create(body);
        return Promises.create(executor);
    }

    @Override
    public <R> Promise<R> send(final Unmarshallable<R> unmarshaller) {
        return CallbackPromiseHelper.createFromCallback(new CallbackPromiseHelper.Call<R, Throwable>() {
            @Override
            public void makeCall(final Callback<R, Throwable> callback) {
                send(new AsyncRequestCallback<R>(unmarshaller) {
                    @Override
                    protected void onSuccess(R result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }

    @Override
    public void send(final AsyncRequestCallback<?> callback) {
        requestBuilder.setIncludeCredentials(true);
        tokenPromise.then(new Operation<String>() {
            @Override
            public void apply(String machineToken) throws OperationException {
                MachineAsyncRequest.this.header(AUTHORIZATION, machineToken);
                MachineAsyncRequest.super.send(callback);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                callback.onError(null, arg.getCause());
            }
        });
    }
}
