/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.JWT_PROXY_POD_NAME;

import io.fabric8.kubernetes.api.model.EnvVar;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Add proxy configuration to pod containers
 *
 * @author Mykhailo Kuznietsov
 */
public class ProxySettingsProvisioner implements ConfigurationProvisioner {
  static final String HTTPS_PROXY = "https_proxy";
  static final String HTTP_PROXY = "http_proxy";
  static final String NO_PROXY = "no_proxy";

  private final Map<String, String> proxyEnvVars;

  @Inject
  public ProxySettingsProvisioner(
      @Named("che.workspace.https_proxy") String httpsProxy,
      @Named("che.workspace.http_proxy") String httpProxy,
      @Named("che.workspace.no_proxy") String noProxy) {
    proxyEnvVars = new HashMap<>();
    if (!httpsProxy.isEmpty()) {
      proxyEnvVars.put(HTTPS_PROXY, httpsProxy);
    }
    if (!httpProxy.isEmpty()) {
      proxyEnvVars.put(HTTP_PROXY, httpProxy);
    }
    if (!noProxy.isEmpty()) {
      proxyEnvVars.put(NO_PROXY, noProxy);
    }
  }

  @Override
  @Traced
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    TracingTags.WORKSPACE_ID.set(identity::getWorkspaceId);

    if (!proxyEnvVars.isEmpty()) {
      k8sEnv
          .getPodsData()
          .entrySet()
          .stream()
          // JWTProxy container doesn't need proxy settings since it never does any outbound
          // requests, and setting of it may fail accessing internal addresses.
          .filter(entry -> !entry.getKey().equals(JWT_PROXY_POD_NAME))
          .flatMap(
              entry ->
                  Stream.concat(
                      entry.getValue().getSpec().getContainers().stream(),
                      entry.getValue().getSpec().getInitContainers().stream()))
          .forEach(
              container ->
                  proxyEnvVars.forEach((k, v) -> container.getEnv().add(new EnvVar(k, v, null))));
    }
  }
}
