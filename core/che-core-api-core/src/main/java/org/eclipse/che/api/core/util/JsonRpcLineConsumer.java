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
package org.eclipse.che.api.core.util;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.slf4j.Logger;

public class JsonRpcLineConsumer implements LineConsumer {
  private static final Logger LOG = getLogger(JsonRpcLineConsumer.class);

  private final String method;
  private final RequestTransmitter transmitter;
  private final JsonRpcEndpointIdProvider jsonRpcEndpointIdProvider;

  public JsonRpcLineConsumer(
      RequestTransmitter transmitter,
      String method,
      JsonRpcEndpointIdProvider jsonRpcEndpointIdProvider) {
    this.method = method;
    this.transmitter = transmitter;
    this.jsonRpcEndpointIdProvider = jsonRpcEndpointIdProvider;
  }

  @Override
  public void writeLine(String line) throws IOException {
    try {
      jsonRpcEndpointIdProvider
          .get()
          .forEach(
              it ->
                  transmitter
                      .newRequest()
                      .endpointId(it)
                      .methodName(method)
                      .paramsAsString(line)
                      .sendAndSkipResult());
    } catch (IllegalStateException e) {
      LOG.error("Error trying to send a line: {}", line);
    }
  }

  @Override
  public void close() throws IOException {}
}
