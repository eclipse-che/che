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
import static java.util.Collections.singletonList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcParams;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcRequest;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcResponse;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcResult;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcUnmarshaller;

@Singleton
public class GsonJsonRpcUnmarshaller implements JsonRpcUnmarshaller {
  private final JsonParser jsonParser;

  @Inject
  public GsonJsonRpcUnmarshaller(JsonParser jsonParser) {
    this.jsonParser = jsonParser;
  }

  @Override
  public List<String> unmarshalArray(String message) {
    return getArray(message, jsonParser.parse(message).isJsonArray());
  }

  @Override
  public JsonRpcRequest unmarshalRequest(String message) {
    checkNotNull(message, "Message must not be null");
    checkArgument(!message.isEmpty(), "Message must not be empty");

    JsonObject request = jsonParser.parse(message).getAsJsonObject();

    String method = getMethod(request);
    String id = getId(request);
    JsonRpcParams params = getParams(request);

    return new JsonRpcRequest(id, method, params);
  }

  @Override
  public JsonRpcResponse unmarshalResponse(String message) {
    checkNotNull(message, "Message must not be null");
    checkArgument(!message.isEmpty(), "Message must not be empty");

    JsonObject response = jsonParser.parse(message).getAsJsonObject();

    String id = getId(response);
    JsonRpcResult result = getResult(response);
    JsonRpcError error = getError(response);

    return new JsonRpcResponse(id, result, error);
  }

  private JsonRpcError getError(JsonObject response) {
    if (!response.has("error")) {
      return null;
    }

    int code = response.get("error").getAsJsonObject().get("code").getAsInt();
    String errorMessage = response.get("error").getAsJsonObject().get("message").getAsString();
    return new JsonRpcError(code, errorMessage);
  }

  private JsonRpcResult getResult(JsonObject response) {
    if (!response.has("result")) {
      return null;
    }

    JsonElement jsonElement = response.get("result");
    if (!jsonElement.isJsonArray()) {
      return new JsonRpcResult(getInnerItem(jsonElement));
    }

    JsonArray jsonArray = jsonElement.getAsJsonArray();
    int size = jsonArray.size();
    List<Object> innerResults = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      JsonElement innerJsonElement = jsonArray.get(i);
      innerResults.add(getInnerItem(innerJsonElement));
    }

    return new JsonRpcResult(innerResults);
  }

  private JsonRpcParams getParams(JsonObject jsonObject) {
    if (!jsonObject.has("params")) {
      return null;
    }

    JsonElement jsonElement = jsonObject.get("params");
    if (!jsonElement.isJsonArray()) {
      return new JsonRpcParams(getInnerItem(jsonElement));
    }

    JsonArray jsonArray = jsonElement.getAsJsonArray();
    int size = jsonArray.size();
    List<Object> innerParameters = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      JsonElement innerJsonElement = jsonArray.get(i);
      innerParameters.add(getInnerItem(innerJsonElement));
    }

    return new JsonRpcParams(innerParameters);
  }

  private String getId(JsonObject jsonObject) {
    return jsonObject.has("id") ? jsonObject.get("id").getAsString() : null;
  }

  private String getMethod(JsonObject jsonObject) {
    return jsonObject.get("method").getAsString();
  }

  private List<String> getArray(String message, boolean isArray) {
    if (!isArray) {
      return singletonList(message);
    }

    JsonArray jsonArray = jsonParser.parse(message).getAsJsonArray();
    int size = jsonArray.size();
    List<String> result = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      JsonElement jsonElement = jsonArray.get(i);
      result.add(jsonElement.toString());
    }
    return result;
  }

  private Object getInnerItem(JsonElement jsonElement) {
    if (jsonElement.isJsonNull()) {
      return null;
    }

    if (jsonElement.isJsonObject()) {
      return jsonElement.getAsJsonObject();
    }

    if (jsonElement.isJsonPrimitive()) {
      JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
      if (jsonPrimitive.isNumber()) {
        return jsonPrimitive.getAsDouble();
      } else if (jsonPrimitive.isString()) {
        return jsonPrimitive.getAsString();
      } else {
        return jsonPrimitive.getAsBoolean();
      }
    }

    throw new IllegalStateException("Unexpected json element type");
  }
}
