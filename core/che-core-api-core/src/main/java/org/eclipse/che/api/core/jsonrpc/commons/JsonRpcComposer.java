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

import java.util.List;

/**
 * Composes objects or list of objects out of JSON RPC entities (like params or results). The
 * resulting objects can be of the following types: {@link String}, {@link Boolean}, {@link Double}
 * or DTO.
 */
public interface JsonRpcComposer {
  /**
   * Composes an object out of JSON RPC params entity. The resulting object can be of the following
   * types: {@link String}, {@link Boolean}, {@link Double} or DTO.
   *
   * @param params JSON RPC params
   * @param type type of resulting object
   * @param <T> generic type of resulting object
   * @return object of class T
   */
  <T> T composeOne(JsonRpcParams params, Class<T> type);

  /**
   * Composes a list of objects out of JSON RPC params entity. The resulting objects can be of the
   * following types: {@link String}, {@link Boolean}, {@link Double} or DTO.
   *
   * @param params JSON RPC params
   * @param type type of resulting objects
   * @param <T> generic type of resulting objects
   * @return list of objects of class T
   */
  <T> List<T> composeMany(JsonRpcParams params, Class<T> type);

  /**
   * Composes an object out of JSON RPC result entity. The resulting object can be of the following
   * types: {@link String}, {@link Boolean}, {@link Double} or DTO.
   *
   * @param result JSON RPC result
   * @param type type of resulting object
   * @param <T> generic type of resulting object
   * @return object of class T
   */
  <T> T composeOne(JsonRpcResult result, Class<T> type);

  /**
   * Composes a list of objects out of JSON RPC result entity. The resulting objects can be of the
   * following types: {@link String}, {@link Boolean}, {@link Double} or DTO.
   *
   * @param result JSON RPC params
   * @param type type of resulting objects
   * @param <T> generic type of resulting objects
   * @return list of objects of class T
   */
  <T> List<T> composeMany(JsonRpcResult result, Class<T> type);
}
