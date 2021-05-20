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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import io.fabric8.kubernetes.api.model.ConfigMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/**
 * Generates config for external servers that we want to expose in the Gateway.
 *
 * <p>Implementation provides configuration for specific Gateway technology (e.g., Traefik).
 */
public interface GatewayRouteConfigGenerator {

  /**
   * Add prepared {@link ConfigMap},that will hold gateway route configuration, to the generator. So
   * it can be generated later with {@link GatewayRouteConfigGenerator#generate(String)}.
   *
   * <p>Provided {@link ConfigMap} must be annotated with {@link
   * org.eclipse.che.api.core.model.workspace.config.ServerConfig} annotations. It's responsibility
   * of the caller to ensure that. The {@link GatewayRouteConfigGenerator} fails on {@link
   * GatewayRouteConfigGenerator#generate(String)} when invalid {@link ConfigMap}s are added to it.
   *
   * @param routeConfig config to add
   */
  void addRouteConfig(String name, ConfigMap routeConfig) throws InfrastructureException;

  /**
   * Generates content of configurations for services, defined earlier by added {@link
   * GatewayRouteConfigGenerator#addRouteConfig(String, ConfigMap)}. Returned {@code Map<String,
   * String>} must be ready to be used as a {@link ConfigMap}'s data, which is further injected into
   * Gateway pod.
   *
   * <p>Implementation must ensure that Gateway configured with returned content will route the
   * requests on {@code path} into {@code serviceUrl}. Also it must strip {@code path} from request
   * url.
   *
   * <p>Returned Map's Keys will be used as file names, Values as their content. e.g.:
   *
   * <pre>
   *   service1.yml: {config-content-for-service-1}
   *   service2.yml: {config-content-for-service-2}
   * </pre>
   *
   * @return full content of configuration for the services
   */
  Map<String, String> generate(String namespace) throws InfrastructureException;
}
