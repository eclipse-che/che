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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ServiceExposureStrategyProvider.STRATEGY_PROPERTY;

import com.google.common.base.Strings;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.inject.ConfigurationException;

/**
 * Provides a host-based strategy for exposing service ports outside the cluster using Ingress
 *
 * <p>This strategy uses different Ingress host entries <br>
 * Each external server is exposed with a unique subdomain of CHE_DOMAIN.
 *
 * <pre>
 *   Host-Based Ingress exposing service's port:
 * Ingress
 * ...
 * spec:
 *   rules:
 *     - host: service123-webapp.che-domain   ---->> Service.metadata.name + - + Service.spec.ports[0].name + . + CHE_DOMAIN
 *     - http:
 *         paths:
 *           - path: /
 *             backend:
 *               serviceName: service123      ---->> Service.metadata.name
 *               servicePort: [8080|web-app]  ---->> Service.spec.ports[0].[port|name]
 * </pre>
 *
 * @author Sergii Leshchenko
 * @author Guy Daich
 */
public class MultiHostExternalServiceExposureStrategy implements ExternalServiceExposureStrategy {

  public static final String MULTI_HOST_STRATEGY = "multi-host";
  protected static final String INGRESS_DOMAIN_PROPERTY = "che.infra.kubernetes.ingress.domain";

  private final String domain;

  @Inject
  public MultiHostExternalServiceExposureStrategy(
      @Named(INGRESS_DOMAIN_PROPERTY) String domain, @Named(STRATEGY_PROPERTY) String strategy) {
    if (Strings.isNullOrEmpty(domain) && MULTI_HOST_STRATEGY.equals(strategy)) {
      throw new ConfigurationException(
          format(
              "Strategy of generating ingress URLs for Che servers is set to '%s', "
                  + "but property '%s' is not set",
              MULTI_HOST_STRATEGY, INGRESS_DOMAIN_PROPERTY));
    }
    this.domain = domain;
  }

  @Override
  public String getExternalHost(String serviceName, String serverName) {
    return serviceName + "-" + serverName + "." + domain;
  }

  @Override
  public String getExternalPath(String serviceName, String serverName) {
    return "/";
  }
}
