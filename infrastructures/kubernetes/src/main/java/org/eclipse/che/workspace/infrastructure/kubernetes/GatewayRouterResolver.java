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
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGeneratorFactory;

/**
 * Resolves {@link GatewayRouteConfig}s from {@link InternalEnvironment} into {@link ConfigMap}s.
 * Created instance of {@link ConfigMap} is annotated with {@link
 * GatewayRouterResolver#GATEWAY_CONFIGMAP_LABELS}, which are needed so Configbump tool can pick
 * them up and provide them to the Gateway pod.
 */
@Singleton
public class GatewayRouterResolver {

  protected static final Map<String, String> GATEWAY_CONFIGMAP_LABELS =
      ImmutableMap.<String, String>builder()
          .put("app", "che")
          .put("role", "gateway-config")
          .build();

  private final GatewayRouteConfigGeneratorFactory configGeneratorFactory;

  @Inject
  public GatewayRouterResolver(GatewayRouteConfigGeneratorFactory configGeneratorFactory) {
    this.configGeneratorFactory = configGeneratorFactory;
  }

  public List<ConfigMap> resolve(RuntimeIdentity id, InternalEnvironment internalEnvironment)
      throws InfrastructureException {
    if (internalEnvironment.getGatewayRouteConfigs().isEmpty()) {
      return Collections.emptyList();
    }
    List<ConfigMap> routeConfigMaps = new ArrayList<>();

    for (GatewayRouteConfig routeConfig : internalEnvironment.getGatewayRouteConfigs()) {
      GatewayRouteConfigGenerator gatewayRouteConfigGenerator =
          configGeneratorFactory.create(id.getInfrastructureNamespace());
      gatewayRouteConfigGenerator.addRouteConfig(routeConfig);

      ConfigMapBuilder configMapBuilder =
          new ConfigMapBuilder()
              .withNewMetadata()
              .withName(id.getWorkspaceId() + routeConfig.getName())
              .withLabels(GATEWAY_CONFIGMAP_LABELS)
              .withAnnotations(routeConfig.getAnnotations())
              .endMetadata()
              .withData(gatewayRouteConfigGenerator.generate());

      routeConfigMaps.add(configMapBuilder.build());
    }

    return routeConfigMaps;
  }
}
