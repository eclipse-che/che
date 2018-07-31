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
package org.eclipse.che.ide.jsonrpc;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static elemental.json.JsonType.ARRAY;
import static elemental.json.JsonType.BOOLEAN;
import static elemental.json.JsonType.NUMBER;
import static elemental.json.JsonType.STRING;
import static java.util.Collections.singletonList;

import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcParams;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcRequest;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcResponse;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcResult;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcUnmarshaller;

@Singleton
public class ElementalJsonRpcUnmarshaller implements JsonRpcUnmarshaller {
  private final JsonFactory jsonFactory;

  @Inject
  public ElementalJsonRpcUnmarshaller(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  @Override
  public List<String> unmarshalArray(String message) {
    return getArray(message, jsonFactory.parse(message));
  }

  @Override
  public JsonRpcRequest unmarshalRequest(String message) {
    checkNotNull(message, "Message must not be null");
    checkArgument(!message.isEmpty(), "Message must not be empty");

    JsonObject jsonObject = jsonFactory.parse(message);

    String id = getId(jsonObject);
    String method = getMethod(jsonObject);
    JsonRpcParams params = getParams(jsonObject);

    return new JsonRpcRequest(id, method, params);
  }

  @Override
  public JsonRpcResponse unmarshalResponse(String message) {
    checkNotNull(message, "Message must not be null");
    checkArgument(!message.isEmpty(), "Message must not be empty");

    JsonObject response = jsonFactory.parse(message);

    String id = getId(response);
    JsonRpcResult result = getResult(response);
    JsonRpcError error = getError(response);

    return new JsonRpcResponse(id, result, error);
  }

  private JsonRpcError getError(JsonObject response) {
    if (response.hasKey("error")) {
      JsonObject errorJsonValue = response.get("error");
      int code = ((Double) errorJsonValue.get("code").asNumber()).intValue();
      String errorMessage = errorJsonValue.get("message").asString();
      return new JsonRpcError(code, errorMessage);
    } else {
      return null;
    }
  }

  private JsonRpcResult getResult(JsonObject response) {
    if (!response.hasKey("result")) {
      return null;
    }

    JsonValue jsonValue = response.get("result");
    if (!jsonValue.getType().equals(ARRAY)) {
      return new JsonRpcResult(getInnerItem(jsonValue));
    }
    JsonArray jsonArray = (JsonArray) jsonValue;
    int size = jsonArray.length();
    List<Object> innerResults = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      JsonValue innerJsonElement = jsonArray.get(i);
      Object innerItem = getInnerItem(innerJsonElement);
      innerResults.add(innerItem);
    }

    return new JsonRpcResult(innerResults);
  }

  private JsonRpcParams getParams(JsonObject jsonObject) {
    if (!jsonObject.hasKey("params")) {
      return null;
    }

    JsonValue jsonValue = jsonObject.get("params");
    if (!jsonValue.getType().equals(ARRAY)) {
      return new JsonRpcParams(getInnerItem(jsonValue));
    }

    JsonArray jsonArray = (JsonArray) jsonValue;
    int size = jsonArray.length();
    List<Object> innerParameters = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      JsonValue innerJsonElement = jsonArray.get(i);
      Object innerItem = getInnerItem(innerJsonElement);
      innerParameters.add(innerItem);
    }

    return new JsonRpcParams(innerParameters);
  }

  private String getId(JsonObject jsonObject) {
    return !jsonObject.hasKey("id") ? null : jsonObject.get("id").asString();
  }

  private String getMethod(JsonObject jsonObject) {
    return jsonObject.get("method").asString();
  }

  private Object getInnerItem(JsonValue jsonElement) {
    JsonType type = jsonElement.getType();
    if (NUMBER.equals(type)) {
      return null;
    } else if (NUMBER.equals(type)) {
      return jsonElement.asNumber();
    } else if (STRING.equals(type)) {
      return jsonElement.asString();
    } else if (BOOLEAN.equals(type)) {
      return jsonElement.asBoolean();
    } else {
      return jsonElement;
    }
  }

  private List<String> getArray(String message, JsonValue jsonValue) {
    if (!ARRAY.equals(jsonValue.getType())) {
      return singletonList(message);
    }

    JsonArray jsonArray = jsonFactory.parse(message);
    int size = jsonArray.length();
    List<String> result = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      JsonValue jsonElement = jsonArray.get(i);
      result.add(jsonElement.toString());
    }

    return result;
  }
}
