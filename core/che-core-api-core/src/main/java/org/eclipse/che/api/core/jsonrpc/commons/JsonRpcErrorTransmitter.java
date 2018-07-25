/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.commons;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.slf4j.Logger;

/** Transmits an instance of {@link JsonRpcException} to an endpoint */
@Singleton
public class JsonRpcErrorTransmitter {
  private static final Logger LOGGER = getLogger(JsonRpcErrorTransmitter.class);

  private final WebSocketMessageTransmitter transmitter;
  private final JsonRpcMarshaller marshaller;

  @Inject
  public JsonRpcErrorTransmitter(
      WebSocketMessageTransmitter transmitter, JsonRpcMarshaller marshaller) {
    this.transmitter = transmitter;
    this.marshaller = marshaller;
  }

  public void transmit(String endpointId, JsonRpcException e) {
    checkNotNull(endpointId, "Endpoint ID must not be null");
    checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");

    LOGGER.debug("Transmitting a JSON RPC error: " + e.getMessage());

    JsonRpcError error =
        new JsonRpcError(e.getCode(), e.getMessage() == null ? "Unexpected error" : e.getMessage());
    JsonRpcResponse response = new JsonRpcResponse(e.getId(), null, error);
    String message = marshaller.marshall(response);
    transmitter.transmit(endpointId, message);
  }
}
