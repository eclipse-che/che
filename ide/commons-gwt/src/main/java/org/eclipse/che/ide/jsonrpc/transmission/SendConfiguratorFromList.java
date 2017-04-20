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
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configurator defines the type of a result (if present) and send a request.
 * Result types that are supported: {@link String}, {@link Boolean},
 * {@link Double}, {@link Void} and DTO. This configurator is used when we
 * have defined request params as a list.
 *
 * @param <P> type of params list items
 */
public class SendConfiguratorFromList<P> {
    private final ResponseDispatcher          dispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              factory;

    private final String  method;
    private final List<P> pListValue;
    private final String  endpointId;

    public SendConfiguratorFromList(ResponseDispatcher dispatcher, WebSocketMessageTransmitter transmitter,
                                    JsonRpcFactory factory, String method, List<P> pListValue, String endpointId) {
        this.dispatcher = dispatcher;
        this.transmitter = transmitter;
        this.factory = factory;
        this.method = method;
        this.pListValue = pListValue;
        this.endpointId = endpointId;
    }

    public void sendAndSkipResult() {
        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + pListValue.iterator().next().getClass() + ", " +
                              "params list value" + pListValue);

        transmitNotification();
    }

    public <R> Promise<R> sendAndReceiveResultAsDto(final Class<R> rClass) {
        checkNotNull(rClass, "Result class value must not be null");

        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + pListValue.iterator().next().getClass() + ", " +
                              "params list value" + pListValue + ", " +
                              "result object class: " + rClass);

        return Promises.create(new Executor.ExecutorBody<R>() {
            @Override
            public void apply(ResolveFunction<R> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfOne(endpointId, requestId, rClass, resolve, reject);
            }
        });
    }

    public Promise<String> sendAndReceiveResultAsString() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + pListValue.iterator().next().getClass() + ", " +
                              "params list value" + pListValue + ", " +
                              "result object class: " + String.class);

        return Promises.create(new Executor.ExecutorBody<String>() {
            @Override
            public void apply(ResolveFunction<String> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfOne(endpointId, requestId, String.class, resolve, reject);
            }
        });
    }

    public Promise<Boolean> sendAndReceiveResultAsBoolean() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + pListValue.iterator().next().getClass() + ", " +
                              "params list value" + pListValue + ", " +
                              "result object class: " + Boolean.class);

        return Promises.create(new Executor.ExecutorBody<Boolean>() {
            @Override
            public void apply(ResolveFunction<Boolean> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfOne(endpointId, requestId, Boolean.class, resolve, reject);
            }
        });
    }

    public <R> Promise<List<R>> sendAndReceiveResultAsListOfDto(final Class<R> rClass) {
        checkNotNull(rClass, "Result class value must not be null");

        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + pListValue.iterator().next().getClass() + ", " +
                              "params list value" + pListValue + ", " +
                              "result list items class: " + rClass);

        return Promises.create(new Executor.ExecutorBody<List<R>>() {
            @Override
            public void apply(ResolveFunction<List<R>> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfMany(endpointId, requestId, rClass, resolve, reject);
            }
        });
    }

    public Promise<List<String>> sendAndReceiveResultAsListOfString() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + pListValue.iterator().next().getClass() + ", " +
                              "params list value" + pListValue + ", " +
                              "result list items class: " + String.class);

        return Promises.create(new Executor.ExecutorBody<List<String>>() {
            @Override
            public void apply(ResolveFunction<List<String>> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfMany(endpointId, requestId, String.class, resolve, reject);
            }
        });
    }

    public Promise<List<Boolean>> sendAndReceiveResultAsListOfBoolean() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + pListValue.iterator().next().getClass() + ", " +
                              "params list value" + pListValue + ", " +
                              "result list items class: " + Boolean.class);

        return Promises.create(new Executor.ExecutorBody<List<Boolean>>() {
            @Override
            public void apply(ResolveFunction<List<Boolean>> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfMany(endpointId, requestId, Boolean.class, resolve, reject);
            }
        });
    }

    public Promise<Void> sendAndReceiveResultAsEmpty() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params list items class: " + pListValue.iterator().next().getClass() + ", " +
                              "params list value" + pListValue + ", " +
                              "result object class: " + Void.class);

        return Promises.create(new Executor.ExecutorBody<Void>() {
            @Override
            public void apply(ResolveFunction<Void> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfOne(endpointId, requestId, Void.class, resolve, reject);
            }
        });
    }

    private void transmitNotification() {
        JsonRpcParams params = factory.createParamsList(pListValue);
        JsonRpcRequest request = factory.createRequest(method, params);
        transmitter.transmit(endpointId, request.toString());
    }

    private String transmitRequest() {
        String requestId = Integer.valueOf(MethodNameConfigurator.id.incrementAndGet()).toString();

        JsonRpcParams params = factory.createParamsList(pListValue);
        JsonRpcRequest request = factory.createRequest(requestId, method, params);
        transmitter.transmit(endpointId, request.toString());
        return requestId;
    }
}
