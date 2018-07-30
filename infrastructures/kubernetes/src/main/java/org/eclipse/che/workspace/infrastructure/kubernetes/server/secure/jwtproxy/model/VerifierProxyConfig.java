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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VerifierProxyConfig {
  @JsonProperty("listen_addr")
  private Integer listenAddr;
  @JsonProperty("verifier")
  private VerifierConfig verifierConfig;

  public Integer getListenAddr() {
    return listenAddr;
  }

  public void setListenAddr(Integer listenAddr) {
    this.listenAddr = listenAddr;
  }

  public VerifierConfig getVerifierConfig() {
    return verifierConfig;
  }

  public void setVerifierConfig(
      VerifierConfig verifierConfig) {
    this.verifierConfig = verifierConfig;
  }
}
