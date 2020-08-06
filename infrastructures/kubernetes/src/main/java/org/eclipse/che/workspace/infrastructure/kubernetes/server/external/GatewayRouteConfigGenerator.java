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
import org.eclipse.che.api.workspace.server.spi.environment.GatewayRouteConfig;

/**
 * Generates config for single external server that we want to expose in the Gateway.
 *
 * <p>Implementation provides configuration for specific Gateway technology (e.g., Traefik).
 */
public interface GatewayRouteConfigGenerator {
  void addRouteConfig(GatewayRouteConfig routeConfig);

  /**
   * Generates content of configuration for service, defined by passed parameters, that should be
   * exposed by the Gateway. Returned {@code Map<String, String>} will be used as a value of
   * ConfigMap.
   *
   * <p>Implementation must ensure that Gateway configured with returned content will route the
   * requests on {@code path} into {@code serviceUrl}. Also it must strip {@code path} from request
   * url.
   *
   * @param name name of the service
   * @param serviceUrl url of service we want to route to
   * @param path path to route and strip
   * @return full content of configuration for the service
   */
  Map<String, String> generate();
}
