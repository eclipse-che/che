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
 * Describes configuration of the verifier proxy.
 *
 * @author Mykhailo Kuznietsov
 */
public class VerifierProxyConfig {
  @JsonProperty("listen_addr")
  private String listenAddr;

  @JsonProperty("verifier")
  private VerifierConfig verifierConfig;

  public String getListenAddr() {
    return listenAddr;
  }

  public void setListenAddr(String listenAddr) {
    this.listenAddr = listenAddr;
  }

  public VerifierProxyConfig withListenAddr(String listenAddr) {
    this.listenAddr = listenAddr;
    return this;
  }

  public VerifierConfig getVerifierConfig() {
    return verifierConfig;
  }

  public void setVerifierConfig(VerifierConfig verifierConfig) {
    this.verifierConfig = verifierConfig;
  }

  public VerifierProxyConfig withVerifierConfig(VerifierConfig verifierConfig) {
    this.verifierConfig = verifierConfig;
    return this;
  }
}
