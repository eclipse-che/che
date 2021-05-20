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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

/**
 * Provides common PVC per each workspace.
 *
 * <p>Names for PVCs are evaluated as: '{configured_prefix}' + '-' +'{workspaceId}' to avoid naming
 * collisions inside of one Kubernetes namespace.
 *
 * <p>This strategy uses subpaths to do the same as {@link CommonPVCStrategy} does and make easier
 * data migration if it will be needed.<br>
 * Subpaths have the following format: '{workspaceId}/{volumeName}'.<br>
 * Note that logs volume has the special format: '{workspaceId}/{volumeName}/{machineName}'. It is
 * done in this way to avoid conflicts e.g. two identical agents inside different machines produce
 * the same log file.
 *
 * @author Sergii Leshchenko
 * @author Masaki Muranaka
 */
public class PerWorkspacePVCStrategy extends CommonPVCStrategy {

  public static final String PER_WORKSPACE_STRATEGY = "per-workspace";

  private final KubernetesNamespaceFactory factory;
  private final String pvcNamePrefix;
  private final String pvcAccessMode;
  private final String pvcQuantity;
  private final String pvcStorageClassName;

  @Inject
  public PerWorkspacePVCStrategy(
      @Named("che.infra.kubernetes.pvc.name") String pvcName,
      @Named("che.infra.kubernetes.pvc.quantity") String pvcQuantity,
      @Named("che.infra.kubernetes.pvc.access_mode") String pvcAccessMode,
      @Named("che.infra.kubernetes.pvc.precreate_subpaths") boolean preCreateDirs,
      @Named("che.infra.kubernetes.pvc.storage_class_name") String pvcStorageClassName,
      @Named("che.infra.kubernetes.pvc.wait_bound") boolean waitBound,
      PVCSubPathHelper pvcSubPathHelper,
      KubernetesNamespaceFactory factory,
      EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter,
      PVCProvisioner pvcProvisioner,
      PodsVolumes podsVolumes,
      SubPathPrefixes subpathPrefixes) {
    super(
        pvcName,
        pvcQuantity,
        pvcAccessMode,
        preCreateDirs,
        pvcStorageClassName,
        waitBound,
        pvcSubPathHelper,
        factory,
        ephemeralWorkspaceAdapter,
        pvcProvisioner,
        podsVolumes,
        subpathPrefixes);
    this.pvcNamePrefix = pvcName;
    this.factory = factory;
    this.pvcAccessMode = pvcAccessMode;
    this.pvcQuantity = pvcQuantity;
    this.pvcStorageClassName = pvcStorageClassName;
  }

  @Override
  protected PersistentVolumeClaim createCommonPVC(String workspaceId) {
    String pvcName = pvcNamePrefix + '-' + workspaceId;

    PersistentVolumeClaim perWorkspacePVC =
        newPVC(pvcName, pvcAccessMode, pvcQuantity, pvcStorageClassName);
    putLabel(perWorkspacePVC.getMetadata(), CHE_WORKSPACE_ID_LABEL, workspaceId);
    return perWorkspacePVC;
  }

  @Override
  public void cleanup(Workspace workspace) throws InfrastructureException {
    if (EphemeralWorkspaceUtility.isEphemeral(workspace)) {
      return;
    }
    final String workspaceId = workspace.getId();
    factory
        .get(workspace)
        .persistentVolumeClaims()
        .delete(ImmutableMap.of(CHE_WORKSPACE_ID_LABEL, workspaceId));
  }
}
