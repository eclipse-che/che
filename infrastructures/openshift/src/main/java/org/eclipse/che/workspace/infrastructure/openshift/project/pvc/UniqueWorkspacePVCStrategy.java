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
package org.eclipse.che.workspace.infrastructure.openshift.project.pvc;

import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolumeMount;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.openshift.Names;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;

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
 * @author Alexander Garagatyi
 */
public class UniqueWorkspacePVCStrategy implements WorkspaceVolumesStrategy {

  public static final String UNIQUE_STRATEGY = "unique";

  private final String pvcName;
  private final String projectName;
  private final String pvcQuantity;
  private final String pvcAccessMode;
  private final OpenShiftClientFactory clientFactory;
  private final OpenShiftProjectFactory factory;

  @Inject
  public UniqueWorkspacePVCStrategy(
      @Nullable @Named("che.infra.openshift.project") String projectName,
      @Named("che.infra.openshift.pvc.name") String pvcName,
      @Named("che.infra.openshift.pvc.quantity") String pvcQuantity,
      @Named("che.infra.openshift.pvc.access_mode") String pvcAccessMode,
      OpenShiftClientFactory clientFactory,
      OpenShiftProjectFactory factory) {
    this.pvcName = pvcName;
    this.pvcQuantity = pvcQuantity;
    this.projectName = projectName;
    this.pvcAccessMode = pvcAccessMode;
    this.clientFactory = clientFactory;
    this.factory = factory;
  }

  @Override
  public void prepare(OpenShiftEnvironment osEnv, String workspaceId)
      throws InfrastructureException {
    Map<String, PersistentVolumeClaim> claims = osEnv.getPersistentVolumeClaims();
    for (Pod pod : osEnv.getPods().values()) {
      final PodSpec podSpec = pod.getSpec();
      for (Container container : podSpec.getContainers()) {
        final String machineName = Names.machineName(pod, container);
        InternalMachineConfig machineConfig = osEnv.getMachines().get(machineName);
        addMachineVolumes(workspaceId, claims, podSpec, container, machineConfig);
      }
    }
    factory.create(workspaceId).persistentVolumeClaims().createIfNotExist(claims.values());
  }

  private void addMachineVolumes(
      String workspaceId,
      Map<String, PersistentVolumeClaim> claims,
      PodSpec podSpec,
      Container container,
      InternalMachineConfig machineConfig) {
    if (machineConfig == null || machineConfig.getVolumes().isEmpty()) {
      return;
    }
    for (Entry<String, Volume> volumeEntry : machineConfig.getVolumes().entrySet()) {
      String volumeName = volumeEntry.getKey();
      String volumePath = volumeEntry.getValue().getPath();
      String subPath = workspaceId + '/' + volumeName;
      String pvcUniqueName = pvcName + '-' + workspaceId + '-' + volumeName;

      container.getVolumeMounts().add(newVolumeMount(pvcUniqueName, volumePath, subPath));
      claims.put(pvcUniqueName, newPVC(pvcUniqueName, pvcAccessMode, pvcQuantity));
      addVolumeIfNeeded(podSpec, pvcUniqueName);
    }
  }

  @Override
  public void cleanup(String workspaceId) throws InfrastructureException {
    final String pvcUniqueName = pvcName + '-' + workspaceId;
    clientFactory
        .create()
        .persistentVolumeClaims()
        .inNamespace(projectName)
        .withName(pvcUniqueName)
        .delete();
  }

  private void addVolumeIfNeeded(PodSpec podSpec, String pvcUniqueName) {
    if (podSpec.getVolumes().stream().noneMatch(volume -> volume.getName().equals(pvcName))) {

      podSpec.getVolumes().add(newVolume(pvcUniqueName, pvcUniqueName));
    }
  }
}
