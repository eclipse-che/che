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
