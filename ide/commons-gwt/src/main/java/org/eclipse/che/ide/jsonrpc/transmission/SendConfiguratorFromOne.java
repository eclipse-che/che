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
package org.eclipse.che.ide.jsonrpc.transmission;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.jsonrpc.JsonRpcFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcParams;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequest;
import org.eclipse.che.ide.jsonrpc.ResponseDispatcher;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import java.util.List;

public class SendConfiguratorFromOne<P> {
    private final ResponseDispatcher          responseDispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              jsonRpcFactory;
    private final String                      method;
    private final P                           paramsValue;
    private final String                      endpointId;

    public SendConfiguratorFromOne(ResponseDispatcher responseDispatcher, WebSocketMessageTransmitter transmitter,
                                   JsonRpcFactory jsonRpcFactory, String method, P paramsValue, String endpointId) {
        this.responseDispatcher = responseDispatcher;
        this.transmitter = transmitter;
        this.jsonRpcFactory = jsonRpcFactory;
        this.method = method;
        this.paramsValue = paramsValue;
        this.endpointId = endpointId;
    }

    public void sendAndSkipResult() {
        transmitNotification();
    }

    public <R> Promise<R> sendAndReceiveResultAsDto(final Class<R> rClass) {
        final String requestId = transmitRequest();

        return Promises.create(new Executor.ExecutorBody<R>() {
            @Override
            public void apply(ResolveFunction<R> resolve, RejectFunction reject) {
                responseDispatcher.registerPromiseOfOne(endpointId, requestId, rClass, resolve, reject);
            }
        });
    }

    public Promise<String> sendAndReceiveResultAsString() {
        final String requestId = transmitRequest();

        return Promises.create(new Executor.ExecutorBody<String>() {
            @Override
            public void apply(ResolveFunction<String> resolve, RejectFunction reject) {
                responseDispatcher.registerPromiseOfOne(endpointId, requestId, String.class, resolve, reject);
            }
        });
    }

    public Promise<Boolean> sendAndReceiveResultAsBoolean() {
        final String requestId = transmitRequest();

        return Promises.create(new Executor.ExecutorBody<Boolean>() {
            @Override
            public void apply(ResolveFunction<Boolean> resolve, RejectFunction reject) {
                responseDispatcher.registerPromiseOfOne(endpointId, requestId, Boolean.class, resolve, reject);
            }
        });
    }

    public <R> Promise<List<R>> sendAndReceiveResultAsListOfDto(final Class<R> rClass) {
        final String requestId = transmitRequest();

        return Promises.create(new Executor.ExecutorBody<List<R>>() {
            @Override
            public void apply(ResolveFunction<List<R>> resolve, RejectFunction reject) {
                responseDispatcher.registerPromiseOfMany(endpointId, requestId, rClass, resolve, reject);
            }
        });
    }

    public Promise<List<String>> sendAndReceiveResultAsListOfString() {
        final String requestId = transmitRequest();

        return Promises.create(new Executor.ExecutorBody<List<String>>() {
            @Override
            public void apply(ResolveFunction<List<String>> resolve, RejectFunction reject) {
                responseDispatcher.registerPromiseOfMany(endpointId, requestId, String.class, resolve, reject);
            }
        });
    }

    public Promise<List<Boolean>> sendAndReceiveResultAsListOfBoolean() {
        final String requestId = transmitRequest();

        return Promises.create(new Executor.ExecutorBody<List<Boolean>>() {
            @Override
            public void apply(ResolveFunction<List<Boolean>> resolve, RejectFunction reject) {
                responseDispatcher.registerPromiseOfMany(endpointId, requestId, Boolean.class, resolve, reject);
            }
        });
    }

    public Promise<Void> sendAndReceiveResultAsEmpty() {
        final String requestId = transmitRequest();

        return Promises.create(new Executor.ExecutorBody<Void>() {
            @Override
            public void apply(ResolveFunction<Void> resolve, RejectFunction reject) {
                responseDispatcher.registerPromiseOfOne(endpointId, requestId, Void.class, resolve, reject);
            }
        });
    }

    private String transmitRequest() {
        String requestId = Integer.valueOf(MethodNameConfigurator.id.incrementAndGet()).toString();

        JsonRpcParams params = jsonRpcFactory.createParams(paramsValue);
        JsonRpcRequest request = jsonRpcFactory.createRequest(requestId, method, params);
        transmitter.transmit(endpointId, request.toString());
        return requestId;
    }

    private void transmitNotification() {
        JsonRpcParams params = jsonRpcFactory.createParams(paramsValue);
        JsonRpcRequest request = jsonRpcFactory.createRequest(method, params);
        transmitter.transmit(endpointId, request.toString());
    }
}
