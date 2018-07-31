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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Add proxy configuration to pod containers
 *
 * @author Mykhailo Kuznietsov
 */
public class ProxySettingsProvisioner implements ConfigurationProvisioner {
  private static final String HTTPS_PROXY = "https_proxy";
  private static final String HTTP_PROXY = "http_proxy";
  private static final String NO_PROXY = "no_proxy";

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
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (!proxyEnvVars.isEmpty()) {
      for (Pod pod : k8sEnv.getPods().values()) {
        pod.getSpec()
            .getContainers()
            .forEach(
                container ->
                    proxyEnvVars.forEach((k, v) -> container.getEnv().add(new EnvVar(k, v, null))));
      }
    }
  }
}
