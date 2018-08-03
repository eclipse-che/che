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
import java.util.List;

/**
 * Describes single signer and multiple verifier proxy configs.
 *
 * @author Mykhailo Kuznietsov
 */
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
