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
package org.eclipse.che.api.core.util;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.slf4j.Logger;

public class JsonRpcMessageConsumer<T> implements MessageConsumer<T> {
  private static final Logger LOG = getLogger(JsonRpcMessageConsumer.class);

  private final String method;
  private final RequestTransmitter transmitter;
  private final JsonRpcEndpointIdProvider jsonRpcEndpointIdProvider;

  public JsonRpcMessageConsumer(
      String method,
      RequestTransmitter transmitter,
      JsonRpcEndpointIdProvider jsonRpcEndpointIdProvider) {
    this.method = method;
    this.transmitter = transmitter;
    this.jsonRpcEndpointIdProvider = jsonRpcEndpointIdProvider;
  }

  @Override
  public void consume(T message) throws IOException {
    try {
      jsonRpcEndpointIdProvider
          .get()
          .forEach(
              it ->
                  transmitter
                      .newRequest()
                      .endpointId(it)
                      .methodName(method)
                      .paramsAsDto(message)
                      .sendAndSkipResult());
    } catch (IllegalStateException e) {
      LOG.error("Error trying send line {}", message);
    }
  }

  @Override
  public void close() throws IOException {}
}
