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
package org.eclipse.che.api.core.jsonrpc.commons.transmission;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcErrorTransmitter;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcRequest;
import org.eclipse.che.api.core.jsonrpc.commons.ResponseDispatcher;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.slf4j.Logger;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Configurator defines the type of a result (if present) and send a request.
 * Result types that are supported: {@link String}, {@link Boolean},
 * {@link Double}, {@link Void} and DTO. This configurator is used when we
 * have not set request params.
 */
public class SendConfiguratorFromNone {
    private final static Logger LOGGER = getLogger(SendConfiguratorFromNone.class);

    private final ResponseDispatcher          dispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcMarshaller           marshaller;

    private final String method;
    private final String endpointId;

    SendConfiguratorFromNone(JsonRpcMarshaller marshaller, ResponseDispatcher dispatcher, WebSocketMessageTransmitter transmitter,
                             String method, String endpointId) {
        this.marshaller = marshaller;
        this.dispatcher = dispatcher;
        this.transmitter = transmitter;

        this.method = method;
        this.endpointId = endpointId;
    }

    public void sendAndSkipResult() {
        LOGGER.debug("Transmitting request: " +
                     "endpoint ID: " + endpointId + ", " +
                     "method: " + method);

        transmitNotification();
    }

    public <R> JsonRpcPromise<R> sendAndReceiveResultAsDto(final Class<R> rClass) {
        checkNotNull(rClass, "Result class value must not be null");

        final String requestId = transmitRequest();

        LOGGER.debug("Transmitting request: " +
                     "endpoint ID: " + endpointId + ", " +
                     "request ID: " + requestId + ", " +
                     "method: " + method + ", " +
                     "result object class: " + rClass);

        return dispatcher.registerPromiseOfOne(endpointId, requestId, rClass);
    }


    public JsonRpcPromise<String> sendAndReceiveResultAsString() {
        final String requestId = transmitRequest();

        LOGGER.debug("Transmitting request: " +
                     "endpoint ID: " + endpointId + ", " +
                     "request ID: " + requestId + ", " +
                     "method: " + method + ", " +
                     "result object class: " + String.class);

        return dispatcher.registerPromiseOfOne(endpointId, requestId, String.class);

    }

    public JsonRpcPromise<Double> sendAndReceiveResultAsDouble() {
        final String requestId = transmitRequest();

        LOGGER.debug("Transmitting request: " +
                     "endpoint ID: " + endpointId + ", " +
                     "request ID: " + requestId + ", " +
                     "method: " + method + ", " +
                     "result object class: " + Double.class);

        return dispatcher.registerPromiseOfOne(endpointId, requestId, Double.class);

    }

    public JsonRpcPromise<Boolean> sendAndReceiveResultAsBoolean() {
        final String requestId = transmitRequest();

        LOGGER.debug("Transmitting request: " +
                     "endpoint ID: " + endpointId + ", " +
                     "request ID: " + requestId + ", " +
                     "method: " + method + ", " +
                     "result object class: " + Boolean.class);

        return dispatcher.registerPromiseOfOne(endpointId, requestId, Boolean.class);
    }

    public <R> JsonRpcPromise<List<R>> sendAndReceiveResultAsListOfDto(final Class<R> rClass) {
        checkNotNull(rClass, "Result class value must not be null");

        final String requestId = transmitRequest();

        LOGGER.debug("Transmitting request: " +
                     "endpoint ID: " + endpointId + ", " +
                     "request ID: " + requestId + ", " +
                     "method: " + method + ", " +
                     "result list items class: " + rClass);

        return dispatcher.registerPromiseOfMany(endpointId, requestId, rClass);

    }

    public JsonRpcPromise<List<String>> sendAndReceiveResultAsListOfString() {
        final String requestId = transmitRequest();

        LOGGER.debug("Transmitting request: " +
                     "endpoint ID: " + endpointId + ", " +
                     "request ID: " + requestId + ", " +
                     "method: " + method + ", " +
                     "result list items class: " + String.class);

        return dispatcher.registerPromiseOfMany(endpointId, requestId, String.class);
    }

    public JsonRpcPromise<List<Boolean>> sendAndReceiveResultAsListOfBoolean() {
        final String requestId = transmitRequest();

        LOGGER.debug("Transmitting request: " +
                     "endpoint ID: " + endpointId + ", " +
                     "request ID: " + requestId + ", " +
                     "method: " + method + ", " +
                     "result list items class: " + Boolean.class);

        return dispatcher.registerPromiseOfMany(endpointId, requestId, Boolean.class);
    }

    public JsonRpcPromise<Void> sendAndReceiveResultAsEmpty() {
        final String requestId = transmitRequest();

        LOGGER.debug("Transmitting request: " +
                     "endpoint ID: " + endpointId + ", " +
                     "request ID: " + requestId + ", " +
                     "method: " + method + ", " +
                     "result list items class: " + Void.class);

        return dispatcher.registerPromiseOfOne(endpointId, requestId, Void.class);
    }

    private void transmitNotification() {
        JsonRpcRequest request = new JsonRpcRequest(null, method, null);
        String message = marshaller.marshall(request);
        transmitter.transmit(endpointId, message);
    }

    private String transmitRequest() {
        Integer id = MethodNameConfigurator.id.incrementAndGet();
        String requestId = id.toString();

        JsonRpcRequest request = new JsonRpcRequest(requestId, method, null);
        String message = marshaller.marshall(request);
        transmitter.transmit(endpointId, message);
        return requestId;
    }
}
