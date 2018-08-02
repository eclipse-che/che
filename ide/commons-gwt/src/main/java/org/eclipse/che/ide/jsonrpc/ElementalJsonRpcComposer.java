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
package org.eclipse.che.ide.jsonrpc;

import static java.util.Collections.emptyList;
import static org.eclipse.che.api.core.jsonrpc.commons.JsonRpcUtils.cast;

import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonValue;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcComposer;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcParams;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcResult;
import org.eclipse.che.ide.dto.DtoFactory;

@Singleton
public class ElementalJsonRpcComposer implements JsonRpcComposer {
  private final JsonFactory jsonFactory;
  private final DtoFactory dtoFactory;

  @Inject
  public ElementalJsonRpcComposer(JsonFactory jsonFactory, DtoFactory dtoFactory) {
    this.jsonFactory = jsonFactory;
    this.dtoFactory = dtoFactory;
  }

  @Override
  public <T> T composeOne(JsonRpcParams params, Class<T> type) {
    return composeOne(type, params.getOne());
  }

  @Override
  public <T> List<T> composeMany(JsonRpcParams params, Class<T> type) {
    return composeMany(type, params.getMany());
  }

  @Override
  public <T> T composeOne(JsonRpcResult params, Class<T> type) {
    return composeOne(type, params.getOne());
  }

  @Override
  public <T> List<T> composeMany(JsonRpcResult result, Class<T> type) {
    return composeMany(type, result.getMany());
  }

  private <T> T composeOne(Class<T> type, Object paramObject) {
    if (paramObject instanceof JsonValue) {
      JsonValue jsonValue = (JsonValue) paramObject;
      return dtoFactory.createDtoFromJson(jsonValue.toJson(), type);
    }

    return cast(paramObject);
  }

  private <T> List<T> composeMany(Class<T> type, List<?> paramsList) {
    if (paramsList.isEmpty()) {
      return emptyList();
    }

    if (paramsList.get(0) instanceof JsonValue) {
      JsonArray jsonArray = jsonFactory.createArray();
      for (int i = 0; i < paramsList.size(); i++) {
        jsonArray.set(i, (JsonValue) paramsList.get(i));
      }
      return dtoFactory.createListDtoFromJson(jsonArray.toJson(), type);
    }

    return cast(paramsList);
  }
}
