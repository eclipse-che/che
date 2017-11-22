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

import com.google.inject.Inject;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.openshift.Names;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;

/**
 * Provides common PVC for each workspace in one OpenShift project.
 *
 * <p>Note that subpaths are used for resolving of backed up data path collisions. Subpaths
 * evaluated as following: '{workspaceId}/{workspace data folder}'. Workspace data folder it's a
 * configured path where workspace projects, logs or any other data located. The number of
 * workspaces that can simultaneously store backups in one PV is limited only by the storage
 * capacity. The number of workspaces that can be running simultaneously depends on access mode
 * configuration and Che configuration limits.
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
public class CommonPVCStrategy implements WorkspaceVolumesStrategy {

  public static final String COMMON_STRATEGY = "common";

  private final String pvcQuantity;
  private final String pvcName;
  private final String pvcAccessMode;
  private final PVCSubPathHelper pvcSubPathHelper;
  private final OpenShiftProjectFactory factory;

  @Inject
  public CommonPVCStrategy(
      @Named("che.infra.openshift.pvc.name") String pvcName,
      @Named("che.infra.openshift.pvc.quantity") String pvcQuantity,
      @Named("che.infra.openshift.pvc.access_mode") String pvcAccessMode,
      PVCSubPathHelper pvcSubPathHelper,
      OpenShiftProjectFactory factory) {
    this.pvcName = pvcName;
    this.pvcQuantity = pvcQuantity;
    this.pvcAccessMode = pvcAccessMode;
    this.pvcSubPathHelper = pvcSubPathHelper;
    this.factory = factory;
  }

  @Override
  public void prepare(OpenShiftEnvironment osEnv, String workspaceId)
      throws InfrastructureException {
    Set<String> subPaths = new HashSet<>();
    osEnv.getPersistentVolumeClaims().put(pvcName, newPVC(pvcName, pvcAccessMode, pvcQuantity));
    factory
        .create(workspaceId)
        .persistentVolumeClaims()
        .createIfNotExist(osEnv.getPersistentVolumeClaims().values());

    for (Pod pod : osEnv.getPods().values()) {
      PodSpec podSpec = pod.getSpec();
      for (Container container : podSpec.getContainers()) {
        String machineName = Names.machineName(pod, container);
        InternalMachineConfig machineConfig = osEnv.getMachines().get(machineName);
        addMachineVolumes(workspaceId, subPaths, podSpec, container, machineConfig);
      }
    }
    if (!subPaths.isEmpty()) {
      pvcSubPathHelper.createDirs(workspaceId, subPaths.toArray(new String[subPaths.size()]));
    }
  }

  @Override
  public void cleanup(String workspaceId) throws InfrastructureException {
    pvcSubPathHelper.removeDirsAsync(workspaceId, getWorkspaceSubPath(workspaceId));
  }

  private void addMachineVolumes(
      String workspaceId,
      Set<String> subPaths,
      PodSpec podSpec,
      Container container,
      InternalMachineConfig machineConfig) {
    if (machineConfig == null || machineConfig.getVolumes().isEmpty()) {
      return;
    }
    for (Entry<String, Volume> volumeEntry : machineConfig.getVolumes().entrySet()) {
      String volumePath = volumeEntry.getValue().getPath();
      String subPath = getVolumeSubPath(workspaceId, volumeEntry.getKey());
      subPaths.add(subPath);

      container.getVolumeMounts().add(newVolumeMount(pvcName, volumePath, subPath));
      addVolumeIfNeeded(podSpec);
    }
  }

  private void addVolumeIfNeeded(PodSpec podSpec) {
    if (podSpec.getVolumes().stream().noneMatch(volume -> volume.getName().equals(pvcName))) {

      podSpec.getVolumes().add(newVolume(pvcName, pvcName));
    }
  }

  /** Get sub-path that holds all the volumes of a particular workspace */
  private String getWorkspaceSubPath(String workspaceId) {
    return workspaceId;
  }

  /** Get sub-path for particular volume in a particular workspace */
  private String getVolumeSubPath(String workspaceId, String volumeName) {
    // this path should correlate with path returned by method getWorkspaceSubPath
    // because this logic is used to correctly cleanup sub-paths related to a workspace
    return getWorkspaceSubPath(workspaceId) + '/' + volumeName;
  }
}
