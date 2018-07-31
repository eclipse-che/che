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
package org.eclipse.che.api.core.jsonrpc.commons;

import java.util.List;

/** Transforms plain text messages into JSON RPC structures. */
public interface JsonRpcUnmarshaller {
  /**
   * Creates an array of stringified JSON RPC structures, which can further be unmarshalled
   * separately.
   *
   * @param message incoming message
   * @return array of serialized JSON RPC
   */
  List<String> unmarshalArray(String message);

  /**
   * Creates a request out of a plain text message
   *
   * @param message plain text message
   * @return JSON RPC request entity
   */
  JsonRpcRequest unmarshalRequest(String message);

  /**
   * Creates a response out of a plain text message
   *
   * @param message plain text message
   * @return JSON RPC response entity
   */
  JsonRpcResponse unmarshalResponse(String message);
}
