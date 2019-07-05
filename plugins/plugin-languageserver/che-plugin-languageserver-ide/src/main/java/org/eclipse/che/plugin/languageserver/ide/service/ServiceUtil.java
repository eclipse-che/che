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
package org.eclipse.che.plugin.languageserver.ide.service;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.promises.client.PromiseError;

public class ServiceUtil {
  private ServiceUtil() {}

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
