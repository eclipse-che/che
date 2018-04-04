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
