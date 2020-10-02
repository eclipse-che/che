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

import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostExternalServiceExposureStrategy.SINGLE_HOST_STRATEGY;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.CheNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;

public class RuntimeCleaner {

  private final boolean cleanupCheNamespace;

  private final CheNamespace cheNamespace;

  @Inject
  public RuntimeCleaner(
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy,
      @Named("che.infra.kubernetes.singlehost.workspace.exposure") String singleHostStrategy,
      CheNamespace cheNamespace) {
    this.cheNamespace = cheNamespace;

    this.cleanupCheNamespace =
        SINGLE_HOST_STRATEGY.equals(exposureStrategy)
            && WorkspaceExposureType.GATEWAY.getConfigValue().equals(singleHostStrategy);
  }

  public void cleanUp(KubernetesNamespace namespace, String workspaceId)
      throws InfrastructureException {
    namespace.cleanUp();
    if (cleanupCheNamespace) {
      cheNamespace.cleanUp(workspaceId);
    }
  }
}
