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

import static org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil.getWsAgentServerMachine;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolumeMount;

import com.google.inject.Inject;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.Map;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
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
 */
public class CommonPVCStrategy implements WorkspacePVCStrategy {

  public static final String COMMON_STRATEGY = "common";

  private final String pvcQuantity;
  private final String pvcName;
  private final String pvcAccessMode;
  private final String projectsPath;
  private final PVCSubPathHelper pvcSubPathHelper;
  private final OpenShiftProjectFactory factory;

  @Inject
  public CommonPVCStrategy(
      @Named("che.infra.openshift.pvc.name") String pvcName,
      @Named("che.infra.openshift.pvc.quantity") String pvcQuantity,
      @Named("che.infra.openshift.pvc.access_mode") String pvcAccessMode,
      @Named("che.workspace.projects.storage") String projectFolderPath,
      PVCSubPathHelper pvcSubPathHelper,
      OpenShiftProjectFactory factory) {
    this.pvcName = pvcName;
    this.pvcQuantity = pvcQuantity;
    this.pvcAccessMode = pvcAccessMode;
    this.projectsPath =
        projectFolderPath.startsWith("/") ? projectFolderPath : '/' + projectFolderPath;
    this.pvcSubPathHelper = pvcSubPathHelper;
    this.factory = factory;
  }

  @Override
  public void prepare(InternalEnvironment env, OpenShiftEnvironment osEnv, String workspaceId)
      throws InfrastructureException {
    final String machineWithSources =
        getWsAgentServerMachine(env)
            .orElseThrow(() -> new InfrastructureException("Machine with ws-agent not found"));
    final Map<String, PersistentVolumeClaim> claims = osEnv.getPersistentVolumeClaims();
    claims.put(pvcName, newPVC(pvcName, pvcAccessMode, pvcQuantity));
    factory.create(workspaceId).persistentVolumeClaims().createIfNotExist(claims.values());
    final String subPath = workspaceId + projectsPath;
    pvcSubPathHelper.createDirs(workspaceId, subPath);
    for (Pod pod : osEnv.getPods().values()) {
      final PodSpec podSpec = pod.getSpec();
      for (Container container : podSpec.getContainers()) {
        final String machine = Names.machineName(pod, container);
        if (machine.equals(machineWithSources)) {
          container.getVolumeMounts().add(newVolumeMount(pvcName, projectsPath, subPath));
          podSpec.getVolumes().add(newVolume(pvcName, pvcName));
          return;
        }
      }
    }
  }

  @Override
  public void cleanup(String workspaceId) throws InfrastructureException {
    pvcSubPathHelper.removeDirsAsync(workspaceId, workspaceId);
  }
}
