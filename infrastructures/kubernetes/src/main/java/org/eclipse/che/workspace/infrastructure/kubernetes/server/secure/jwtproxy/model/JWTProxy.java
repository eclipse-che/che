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
import java.util.List;

public class JWTProxy {
  @JsonProperty("verified_proxies")
  private List<VerifierProxyConfig> verifiedProxyConfigs;

  @JsonProperty("signer_proxy")
  private SignerProxy signerProxy;

  public List<VerifierProxyConfig> getVerifiedProxyConfigs() {
    return verifiedProxyConfigs;
  }

  public void setVerifiedProxyConfigs(List<VerifierProxyConfig> verifiedProxyConfigs) {
    this.verifiedProxyConfigs = verifiedProxyConfigs;
  }

  public SignerProxy getSignerProxy() {
    return signerProxy;
  }

  public void setSignerProxy(SignerProxy signerProxy) {
    this.signerProxy = signerProxy;
  }
}
