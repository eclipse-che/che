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
package org.eclipse.che.workspace.infrastructure.openshift.server;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.ConfigMapServerResolver;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.ServerResolver;

/**
 * Factory that decides by configuration, which {@link ServerResolver} implementation to use in
 * OpenShift environment.
 */
public class OpenShiftServerResolverFactory {

  private final String cheHost;
  private final String exposureStrategy;

  @Inject
  public OpenShiftServerResolverFactory(
      @Named("che.host") String cheHost,
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy) {
    this.cheHost = cheHost;
    this.exposureStrategy = exposureStrategy;
  }

  /**
   * Create {@link ServerResolver} for configured server strategy.
   *
   * <p>TODO: use {@link ConfigMapServerResolver} for gateway based single-host
   *
   * @return {@link ServerResolver} instance
   */
  public ServerResolver create(
      List<Service> services, List<Route> routes, List<ConfigMap> configMaps) {
    return new RouteServerResolver(services, routes);
  }
}
