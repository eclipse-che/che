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

import elemental.json.JsonException;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcQualifier;

@Singleton
public class ElementalJsonRpcQualifier implements JsonRpcQualifier {
  private final JsonFactory jsonFactory;

  @Inject
  public ElementalJsonRpcQualifier(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  @Override
  public boolean isValidJson(String message) {
    try {
      jsonFactory.parse(message);
      return true;
    } catch (JsonException e) {
      return false;
    }
  }

  @Override
  public boolean isJsonRpcRequest(String message) {
    JsonObject jsonObject = jsonFactory.parse(message);
    return jsonObject.hasKey("method");
  }

  @Override
  public boolean isJsonRpcResponse(String message) {
    return !isJsonRpcRequest(message);
  }
}
