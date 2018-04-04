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
package org.eclipse.che.api.core.jsonrpc.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcQualifier;
import org.slf4j.Logger;

@Singleton
public class GsonJsonRpcQualifier implements JsonRpcQualifier {
  private static final Logger LOGGER = getLogger(GsonJsonRpcQualifier.class);

  private final JsonParser jsonParser;

  @Inject
  public GsonJsonRpcQualifier(JsonParser jsonParser) {
    this.jsonParser = jsonParser;
  }

  @Override
  public boolean isValidJson(String message) {
    checkNotNull(message, "Message must not be null");
    checkArgument(!message.isEmpty(), "Message must not be empty");

    LOGGER.debug("Validating message: {}", message);

    try {
      jsonParser.parse(message);

      LOGGER.debug("Validation successful");
      return true;
    } catch (JsonParseException e) {
      LOGGER.debug("Validation failed: {}", e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isJsonRpcRequest(String message) {
    checkNotNull(message, "Message must not be null");
    checkArgument(!message.isEmpty(), "Message must not be empty");
    LOGGER.debug("Qualifying message: " + message);

    JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
    LOGGER.debug(
        "Json keys: "
            + jsonObject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));

    if (jsonObject.has("method")) {
      LOGGER.debug("Qualified to request");
      return true;
    } else {
      LOGGER.debug("Qualified to response");
      return false;
    }
  }

  @Override
  public boolean isJsonRpcResponse(String message) {
    checkNotNull(message, "Message must not be null");
    checkArgument(!message.isEmpty(), "Message must not be empty");
    LOGGER.debug("Qualifying message: " + message);

    JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
    LOGGER.debug(
        "Json keys: "
            + jsonObject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));

    if (jsonObject.has("error") != jsonObject.has("result")) {
      LOGGER.debug("Qualified to response");
      return true;
    }
    return false;
  }
}
