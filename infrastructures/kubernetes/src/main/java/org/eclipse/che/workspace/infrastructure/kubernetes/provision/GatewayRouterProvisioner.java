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

import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_PORT_ATTRIBUTE;

import io.fabric8.kubernetes.api.model.ConfigMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGeneratorFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.GatewayConfigmapLabels;

/**
 * This provisioner finds {@link ConfigMap}s, that configures the single-host Gateway, generates
 * Gateway configuration and puts it into their data.
 *
 * <p>It uses {@link GatewayRouteConfigGenerator} to generate the gateway configuration.
 */
public class GatewayRouterProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  private final GatewayRouteConfigGeneratorFactory configGeneratorFactory;
  private final GatewayConfigmapLabels configmapLabels;

  @Inject
  public GatewayRouterProvisioner(
      GatewayRouteConfigGeneratorFactory configGeneratorFactory,
      GatewayConfigmapLabels configmapLabels) {
    this.configGeneratorFactory = configGeneratorFactory;
    this.configmapLabels = configmapLabels;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    for (Entry<String, ConfigMap> configMapEntry : k8sEnv.getConfigMaps().entrySet()) {
      if (configmapLabels.isGatewayConfig(configMapEntry.getValue())) {
        ConfigMap gatewayConfigMap = configMapEntry.getValue();

        Map<String, ServerConfigImpl> servers =
            new Annotations.Deserializer(gatewayConfigMap.getMetadata().getAnnotations()).servers();
        if (servers.size() != 1) {
          throw new InfrastructureException(
              "Expected exactly 1 server in gateway config ConfigMap's '"
                  + gatewayConfigMap.getMetadata().getName()
                  + "' annotations. This is a bug, please report.");
        }
        Entry<String, ServerConfigImpl> serverConfigEntry = servers.entrySet().iterator().next();
        ServerConfigImpl server = serverConfigEntry.getValue();

        if (!server.getAttributes().containsKey(SERVICE_NAME_ATTRIBUTE)
            || !server.getAttributes().containsKey(SERVICE_PORT_ATTRIBUTE)) {
          throw new InfrastructureException(
              "Expected `serviceName` and `servicePort` in gateway config ServerConfig attributes for gateway config Configmap '"
                  + gatewayConfigMap.getMetadata().getName()
                  + "'. This is a bug, please report.");
        }

        // We're now creating only 1 gateway route configuration per ConfigMap, so we need to create
        // generator in each loop iteration.
        GatewayRouteConfigGenerator gatewayRouteConfigGenerator = configGeneratorFactory.create();
        gatewayRouteConfigGenerator.addRouteConfig(configMapEntry.getKey(), gatewayConfigMap);

        Map<String, String> gatewayConfiguration =
            gatewayRouteConfigGenerator.generate(identity.getInfrastructureNamespace());
        gatewayConfigMap.setData(gatewayConfiguration);

        // Configuration is now generated, so remove these internal attributes
        server.getAttributes().remove(SERVICE_NAME_ATTRIBUTE);
        server.getAttributes().remove(SERVICE_PORT_ATTRIBUTE);
        gatewayConfigMap
            .getMetadata()
            .getAnnotations()
            .putAll(
                new Annotations.Serializer()
                    .server(serverConfigEntry.getKey(), server)
                    .annotations());
      }
    }
  }
}
