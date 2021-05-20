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

    LOGGER.trace("Validating message: {}", message);

    try {
      jsonParser.parse(message);

      LOGGER.trace("Validation successful");
      return true;
    } catch (JsonParseException e) {
      LOGGER.warn("Validation failed: {}", e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isJsonRpcRequest(String message) {
    checkNotNull(message, "Message must not be null");
    checkArgument(!message.isEmpty(), "Message must not be empty");
    LOGGER.trace("Qualifying message: " + message);

    JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
    LOGGER.trace(
        "Json keys: "
            + jsonObject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));

    if (jsonObject.has("method")) {
      LOGGER.trace("Qualified to request");
      return true;
    } else {
      LOGGER.trace("Qualified to response");
      return false;
    }
  }

  @Override
  public boolean isJsonRpcResponse(String message) {
    checkNotNull(message, "Message must not be null");
    checkArgument(!message.isEmpty(), "Message must not be empty");
    LOGGER.trace("Qualifying message: " + message);

    JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
    LOGGER.trace(
        "Json keys: "
            + jsonObject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));

    if (jsonObject.has("error") != jsonObject.has("result")) {
      LOGGER.trace("Qualified to response");
      return true;
    }
    return false;
  }
}
