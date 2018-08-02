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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.JWT_PROXY_CONFIG_FOLDER;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.JWT_PROXY_PUBLIC_KEY_FILE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  private static final ObjectMapper YAML_PARSER =
      new ObjectMapper(new YAMLFactory())
          .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

  public JwtProxyConfigBuilder(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public void addVerifierProxy(Integer listenPort, String upstream, Set<String> excludes) {
    verifierProxies.add(new VerifierProxy(listenPort, upstream, excludes));
  }

  public String build() {
    List<VerifierProxyConfig> proxyConfigs = new ArrayList<>();
    Config config =
        new Config()
            .withJWTProxy(
                new JWTProxy()
                    .withSignerProxy(new SignerProxy().withEnabled(false))
                    .withVerifiedProxyConfigs(proxyConfigs));

    for (VerifierProxy verifierProxy : verifierProxies) {
      VerifierConfig verifierConfig =
          new VerifierConfig()
              .withAudience(workspaceId)
              .withUpstream(verifierProxy.upstream)
              .withMaxSkew("1m")
              .withMaxTtl("8800h")
              .withKeyServer(
                  new RegistrableComponentConfig()
                      .withType("preshared")
                      .withOptions(
                          ImmutableMap.of(
                              "issuer",
                              "wsmaster",
                              "key_id",
                              workspaceId,
                              "public_key_path",
                              JWT_PROXY_CONFIG_FOLDER + '/' + JWT_PROXY_PUBLIC_KEY_FILE)))
              .withClaimsVerifier(
                  Collections.singleton(
                      new RegistrableComponentConfig()
                          .withType("static")
                          .withOptions(ImmutableMap.of("iss", "wsmaster"))))
              .withNonceStorage(new RegistrableComponentConfig().withType("void"));

      if (!verifierProxy.excludes.isEmpty()) {
        verifierConfig.setExcludes(verifierProxy.excludes);
      }

      VerifierProxyConfig proxyConfig =
          new VerifierProxyConfig()
              .withListenAddr(":" + verifierProxy.listenPort)
              .withVerifierConfig(verifierConfig);

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
