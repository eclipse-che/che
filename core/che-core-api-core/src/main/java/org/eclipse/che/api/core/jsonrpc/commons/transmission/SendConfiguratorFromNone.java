/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.commons.transmission;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcRequest;
import org.eclipse.che.api.core.jsonrpc.commons.ResponseDispatcher;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.slf4j.Logger;

/**
 * Configurator defines the type of a result (if present) and send a request. Result types that are
 * supported: {@link String}, {@link Boolean}, {@link Double}, {@link Void} and DTO. This
 * configurator is used when we have not set request params.
 */
public class SendConfiguratorFromNone {
  private static final Logger LOGGER = getLogger(SendConfiguratorFromNone.class);

  private final ResponseDispatcher dispatcher;
  private final WebSocketMessageTransmitter transmitter;
  private final JsonRpcMarshaller marshaller;

  private final String method;
  private final String endpointId;

  SendConfiguratorFromNone(
      JsonRpcMarshaller marshaller,
      ResponseDispatcher dispatcher,
      WebSocketMessageTransmitter transmitter,
      String method,
      String endpointId) {
    this.marshaller = marshaller;
    this.dispatcher = dispatcher;
    this.transmitter = transmitter;

    this.method = method;
    this.endpointId = endpointId;
  }

  public void sendAndSkipResult() {
    LOGGER.debug(
        "Transmitting request: " + "endpoint ID: " + endpointId + ", " + "method: " + method);

    transmitNotification();
  }

  public <R> JsonRpcPromise<R> sendAndReceiveResultAsDto(final Class<R> rClass) {
    return sendAndReceiveResultAsDto(rClass, 0);
  }

  public <R> JsonRpcPromise<R> sendAndReceiveResultAsDto(final Class<R> rClass, int timeInMillis) {
    checkNotNull(rClass, "Result class value must not be null");

    final String requestId = transmitRequest();

    LOGGER.debug(
        "Transmitting request: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "request ID: "
            + requestId
            + ", "
            + "method: "
            + method
            + ", "
            + "result object class: "
            + rClass);

    return dispatcher.registerPromiseForSingleObject(endpointId, requestId, rClass, timeInMillis);
  }

  public JsonRpcPromise<String> sendAndReceiveResultAsString() {
    return sendAndReceiveResultAsString(0);
  }

  public JsonRpcPromise<String> sendAndReceiveResultAsString(int timeInMillis) {
    final String requestId = transmitRequest();

    LOGGER.debug(
        "Transmitting request: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "request ID: "
            + requestId
            + ", "
            + "method: "
            + method
            + ", "
            + "result object class: "
            + String.class);

    return dispatcher.registerPromiseForSingleObject(
        endpointId, requestId, String.class, timeInMillis);
  }

  public JsonRpcPromise<Double> sendAndReceiveResultAsDouble() {
    return sendAndReceiveResultAsDouble(0);
  }

  public JsonRpcPromise<Double> sendAndReceiveResultAsDouble(int timeInMillis) {
    final String requestId = transmitRequest();

    LOGGER.debug(
        "Transmitting request: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "request ID: "
            + requestId
            + ", "
            + "method: "
            + method
            + ", "
            + "result object class: "
            + Double.class);

    return dispatcher.registerPromiseForSingleObject(
        endpointId, requestId, Double.class, timeInMillis);
  }

  public JsonRpcPromise<Boolean> sendAndReceiveResultAsBoolean() {
    return sendAndReceiveResultAsBoolean(0);
  }

  public JsonRpcPromise<Boolean> sendAndReceiveResultAsBoolean(int timeInMillis) {
    final String requestId = transmitRequest();

    LOGGER.debug(
        "Transmitting request: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "request ID: "
            + requestId
            + ", "
            + "method: "
            + method
            + ", "
            + "result object class: "
            + Boolean.class);

    return dispatcher.registerPromiseForSingleObject(
        endpointId, requestId, Boolean.class, timeInMillis);
  }

  public <R> JsonRpcPromise<List<R>> sendAndReceiveResultAsListOfDto(final Class<R> rClass) {
    return sendAndReceiveResultAsListOfDto(rClass, 0);
  }

  public <R> JsonRpcPromise<List<R>> sendAndReceiveResultAsListOfDto(
      final Class<R> rClass, int timeInMillis) {
    checkNotNull(rClass, "Result class value must not be null");

    final String requestId = transmitRequest();

    LOGGER.debug(
        "Transmitting request: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "request ID: "
            + requestId
            + ", "
            + "method: "
            + method
            + ", "
            + "result list items class: "
            + rClass);

    return dispatcher.registerPromiseForListOfObjects(endpointId, requestId, rClass, timeInMillis);
  }

  public JsonRpcPromise<List<String>> sendAndReceiveResultAsListOfString() {
    return sendAndReceiveResultAsListOfString(0);
  }

  public JsonRpcPromise<List<String>> sendAndReceiveResultAsListOfString(int timeInMillis) {
    final String requestId = transmitRequest();

    LOGGER.debug(
        "Transmitting request: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "request ID: "
            + requestId
            + ", "
            + "method: "
            + method
            + ", "
            + "result list items class: "
            + String.class);

    return dispatcher.registerPromiseForListOfObjects(
        endpointId, requestId, String.class, timeInMillis);
  }

  public JsonRpcPromise<List<Boolean>> sendAndReceiveResultAsListOfBoolean() {
    return sendAndReceiveResultAsListOfBoolean(0);
  }

  public JsonRpcPromise<List<Boolean>> sendAndReceiveResultAsListOfBoolean(int timeInMillis) {
    final String requestId = transmitRequest();

    LOGGER.debug(
        "Transmitting request: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "request ID: "
            + requestId
            + ", "
            + "method: "
            + method
            + ", "
            + "result list items class: "
            + Boolean.class);

    return dispatcher.registerPromiseForListOfObjects(
        endpointId, requestId, Boolean.class, timeInMillis);
  }

  public JsonRpcPromise<List<Double>> sendAndReceiveResultAsListOfDouble() {
    return sendAndReceiveResultAsListOfDouble(0);
  }

  public JsonRpcPromise<List<Double>> sendAndReceiveResultAsListOfDouble(int timeInMillis) {
    final String requestId = transmitRequest();

    LOGGER.debug(
        "Transmitting request: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "request ID: "
            + requestId
            + ", "
            + "method: "
            + method
            + ", "
            + "result list items class: "
            + Double.class);

    return dispatcher.registerPromiseForListOfObjects(
        endpointId, requestId, Double.class, timeInMillis);
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
