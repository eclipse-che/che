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
 * Is used to check whether Json Rpc method can be invoked. For example, can be used to check
 * permission before method invocation.
 *
 * @author Sergii Leshchenko
 */
public interface JsonRpcMethodInvokerFilter {

  /**
   * Check whether supplied method can be invoked.
   *
   * @param method method name that is going to be invoked
   * @param params actual method parameters
   * @throws JsonRpcException if method can not be invoked cause current environment context, e.g.
   *     for current user, with current request attributes, etc. JsonRpcException should contain the
   *     corresponding message and code.
   */
  void accept(String method, Object... params) throws JsonRpcException;
}
