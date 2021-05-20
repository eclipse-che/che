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

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Provides a path-based strategy for exposing service ports outside the cluster using Ingress.
 * Ingresses will be created with a common host name for all workspaces.
 *
 * <p>This strategy uses different Ingress path entries <br>
 * Each external server is exposed with a unique path prefix.
 *
 * <p>This strategy imposes limitation on user-developed applications. <br>
 *
 * <pre>
 *   Path-Based Ingress exposing service's port:
 * Ingress
 * ...
 * spec:
 *   rules:
 *     - host: CHE_HOST
 *       http:
 *         paths:
 *           - path: service123/webapp        ---->> Service.metadata.name + / + Service.spec.ports[0].name
 *             backend:
 *               serviceName: service123      ---->> Service.metadata.name
 *               servicePort: [8080|web-app]  ---->> Service.spec.ports[0].[port|name]
 * </pre>
 *
 * @author Sergii Leshchenko
 * @author Guy Daich
 */
public class SingleHostExternalServiceExposureStrategy implements ExternalServiceExposureStrategy {

  public static final String SINGLE_HOST_STRATEGY = "single-host";
  private final String cheHost;

  @Inject
  public SingleHostExternalServiceExposureStrategy(@Named("che.host") String cheHost) {
    this.cheHost = cheHost;
  }

  @Override
  public String getExternalHost(String serviceName, String serverName) {
    return cheHost;
  }

  @Override
  public String getExternalPath(String serviceName, String serverName) {
    return "/" + serviceName + "/" + serverName;
  }
}
