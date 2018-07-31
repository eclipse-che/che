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
package org.eclipse.che.api.core.jsonrpc.impl;

import static org.eclipse.che.api.core.jsonrpc.commons.JsonRpcUtils.cast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcParams;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcRequest;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcResponse;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcResult;
import org.eclipse.che.dto.server.DtoFactory;

public class GsonJsonRpcMarshaller implements JsonRpcMarshaller {
  private final JsonParser jsonParser;
  private final Gson gson;

  @Inject
  public GsonJsonRpcMarshaller(JsonParser jsonParser, Gson gson) {
    this.jsonParser = jsonParser;
    this.gson = gson;
  }

  @Override
  public String marshall(JsonRpcResponse response) {
    JsonElement jsonId = getId(response);
    JsonElement jsonResult = getResult(response);
    JsonElement jsonError = getError(response);

    return getResponse(jsonId, jsonResult, jsonError).toString();
  }

  @Override
  public String marshall(JsonRpcRequest request) {
    JsonElement method = getMethod(request);
    JsonElement id = getId(request);
    JsonElement params = getParams(request);

    return getRequest(method, id, params).toString();
  }

  private JsonObject getRequest(
      JsonElement jsonMethod, JsonElement jsonId, JsonElement jsonParams) {
    JsonObject jsonRequest = new JsonObject();
    jsonRequest.addProperty("jsonrpc", "2.0");
    jsonRequest.add("method", jsonMethod);

    if (jsonId != null) {
      jsonRequest.add("id", jsonId);
    }

    if (jsonParams != null) {
      jsonRequest.add("params", jsonParams);
    }
    return jsonRequest;
  }

  private JsonElement getParams(JsonRpcRequest request) {
    if (!request.hasParams()) {
      return null;
    }

    JsonRpcParams params = request.getParams();
    return params.isSingle() ? getElement(params.getOne()) : getElements(params.getMany());
  }

  private JsonElement getId(JsonRpcRequest request) {
    return request.hasId() ? new JsonPrimitive(request.getId()) : null;
  }

  private JsonPrimitive getMethod(JsonRpcRequest request) {
    return new JsonPrimitive(request.getMethod());
  }

  private JsonObject getResponse(
      JsonElement jsonId, JsonElement jsonResult, JsonElement jsonError) {
    JsonObject jsonResponse = new JsonObject();
    jsonResponse.addProperty("jsonrpc", "2.0");
    if (jsonId != null) {
      jsonResponse.add("id", jsonId);
    }

    if (jsonResult != null) {
      jsonResponse.add("result", jsonResult);
    } else {
      jsonResponse.add("error", jsonError);
    }
    return jsonResponse;
  }

  private JsonObject getError(JsonRpcResponse response) {
    if (!response.hasError()) {
      return null;
    }

    JsonObject jsonError = new JsonObject();
    JsonRpcError error = response.getError();
    jsonError.add("code", new JsonPrimitive(error.getCode()));
    jsonError.add("message", new JsonPrimitive(error.getMessage()));
    return jsonError;
  }

  private JsonElement getResult(JsonRpcResponse response) {
    if (!response.hasResult()) {
      return null;
    }

    JsonRpcResult result = response.getResult();
    return result.isSingle() ? getElement(result.getOne()) : getElements(result.getMany());
  }

  private JsonElement getId(JsonRpcResponse response) {
    return response.hasId() ? new JsonPrimitive(response.getId()) : null;
  }

  private JsonElement getElements(List<?> params) {
    JsonArray elements = new JsonArray();
    params.forEach(param -> elements.add(getJsonElement(param)));
    return elements;
  }

  private JsonElement getElement(Object param) {
    JsonElement jsonElement = getJsonElement(param);
    if (jsonElement.isJsonObject()) {
      return jsonElement;
    }

    JsonArray array = new JsonArray();
    array.add(jsonElement);
    return array;
  }

  private JsonElement getJsonElement(Object param) {
    if (param == null) {
      return JsonNull.INSTANCE;
    }
    if (param instanceof JsonElement) {
      return cast(param);
    }
    if (param instanceof String) {
      return new JsonPrimitive((String) param);
    }
    if (param instanceof Boolean) {
      return new JsonPrimitive((Boolean) param);
    }
    if (param instanceof Double) {
      return new JsonPrimitive((Double) param);
    }
    try {
      return jsonParser.parse(DtoFactory.getInstance().toJson(param));
    } catch (IllegalArgumentException e) {
      return gson.toJsonTree(param);
    }
  }
}
