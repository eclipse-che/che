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
package org.eclipse.che.ide.jsonrpc;

import static elemental.json.JsonType.OBJECT;
import static org.eclipse.che.api.core.jsonrpc.commons.JsonRpcUtils.cast;

import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.JsonString;
import elemental.json.JsonValue;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcParams;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcRequest;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcResponse;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcResult;
import org.eclipse.che.ide.dto.DtoFactory;

@Singleton
public class ElementalJsonRpcMarshaller implements JsonRpcMarshaller {
  private final JsonFactory jsonFactory;
  private final DtoFactory dtoFactory;

  @Inject
  public ElementalJsonRpcMarshaller(JsonFactory jsonFactory, DtoFactory dtoFactory) {
    this.jsonFactory = jsonFactory;
    this.dtoFactory = dtoFactory;
  }

  @Override
  public String marshall(JsonRpcResponse response) {
    JsonValue id = getId(response);
    JsonValue result = getResult(response);
    JsonValue error = getError(response);

    return getResponse(id, result, error).toJson();
  }

  @Override
  public String marshall(JsonRpcRequest request) {
    JsonValue id = getId(request);
    JsonValue method = getMethod(request);
    JsonValue jsonParams = getParams(request);

    return getRequest(id, method, jsonParams).toJson();
  }

  private JsonObject getRequest(JsonValue id, JsonValue method, JsonValue jsonParams) {
    JsonObject jsonRequest = jsonFactory.createObject();
    jsonRequest.put("jsonrpc", "2.0");
    jsonRequest.put("method", method);

    if (id != null) {
      jsonRequest.put("id", id);
    }

    if (jsonParams != null) {
      jsonRequest.put("params", jsonParams);
    }
    return jsonRequest;
  }

  private JsonValue getParams(JsonRpcRequest request) {
    if (!request.hasParams()) {
      return null;
    }

    JsonRpcParams params = request.getParams();
    return params.isSingle() ? getParam(params.getOne()) : getParams(params.getMany());
  }

  private JsonValue getId(JsonRpcRequest request) {
    return request.hasId() ? jsonFactory.create(request.getId()) : null;
  }

  private JsonString getMethod(JsonRpcRequest request) {
    return jsonFactory.create(request.getMethod());
  }

  private JsonObject getResponse(JsonValue jsonId, JsonValue jsonResult, JsonValue jsonError) {
    JsonObject jsonResponse = jsonFactory.createObject();
    jsonResponse.put("jsonrpc", "2.0");
    if (jsonId != null) {
      jsonResponse.put("id", jsonId);
    }

    if (jsonResult != null) {
      jsonResponse.put("result", jsonResult);
    } else {
      jsonResponse.put("error", jsonError);
    }
    return jsonResponse;
  }

  private JsonObject getError(JsonRpcResponse response) {
    if (!response.hasError()) {
      return null;
    }

    JsonObject jsonError = jsonFactory.createObject();
    JsonRpcError error = response.getError();
    jsonError.put("code", error.getCode());
    jsonError.put("message", error.getMessage());
    return jsonError;
  }

  private JsonValue getResult(JsonRpcResponse response) {
    JsonValue jsonResult;
    if (response.hasResult()) {
      JsonRpcResult result = response.getResult();
      jsonResult = result.isSingle() ? getParam(result.getOne()) : getParams(result.getMany());
    } else {
      jsonResult = null;
    }
    return jsonResult;
  }

  private JsonValue getId(JsonRpcResponse response) {
    return response.hasId() ? jsonFactory.parse(response.getId()) : null;
  }

  private JsonValue getParam(Object param) {
    JsonValue jsonValue = getJsonValue(param);
    if (OBJECT.equals(jsonValue.getType())) {
      return jsonValue;
    }

    JsonArray array = jsonFactory.createArray();
    array.set(0, jsonValue);
    return array;
  }

  private JsonValue getParams(List<?> params) {
    JsonArray jsonArray = jsonFactory.createArray();
    for (int i = 0; i < params.size(); i++) {
      Object param = params.get(i);
      JsonValue jsonValue = getJsonValue(param);
      jsonArray.set(i, jsonValue);
    }
    return jsonArray;
  }

  private JsonValue getJsonValue(Object param) {
    if (param == null) {
      return jsonFactory.createNull();
    }
    if (param instanceof JsonValue) {
      return cast(param);
    }
    if (param instanceof String) {
      return jsonFactory.create((String) param);
    }
    if (param instanceof Boolean) {
      return jsonFactory.create((Boolean) param);
    }
    if (param instanceof Double) {
      return jsonFactory.create((Double) param);
    }
    return jsonFactory.parse(dtoFactory.toJson(param));
  }
}
