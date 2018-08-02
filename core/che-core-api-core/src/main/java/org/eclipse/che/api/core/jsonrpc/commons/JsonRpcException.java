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

/**
 * Specific to JSON RPC exception that is to be raised when any JSON RPC related error is met.
 * According to the spec there should be an error code and an error message.
 */
public class JsonRpcException extends RuntimeException {
  private final int code;
  private final String id;

  public JsonRpcException(int code, String message) {
    this(code, message, null);
  }

  JsonRpcException(int code, String message, String id) {
    super(message);
    this.code = code;
    this.id = id;
  }

  public int getCode() {
    return code;
  }

  public String getId() {
    return id;
  }
}
