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

import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.ResponseDispatcher;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.slf4j.Logger;

/** Method name configurator to defined method name that the request will have. */
public class MethodNameConfigurator {
  private static final Logger LOGGER = getLogger(MethodNameConfigurator.class);

  public static AtomicInteger id = new AtomicInteger(0);

  private final JsonRpcMarshaller marshaller;
  private final ResponseDispatcher dispatcher;
  private final WebSocketMessageTransmitter transmitter;

  private final String endpointId;

  @Inject
  MethodNameConfigurator(
      JsonRpcMarshaller marshaller,
      ResponseDispatcher dispatcher,
      WebSocketMessageTransmitter transmitter,
      String endpointId) {
    this.marshaller = marshaller;
    this.dispatcher = dispatcher;
    this.transmitter = transmitter;

    this.endpointId = endpointId;
  }

  public ParamsConfigurator methodName(String name) {
    checkNotNull(name, "Method name must not be null");
    checkArgument(!name.isEmpty(), "Method name must not be empty");

    LOGGER.debug("Configuring outgoing request method name name: " + name);

    return new ParamsConfigurator(marshaller, dispatcher, transmitter, name, endpointId);
  }
}
