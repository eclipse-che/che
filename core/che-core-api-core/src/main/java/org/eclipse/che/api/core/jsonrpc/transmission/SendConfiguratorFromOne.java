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
package org.eclipse.che.api.core.jsonrpc.transmission;

import org.eclipse.che.api.core.jsonrpc.JsonRpcFactory;
import org.eclipse.che.api.core.jsonrpc.JsonRpcParams;
import org.eclipse.che.api.core.jsonrpc.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.JsonRpcRequest;
import org.eclipse.che.api.core.jsonrpc.ResponseDispatcher;
import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configurator defines the type of a result (if present) and send a request.
 * Result types that are supported: {@link String}, {@link Boolean},
 * {@link Double}, {@link Void} and DTO. This configurator is used when we
 * have defined request params as a single object.
 *
 * @param <P>
 *         type of params objects
 */
public class SendConfiguratorFromOne<P> {
    private static final Logger LOG = LoggerFactory.getLogger(SendConfiguratorFromOne.class);

    private final ResponseDispatcher          dispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              factory;
    private final String                      method;
    private final P                           pValue;
    private final String                      endpointId;

    SendConfiguratorFromOne(ResponseDispatcher dispatcher, WebSocketMessageTransmitter transmitter,
                            JsonRpcFactory factory, String method, P pValue, String endpointId) {
        this.dispatcher = dispatcher;
        this.transmitter = transmitter;
        this.factory = factory;
        this.method = method;
        this.pValue = pValue;
        this.endpointId = endpointId;
    }

    public void sendAndSkipResult() {
        LOG.debug("Transmitting request: " +
                  "endpoint ID: " + endpointId + ", " +
                  "method: " + method + ", " +
                  (pValue != null ? "params object class: " + pValue.getClass() + ", " : "") +
                  "params list value" + pValue);

        transmitNotification();
    }

    public <R> JsonRpcPromise<R> sendAndReceiveResultAsDto(final Class<R> rClass) {
        checkNotNull(rClass, "Result class value must not be null");

        final String requestId = transmitRequest();

        LOG.debug("Transmitting request: " +
                  "endpoint ID: " + endpointId + ", " +
                  "request ID: " + requestId + ", " +
                  "method: " + method + ", " +
                  (pValue != null ? "params object class: " + pValue.getClass() + ", " : "") +
                  "params list value" + pValue + ", " +
                  "result object class: " + rClass);

        return dispatcher.registerPromiseOfOne(endpointId, requestId, rClass, new JsonRpcPromise<>());
    }


    public JsonRpcPromise<String> sendAndReceiveResultAsString() {
        final String requestId = transmitRequest();

        LOG.debug("Transmitting request: " +
                  "endpoint ID: " + endpointId + ", " +
                  "request ID: " + requestId + ", " +
                  "method: " + method + ", " +
                  (pValue != null ? "params object class: " + pValue.getClass() + ", " : "") +
                  "params list value" + pValue + ", " +
                  "result object class: " + String.class);

        return dispatcher.registerPromiseOfOne(endpointId, requestId, String.class, new JsonRpcPromise<>());

    }

    public JsonRpcPromise<Double> sendAndReceiveResultAsDouble() {
        final String requestId = transmitRequest();

        LOG.debug("Transmitting request: " +
                  "endpoint ID: " + endpointId + ", " +
                  "request ID: " + requestId + ", " +
                  "method: " + method + ", " +
                  (pValue != null ? "params object class: " + pValue.getClass() + ", " : "") +
                  "params list value" + pValue + ", " +
                  "result object class: " + Double.class);

        return dispatcher.registerPromiseOfOne(endpointId, requestId, Double.class, new JsonRpcPromise<>());

    }

    public JsonRpcPromise<Boolean> sendAndReceiveResultAsBoolean() {
        final String requestId = transmitRequest();

        LOG.debug("Transmitting request: " +
                  "endpoint ID: " + endpointId + ", " +
                  "request ID: " + requestId + ", " +
                  "method: " + method + ", " +
                  (pValue != null ? "params object class: " + pValue.getClass() + ", " : "") +
                  "params list value" + pValue + ", " +
                  "result object class: " + Boolean.class);

        return dispatcher.registerPromiseOfOne(endpointId, requestId, Boolean.class, new JsonRpcPromise<>());
    }

    public <R> JsonRpcPromise<List<R>> sendAndReceiveResultAsListOfDto(final Class<R> rClass) {
        checkNotNull(rClass, "Result class value must not be null");

        final String requestId = transmitRequest();

        LOG.debug("Transmitting request: " +
                  "endpoint ID: " + endpointId + ", " +
                  "request ID: " + requestId + ", " +
                  "method: " + method + ", " +
                  (pValue != null ? "params object class: " + pValue.getClass() + ", " : "") +
                  "params list value" + pValue + ", " +
                  "result list items class: " + rClass);

        return dispatcher.registerPromiseOfMany(endpointId, requestId, rClass, new JsonRpcPromise<>());

    }

    public JsonRpcPromise<List<String>> sendAndReceiveResultAsListOfString() {
        final String requestId = transmitRequest();

        LOG.debug("Transmitting request: " +
                  "endpoint ID: " + endpointId + ", " +
                  "request ID: " + requestId + ", " +
                  "method: " + method + ", " +
                  (pValue != null ? "params object class: " + pValue.getClass() + ", " : "") +
                  "params list value" + pValue + ", " +
                  "result list items class: " + String.class);

        return dispatcher.registerPromiseOfMany(endpointId, requestId, String.class, new JsonRpcPromise<>());
    }

    public JsonRpcPromise<List<Boolean>> sendAndReceiveResultAsListOfBoolean() {
        final String requestId = transmitRequest();

        LOG.debug("Transmitting request: " +
                  "endpoint ID: " + endpointId + ", " +
                  "request ID: " + requestId + ", " +
                  "method: " + method + ", " +
                  (pValue != null ? "params object class: " + pValue.getClass() + ", " : "") +
                  "params list value" + pValue + ", " +
                  "result list items class: " + Boolean.class);

        return dispatcher.registerPromiseOfMany(endpointId, requestId, Boolean.class, new JsonRpcPromise<>());
    }

    public JsonRpcPromise<Void> sendAndReceiveResultAsEmpty() {
        final String requestId = transmitRequest();

        LOG.debug("Transmitting request: " +
                  "endpoint ID: " + endpointId + ", " +
                  "request ID: " + requestId + ", " +
                  "method: " + method + ", " +
                  (pValue != null ? "params object class: " + pValue.getClass() + ", " : "") +
                  "params list value" + pValue + ", " +
                  "result list items class: " + Void.class);

        return dispatcher.registerPromiseOfOne(endpointId, requestId, Void.class, new JsonRpcPromise<>());
    }

    private String transmitRequest() {
        Integer id = MethodNameConfigurator.id.incrementAndGet();
        String requestId = id.toString();

        JsonRpcParams params = factory.createParams(pValue);
        JsonRpcRequest request = factory.createRequest(requestId, method, params);
        transmitter.transmit(endpointId, request.toString());
        return requestId;
    }

    private void transmitNotification() {
        JsonRpcParams params = factory.createParams(pValue);
        JsonRpcRequest request = factory.createRequest(method, params);
        if (endpointId != null) {
            transmitter.transmit(endpointId, request.toString());
        }
    }
}
