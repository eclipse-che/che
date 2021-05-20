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
package org.eclipse.che.workspace.infrastructure.openshift.server;

import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServiceExposureStrategy;

/**
 * Even though we don't use ingresses to expose the services on OpenShift, we still need to provide
 * an implementation of this strategy that gives the rest of the system the idea of the paths on
 * which the services are exposed.
 */
public class OpenShiftServerExposureStrategy implements ExternalServiceExposureStrategy {
  @Override
  public String getExternalHost(String serviceName, String serverName) {
    return null;
  }

  @Override
  public String getExternalPath(String serviceName, String serverName) {
    return "/";
  }
}
