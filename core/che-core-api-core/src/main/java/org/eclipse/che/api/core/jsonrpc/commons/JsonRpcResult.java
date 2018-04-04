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
