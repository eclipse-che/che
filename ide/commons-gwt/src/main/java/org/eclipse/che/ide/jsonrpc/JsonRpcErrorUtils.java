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

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.promises.client.PromiseError;

public class JsonRpcErrorUtils {

  private JsonRpcErrorUtils() {}

  /** Transform {@link JsonRpcError} to {@link PromiseError}. */
  public static PromiseError getPromiseError(JsonRpcError jsonRpcError) {
    return new PromiseError() {
      @Override
      public String getMessage() {
        return jsonRpcError.getMessage();
      }

      @Override
      public Throwable getCause() {
        return new JsonRpcException(jsonRpcError.getCode(), jsonRpcError.getMessage());
      }
    };
  }
}
