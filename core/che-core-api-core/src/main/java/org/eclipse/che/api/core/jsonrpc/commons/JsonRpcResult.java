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

import static java.util.Collections.singletonList;

import java.util.List;

/** Represents JSON RPC result object */
public class JsonRpcResult {
  private List<?> result;
  private boolean single;

  public JsonRpcResult(Object result) {
    this.result = singletonList(result);
    this.single = true;
  }

  public JsonRpcResult(List<?> result) {
    this.result = result;
    this.single = false;
  }

  public boolean isSingle() {
    return single;
  }

  public List<?> getMany() {
    return result;
  }

  public Object getOne() {
    return result.get(0);
  }
}
