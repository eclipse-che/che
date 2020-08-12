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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.GatewayRouteConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.CheNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.TraefikGatewayRouteConfigGenerator;

@Singleton
public class GatewayRouterProvisioner {

  protected static final Map<String, String> GATEWAY_CONFIGMAP_LABELS =
      ImmutableMap.<String, String>builder()
          .put("app", "che")
          .put("role", "gateway-config")
          .build();

  private final CheNamespace cheNamespace;

  @Inject
  public GatewayRouterProvisioner(CheNamespace cheNamespace) {
    this.cheNamespace = cheNamespace;
  }

  public List<ConfigMap> provision(RuntimeIdentity id, InternalEnvironment internalEnvironment)
      throws InfrastructureException {
    if (internalEnvironment.getGatewayRouteConfigs().isEmpty()) {
      return Collections.emptyList();
    }
    List<ConfigMap> routeConfigMaps = new ArrayList<>();

    for (GatewayRouteConfig routeConfig : internalEnvironment.getGatewayRouteConfigs()) {
      GatewayRouteConfigGenerator gatewayRouteConfigGenerator =
          new TraefikGatewayRouteConfigGenerator(id.getInfrastructureNamespace());
      gatewayRouteConfigGenerator.addRouteConfig(routeConfig);
      ConfigMapBuilder configMapBuilder =
          new ConfigMapBuilder()
              .withNewMetadata()
              .withName(id.getWorkspaceId() + routeConfig.getName())
              .withLabels(GATEWAY_CONFIGMAP_LABELS)
              .withAnnotations(routeConfig.getAnnotations())
              .endMetadata()
              .withData(gatewayRouteConfigGenerator.generate());

      ConfigMap routeConfigMap =
          cheNamespace.createConfigMap(configMapBuilder.build(), id.getWorkspaceId());
      //      ConfigMap routeConfigMap = cheNamespace.configMaps().create(configMapBuilder.build());
      routeConfigMaps.add(routeConfigMap);
    }

    return routeConfigMaps;
  }
}
