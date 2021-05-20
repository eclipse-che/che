/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import io.fabric8.kubernetes.api.model.ConfigMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.GatewayConfigmapLabels;

/**
 * Enables Transport Layer Security (TLS) for external server implemented with gateway ConfigMaps.
 */
@Singleton
public class GatewayTlsProvisioner<T extends KubernetesEnvironment>
    implements ConfigurationProvisioner<T>, TlsProvisioner<T> {

  private final boolean isTlsEnabled;
  private final GatewayConfigmapLabels configmapLabels;
  private final TlsProvisioner<T> nativeProvisioner;

  @Inject
  public GatewayTlsProvisioner(
      @Named("che.infra.kubernetes.tls_enabled") boolean isTlsEnabled,
      GatewayConfigmapLabels configmapLabels,
      TlsProvisionerProvider<T> provisionerProvider) {
    this.isTlsEnabled = isTlsEnabled;
    this.configmapLabels = configmapLabels;
    this.nativeProvisioner = provisionerProvider.get(WorkspaceExposureType.NATIVE);
  }

  @Override
  public void provision(T k8sEnv, RuntimeIdentity identity) throws InfrastructureException {
    if (!isTlsEnabled) {
      return;
    }

    for (ConfigMap configMap : k8sEnv.getConfigMaps().values()) {
      if (configmapLabels.isGatewayConfig(configMap)) {
        useSecureProtocolForGatewayConfigMap(configMap);
      }
    }

    nativeProvisioner.provision(k8sEnv, identity);
  }

  private void useSecureProtocolForGatewayConfigMap(ConfigMap configMap)
      throws InfrastructureException {
    Map<String, ServerConfigImpl> servers =
        Annotations.newDeserializer(configMap.getMetadata().getAnnotations()).servers();

    if (servers.isEmpty()) {
      return;
    }
    if (servers.size() != 1) {
      throw new InfrastructureException(
          "Expected exactly 1 server in Gateway configuration ConfigMap '"
              + configMap.getMetadata().getName()
              + "'. This is a bug, please report.");
    }
    Entry<String, ServerConfigImpl> serverConfigEntry = servers.entrySet().iterator().next();
    ServerConfigImpl serverConfig = serverConfigEntry.getValue();

    serverConfig.setProtocol(getSecureProtocol(serverConfig.getProtocol()));
    configMap
        .getMetadata()
        .getAnnotations()
        .putAll(
            Annotations.newSerializer()
                .server(serverConfigEntry.getKey(), serverConfig)
                .annotations());
  }
}
