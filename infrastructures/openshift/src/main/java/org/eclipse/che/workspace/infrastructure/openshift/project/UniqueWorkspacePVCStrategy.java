/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil.getWsAgentServerMachine;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolumeMount;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.openshift.client.OpenShiftClient;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * Provides a unique PVC for each workspace.
 *
 * <p>Name for PVCs are evaluated as: '{configuredPVCName}' + '-' +'{workspaceId}' to avoid naming
 * collisions inside of one OpenShift project. Note that for this strategy count of simultaneously
 * running workspaces and workspaces with backed up data is always the same and equal to the count
 * of available PVCs in OpenShift project. Cleanup of backed up data is performed by removing of PVC
 * related to the workspace.
 *
 * @author Anton Korneta
 */
public class UniqueWorkspacePVCStrategy implements WorkspacePVCStrategy {

  public static final String UNIQUE_STRATEGY = "unique";

  private final String pvcName;
  private final String projectName;
  private final String pvcQuantity;
  private final String pvcAccessMode;
  private final String projectsPath;
  private final OpenShiftClientFactory clientFactory;

  @Inject
  public UniqueWorkspacePVCStrategy(
      @Nullable @Named("che.infra.openshift.project") String projectName,
      @Named("che.infra.openshift.pvc.name") String pvcName,
      @Named("che.infra.openshift.pvc.quantity") String pvcQuantity,
      @Named("che.infra.openshift.pvc.access_mode") String pvcAccessMode,
      @Named("che.workspace.projects.storage") String projectFolderPath,
      OpenShiftClientFactory clientFactory) {
    this.pvcName = pvcName;
    this.pvcQuantity = pvcQuantity;
    this.projectName = projectName;
    this.pvcAccessMode = pvcAccessMode;
    this.projectsPath = projectFolderPath;
    this.clientFactory = clientFactory;
  }

  @Override
  public void prepare(InternalEnvironment env, OpenShiftEnvironment osEnv, String workspaceId)
      throws InfrastructureException {
    final String pvcUniqueName = pvcName + '-' + workspaceId;
    final PersistentVolumeClaim pvc = osEnv.getPersistentVolumeClaims().get(pvcUniqueName);
    if (pvc != null) {
      return;
    }
    final String machineWithSources =
        getWsAgentServerMachine(env)
            .orElseThrow(() -> new InfrastructureException("Machine with wsagent not found"));
    osEnv
        .getPersistentVolumeClaims()
        .put(pvcUniqueName, newPVC(pvcUniqueName, pvcAccessMode, pvcQuantity));
    for (Pod pod : osEnv.getPods().values()) {
      final PodSpec podSpec = pod.getSpec();
      for (Container container : podSpec.getContainers()) {
        final String machine = pod.getMetadata().getName() + '/' + container.getName();
        if (machine.equals(machineWithSources)) {
          final String subPath =
              projectsPath.startsWith("/") ? projectsPath.substring(1) : projectsPath;
          container.getVolumeMounts().add(newVolumeMount(pvcUniqueName, projectsPath, subPath));
          podSpec.getVolumes().add(newVolume(pvcUniqueName, pvcUniqueName));
          return;
        }
      }
    }
  }

  @Override
  public void cleanup(String workspaceId) {
    try (OpenShiftClient client = clientFactory.create()) {
      final String pvcUniqueName = pvcName + '-' + workspaceId;
      client.persistentVolumeClaims().inNamespace(projectName).withName(pvcUniqueName).delete();
    }
  }
}
