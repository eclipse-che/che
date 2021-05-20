/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.commons;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.che.api.core.websocket.impl.WebsocketIdService.SEPARATOR;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageReceiver;
import org.slf4j.Logger;

/**
 * Receives and process messages coming from web socket service. Basically it validates, qualifies
 * and transforms a raw web socket message to a JSON RPC known structure and pass it further to
 * appropriate dispatchers. In case of any {@link JsonRpcException} happens during request/response
 * processing this class is also responsible for an error transmission.
 */
@Singleton
public class JsonRpcMessageReceiver implements WebSocketMessageReceiver {
  private static final Logger LOGGER = getLogger(JsonRpcMessageReceiver.class);

  private final RequestDispatcher requestDispatcher;
  private final ResponseDispatcher responseDispatcher;
  private final JsonRpcErrorTransmitter errorTransmitter;
  private final JsonRpcQualifier jsonRpcQualifier;
  private final JsonRpcUnmarshaller jsonRpcUnmarshaller;
  private final RequestProcessor requestProcessor;

  @Inject
  public JsonRpcMessageReceiver(
      RequestDispatcher requestDispatcher,
      ResponseDispatcher responseDispatcher,
      JsonRpcErrorTransmitter errorTransmitter,
      JsonRpcQualifier jsonRpcQualifier,
      JsonRpcUnmarshaller jsonRpcUnmarshaller,
      RequestProcessor requestProcessor) {
    this.requestDispatcher = requestDispatcher;
    this.responseDispatcher = responseDispatcher;
    this.errorTransmitter = errorTransmitter;
    this.jsonRpcQualifier = jsonRpcQualifier;
    this.jsonRpcUnmarshaller = jsonRpcUnmarshaller;
    this.requestProcessor = requestProcessor;
  }

  @Override
  public void receive(String combinedEndpointId, String message) {
    checkNotNull(combinedEndpointId, "Endpoint ID must not be null");
    checkArgument(!combinedEndpointId.isEmpty(), "Endpoint ID name must not be empty");
    checkNotNull(message, "Message must not be null");
    checkArgument(!message.isEmpty(), "Message must not be empty");

    LOGGER.trace("Receiving message: {}, from endpoint: {}", message, combinedEndpointId);
    if (!jsonRpcQualifier.isValidJson(message)) {
      String error = "An error occurred on the server while parsing the JSON text";
      errorTransmitter.transmit(combinedEndpointId, new JsonRpcException(-32700, error));
    }

    List<String> messages = jsonRpcUnmarshaller.unmarshalArray(message);
    for (String innerMessage : messages) {
      if (jsonRpcQualifier.isJsonRpcRequest(innerMessage)) {
        String endpointId = combinedEndpointId.split(SEPARATOR)[1];
        ProcessRequestTask task = new ProcessRequestTask(combinedEndpointId, innerMessage);
        requestProcessor.process(endpointId, task);
      } else if (jsonRpcQualifier.isJsonRpcResponse(innerMessage)) {
        processResponse(combinedEndpointId, innerMessage);
      } else {
        processError();
      }
    }
  }

  private void processError() {
    String error = "Something wen't wrong during incoming websocket message parsing";
    IllegalStateException exception = new IllegalStateException(error);
    LOGGER.error(error, exception);
    throw exception;
  }

  private void processResponse(String endpointId, String innerMessage) {
    JsonRpcResponse response = jsonRpcUnmarshaller.unmarshalResponse(innerMessage);
    responseDispatcher.dispatch(endpointId, response);
  }

  private class ProcessRequestTask implements Runnable {

    private final String endpointId;
    private final String innerMessage;

    public ProcessRequestTask(String endpointId, String innerMessage) {
      this.endpointId = endpointId;
      this.innerMessage = innerMessage;
    }

    @Override
    public void run() {
      JsonRpcRequest request = null;
      try {
        request = jsonRpcUnmarshaller.unmarshalRequest(innerMessage);
        requestDispatcher.dispatch(endpointId, request);
      } catch (JsonRpcException e) {
        if (request == null || request.getId() == null) {
          errorTransmitter.transmit(endpointId, e);
        } else {
          errorTransmitter.transmit(
              endpointId, new JsonRpcException(e.getCode(), e.getMessage(), request.getId()));
        }
      }
    }

    @Override
    public String toString() {
      return "JsonRPC request `" + innerMessage + "` for " + endpointId;
    }
  }
}
