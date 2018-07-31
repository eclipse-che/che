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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.JWT_PROXY_CONFIG_FILE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.JWT_PROXY_CONFIG_FOLDER;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.JWT_PROXY_PUBLIC_KEY_FILE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model.Config;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model.JWTProxy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model.RegistrableComponentConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model.SignerProxy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model.VerifierConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model.VerifierProxyConfig;

/**
 * Helps to build JWTProxy config with several verifier proxies.
 *
 * @author Sergii Leshchenko
 */
public class JwtProxyConfigBuilder {
  private final List<VerifierProxy> verifierProxies = new ArrayList<>();
  private final String workspaceId;
  private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());

  public JwtProxyConfigBuilder(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public void addVerifierProxy(Integer listenPort, String upstream, Set<String> excludes) {
    verifierProxies.add(new VerifierProxy(listenPort, upstream, excludes));
  }

  public String build() {
    Config config = new Config();
    JWTProxy jwtProxy = new JWTProxy();
    config.setJwtProxy(jwtProxy);

    jwtProxy.setSignerProxy(new SignerProxy());
    jwtProxy.getSignerProxy().setEnabled(false);

    List<VerifierProxyConfig> proxyConfigs = new ArrayList<>();
    jwtProxy.setVerifiedProxyConfigs(proxyConfigs);
    for (VerifierProxy verifierProxy : verifierProxies) {
      VerifierProxyConfig proxyConfig = new VerifierProxyConfig();

      proxyConfig.setListenAddr(verifierProxy.listenPort);

      VerifierConfig verifierConfig = new VerifierConfig();

      verifierConfig.setUpstream(verifierProxy.upstream);
      verifierConfig.setAudience(workspaceId);
      verifierConfig.setMaxSkew("1m");
      verifierConfig.setMaxTtl("8800h");

      Map<String,String> keyServerOptions = new HashMap<>();
      keyServerOptions.put("issuer", "wsmaster");
      keyServerOptions.put("key_id", workspaceId);
      keyServerOptions.put("public_key_path", JWT_PROXY_CONFIG_FOLDER + '/' + JWT_PROXY_PUBLIC_KEY_FILE);
      verifierConfig.setKeyServer(new RegistrableComponentConfig().withType("preshared").withOptions(keyServerOptions));

      verifierConfig.setClaimsVerifier(new RegistrableComponentConfig().withType("static").withOptions(Collections
          .singletonMap("iss", "wsmaster")));

      verifierConfig.setNonceStorage(new RegistrableComponentConfig().withType("void"));

      if (!verifierProxy.excludes.isEmpty()) {
        verifierConfig.setExcludes(verifierProxy.excludes);
      }
      proxyConfig.setVerifierConfig(verifierConfig);

      proxyConfigs.add(proxyConfig);
    }
      try {
        return YAML_PARSER.writeValueAsString(config);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error during creation of JWTProxy config YAML: ", e);
      }
    }

  private class VerifierProxy {
    private Integer listenPort;
    private String upstream;
    private Set<String> excludes;

    VerifierProxy(Integer listenPort, String upstream, Set<String> excludes) {
      this.listenPort = listenPort;
      this.upstream = upstream;
      this.excludes = excludes;
    }
  }
}
