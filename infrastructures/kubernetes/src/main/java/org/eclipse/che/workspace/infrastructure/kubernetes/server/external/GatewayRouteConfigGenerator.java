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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.GatewayRouteConfig;

/**
 * Generates config for external servers that we want to expose in the Gateway.
 *
 * <p>Implementation must accept new {@link GatewayRouteConfig}s with {@link
 * GatewayRouteConfigGenerator#addRouteConfig(GatewayRouteConfig)}. Then call to {@link
 * GatewayRouteConfigGenerator#generate()} must generate gateway specific configuration for all
 * added {@link GatewayRouteConfig}s.
 *
 * <p>Implementation provides configuration for specific Gateway technology (e.g., Traefik).
 */
public interface GatewayRouteConfigGenerator {

  /**
   * Add {@link GatewayRouteConfig} to the generator so it can be generated later with {@link
   * GatewayRouteConfigGenerator#generate()}
   *
   * @param routeConfig config to add
   */
  void addRouteConfig(GatewayRouteConfig routeConfig);

  /**
   * Generates content of configurations for services, defined earlier by added {@link
   * GatewayRouteConfig}s with {@link
   * GatewayRouteConfigGenerator#addRouteConfig(GatewayRouteConfig)}. Returned {@code Map<String,
   * String>} will be used as a value of ConfigMap and injected into Gateway pod.
   *
   * <p>Implementation must ensure that Gateway configured with returned content will route the
   * requests on {@code path} into {@code serviceUrl}. Also it must strip {@code path} from request
   * url.
   *
   * <p>Keys and Values of returned {@link Map} depends on gateway technology. e.g.:
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
