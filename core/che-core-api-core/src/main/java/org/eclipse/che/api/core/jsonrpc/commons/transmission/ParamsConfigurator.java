/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.commons.transmission;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.ResponseDispatcher;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.slf4j.Logger;

/**
 * Params configurator provide means to configure params type in a request that is to be sent.
 * Params types that are supported: {@link String}, {@link Boolean}, {@link Double}, {@link Void}
 * and DTO.
 */
public class ParamsConfigurator {
  private static final Logger LOGGER = getLogger(ParamsConfigurator.class);

  private final JsonRpcMarshaller marshaller;
  private final ResponseDispatcher dispatcher;
  private final WebSocketMessageTransmitter transmitter;

  private final String method;
  private final String endpointId;

  ParamsConfigurator(
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

  public <P> SendConfiguratorFromOne<P> paramsAsDto(P pValue) {
    checkNotNull(pValue, "Params value must not be null");

    LOGGER.debug(
        "Configuring outgoing request params: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "method: "
            + method
            + ", "
            + "params object class: "
            + pValue.getClass()
            + ", "
            + "params object value: "
            + pValue);

    return new SendConfiguratorFromOne<>(
        marshaller, dispatcher, transmitter, method, pValue, endpointId);
  }

  public SendConfiguratorFromOne<Double> paramsAsDouble(Double pValue) {
    checkNotNull(pValue, "Params value must not be null");

    LOGGER.debug(
        "Configuring outgoing request params: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "method: "
            + method
            + ", "
            + "params object class: "
            + Double.class
            + ", "
            + "params object value: "
            + pValue);

    return new SendConfiguratorFromOne<>(
        marshaller, dispatcher, transmitter, method, pValue, endpointId);
  }

  public SendConfiguratorFromOne<String> paramsAsString(String pValue) {
    checkNotNull(pValue, "Params value must not be null");

    LOGGER.debug(
        "Configuring outgoing request params: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "method: "
            + method
            + ", "
            + "params object class: "
            + String.class
            + ", "
            + "params object value: "
            + pValue);

    return new SendConfiguratorFromOne<>(
        marshaller, dispatcher, transmitter, method, pValue, endpointId);
  }

  public SendConfiguratorFromOne<Boolean> paramsAsBoolean(Boolean pValue) {
    checkNotNull(pValue, "Params value must not be null");

    LOGGER.debug(
        "Configuring outgoing request params: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "method: "
            + method
            + ", "
            + "params object class: "
            + Boolean.class
            + ", "
            + "params object value: "
            + pValue);

    return new SendConfiguratorFromOne<>(
        marshaller, dispatcher, transmitter, method, pValue, endpointId);
  }

  public SendConfiguratorFromNone noParams() {
    LOGGER.debug(
        "Configuring outgoing request params: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "method: "
            + method
            + ", "
            + "params object class: "
            + Void.class
            + ", "
            + "params object value: void");

    return new SendConfiguratorFromNone(marshaller, dispatcher, transmitter, method, endpointId);
  }

  public <P> SendConfiguratorFromMany<P> paramsAsListOfDto(List<P> pListValue) {
    checkNotNull(pListValue, "Params list value must not be null");
    checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

    LOGGER.debug(
        "Configuring outgoing request params: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "method: "
            + method
            + ", "
            + "params list items class: "
            + pListValue.iterator().next().getClass()
            + ", "
            + "params list value: "
            + pListValue);

    return new SendConfiguratorFromMany<>(
        marshaller, dispatcher, transmitter, method, pListValue, endpointId);
  }

  public SendConfiguratorFromMany<String> paramsAsListOfString(List<String> pListValue) {
    checkNotNull(pListValue, "Params list value must not be null");
    checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

    LOGGER.debug(
        "Configuring outgoing request params: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "method: "
            + method
            + ", "
            + "params list items class: "
            + String.class
            + ", "
            + "params list value: "
            + pListValue);
    return new SendConfiguratorFromMany<>(
        marshaller, dispatcher, transmitter, method, pListValue, endpointId);
  }

  public SendConfiguratorFromMany<Double> paramsAsListOfDouble(List<Double> pListValue) {
    checkNotNull(pListValue, "Params list value must not be null");
    checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

    LOGGER.debug(
        "Configuring outgoing request params: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "method: "
            + method
            + ", "
            + "params list items class: "
            + Double.class
            + ", "
            + "params list value: "
            + pListValue);

    return new SendConfiguratorFromMany<>(
        marshaller, dispatcher, transmitter, method, pListValue, endpointId);
  }

  public SendConfiguratorFromMany<Boolean> paramsAsListOfBoolean(List<Boolean> pListValue) {
    checkNotNull(pListValue, "Params list value must not be null");
    checkArgument(!pListValue.isEmpty(), "Params list value must not be empty");

    LOGGER.debug(
        "Configuring outgoing request params: "
            + "endpoint ID: "
            + endpointId
            + ", "
            + "method: "
            + method
            + ", "
            + "params list items class: "
            + Boolean.class
            + ", "
            + "params list value: "
            + pListValue);

    return new SendConfiguratorFromMany<>(
        marshaller, dispatcher, transmitter, method, pListValue, endpointId);
  }
}
