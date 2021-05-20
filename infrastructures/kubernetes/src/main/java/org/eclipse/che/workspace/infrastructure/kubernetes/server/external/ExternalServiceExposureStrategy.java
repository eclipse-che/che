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

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Implementations of this strategy are used by the {@link ExternalServerExposer} to compose an
 * Ingress rule that exposes the services.
 */
public interface ExternalServiceExposureStrategy {

  /** Returns a host that should be used to expose the service */
  @Nullable
  String getExternalHost(String serviceName, String serverName);

  /** Returns the path on which the service should be exposed */
  String getExternalPath(String serviceName, String serverName);
}
