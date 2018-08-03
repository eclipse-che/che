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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes parent node of jwtproxy config.
 *
 * @author Mykhailo Kuznietsov
 */
public class Config {
  @JsonProperty("jwtproxy")
  private JWTProxy jwtProxy;

  public JWTProxy getJwtProxy() {
    return jwtProxy;
  }

  public void setJwtProxy(JWTProxy jwtProxy) {
    this.jwtProxy = jwtProxy;
  }

  public Config withJWTProxy(JWTProxy jwtProxy) {
    this.jwtProxy = jwtProxy;
    return this;
  }
}
