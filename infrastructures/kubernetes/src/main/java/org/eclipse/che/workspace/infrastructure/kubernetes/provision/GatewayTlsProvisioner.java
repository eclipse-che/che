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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.TlsProvisioner.getSecureProtocol;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.environment.GatewayRouteConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Enables Transport Layer Security (TLS) for external server implemented with gateway ConfigMaps.
 */
@Singleton
public class GatewayTlsProvisioner<T extends KubernetesEnvironment>
    implements ConfigurationProvisioner<T>, TlsProvisioner<T> {

  final boolean isTlsEnabled;

  @Inject
  public GatewayTlsProvisioner(@Named("che.infra.kubernetes.tls_enabled") boolean isTlsEnabled) {
    this.isTlsEnabled = isTlsEnabled;
  }

  @Override
  public void provision(T k8sEnv, RuntimeIdentity identity)
      throws KubernetesInfrastructureException {
    if (!isTlsEnabled) {
      return;
    }

    for (GatewayRouteConfig routeConfig : k8sEnv.getGatewayRouteConfigs()) {
      useSecureProtocolForGatewayServers(routeConfig);
    }
  }

  private void useSecureProtocolForGatewayServers(GatewayRouteConfig routeConfig) {
    Map<String, ServerConfigImpl> servers =
        Annotations.newDeserializer(routeConfig.getAnnotations()).servers();

    if (servers.isEmpty()) {
      return;
    }

    servers.values().forEach(s -> s.setProtocol(getSecureProtocol(s.getProtocol())));

    Map<String, String> annotations = Annotations.newSerializer().servers(servers).annotations();
    if (!annotations.isEmpty() && routeConfig.getAnnotations() != null) {
      routeConfig.getAnnotations().putAll(annotations);
    }
  }
}
