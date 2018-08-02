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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class JWTProxy {
  @JsonProperty("verifier_proxies")
  private List<VerifierProxyConfig> verifiedProxyConfigs;

  @JsonProperty("signer_proxy")
  private SignerProxy signerProxy;

  public List<VerifierProxyConfig> getVerifiedProxyConfigs() {
    return verifiedProxyConfigs;
  }

  public void setVerifiedProxyConfigs(List<VerifierProxyConfig> verifiedProxyConfigs) {
    this.verifiedProxyConfigs = verifiedProxyConfigs;
  }

  public JWTProxy withVerifiedProxyConfigs(List<VerifierProxyConfig> verifiedProxyConfigs) {
    this.verifiedProxyConfigs = verifiedProxyConfigs;
    return this;
  }

  public SignerProxy getSignerProxy() {
    return signerProxy;
  }

  public void setSignerProxy(SignerProxy signerProxy) {
    this.signerProxy = signerProxy;
  }

  public JWTProxy withSignerProxy(SignerProxy signerProxy) {
    this.signerProxy = signerProxy;
    return this;
  }
}
