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

import static java.util.Collections.singletonList;

import java.util.List;

/** Represents JSON RPC params object */
public class JsonRpcParams {
  private List<?> params;
  private boolean single;

  public JsonRpcParams(Object params) {
    this.params = singletonList(params);
    this.single = true;
  }

  public JsonRpcParams(List<?> params) {
    this.params = params;
    this.single = false;
  }

  public boolean isSingle() {
    return single;
  }

  public List<?> getMany() {
    return params;
  }

  public Object getOne() {
    return params.get(0);
  }
}
