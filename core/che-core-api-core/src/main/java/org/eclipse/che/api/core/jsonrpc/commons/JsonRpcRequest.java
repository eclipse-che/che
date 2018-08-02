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
package org.eclipse.che.api.core.jsonrpc.commons;

/** Represents JSON RPC request object */
public class JsonRpcRequest {
  private final String id;
  private final String method;
  private final JsonRpcParams params;

  public JsonRpcRequest(String id, String method, JsonRpcParams params) {
    this.id = id;
    this.method = method;
    this.params = params;
  }

  public boolean hasParams() {
    return params != null;
  }

  public boolean hasId() {
    return id != null;
  }

  public String getMethod() {
    return method;
  }

  public String getId() {
    return id;
  }

  public JsonRpcParams getParams() {
    return params;
  }
}
