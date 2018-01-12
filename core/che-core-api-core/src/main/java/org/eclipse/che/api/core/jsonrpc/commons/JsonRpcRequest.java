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
