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
package org.eclipse.che.api.core.jsonrpc.commons;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Dispatches incoming JSON RPC requests and notifications. If during dispatching happens any kind
 * of error related to JSON RPC it throws appropriate exception {@link JsonRpcException}.
 */
@Singleton
public class RequestDispatcher {
  private static final Logger LOGGER = getLogger(RequestDispatcher.class);

  private final RequestHandlerManager requestHandlerManager;

  @Inject
  public RequestDispatcher(RequestHandlerManager requestHandlerManager) {
    this.requestHandlerManager = requestHandlerManager;
  }

  public void dispatch(String endpointId, JsonRpcRequest request) throws JsonRpcException {
    checkNotNull(endpointId, "Endpoint ID must not be null");
    checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");
    checkNotNull(request, "Request must not be null");

    LOGGER.debug("Dispatching request: " + request + ", endpoint: " + endpointId);

    String method = request.getMethod();

    JsonRpcParams params = request.getParams();

    if (request.hasId()) {
      LOGGER.debug("Request has ID");
      String requestId = request.getId();
      checkRequestHandlerRegistration(method, requestId);
      requestHandlerManager.handle(endpointId, requestId, method, params);
    } else {
      LOGGER.debug("Request has no ID -> it is a notification");
      checkNotificationHandlerRegistration(method);
      requestHandlerManager.handle(endpointId, method, params);
    }
  }

  private void checkNotificationHandlerRegistration(String method) throws JsonRpcException {
    if (!requestHandlerManager.isRegistered(method)) {
      LOGGER.error("No corresponding to method '" + method + "' handler is registered");
      throw new JsonRpcException(-32601, "Method '" + method + "' not registered");
    }
  }

  private void checkRequestHandlerRegistration(String method, String requestId)
      throws JsonRpcException {
    if (!requestHandlerManager.isRegistered(method)) {
      LOGGER.error("No corresponding to method '" + method + "' handler is registered");
      throw new JsonRpcException(-32601, "Method '" + method + "' not registered", requestId);
    }
  }
}
