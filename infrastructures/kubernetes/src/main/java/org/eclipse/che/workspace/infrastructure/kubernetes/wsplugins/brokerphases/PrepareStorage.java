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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newPVC;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;

/**
 * Prepares PVC in a workspace and calls next {@link BrokerPhase}.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class PrepareStorage extends BrokerPhase {

  private final String workspaceId;
  private final KubernetesEnvironment environment;
  private final WorkspaceVolumesStrategy volumesStrategy;
  private final String pvcName;
  private final String pvcAccessMode;
  private final String pvcQuantity;

  public PrepareStorage(
      String workspaceId,
      KubernetesEnvironment environment,
      WorkspaceVolumesStrategy volumesStrategy,
      String pvcName,
      String pvcAccessMode,
      String pvcQuantity) {
    this.workspaceId = workspaceId;
    this.environment = environment;
    this.volumesStrategy = volumesStrategy;
    this.pvcName = pvcName;
    this.pvcAccessMode = pvcAccessMode;
    this.pvcQuantity = pvcQuantity;
  }

  @Override
  public List<ChePlugin> execute() throws InfrastructureException {
    final PersistentVolumeClaim pvc = newPVC(pvcName, pvcAccessMode, pvcQuantity);
    environment.getPersistentVolumeClaims().put(pvcName, pvc);
    volumesStrategy.prepare(environment, workspaceId);

    return nextPhase.execute();
  }
}
