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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static org.eclipse.che.api.workspace.shared.Constants.MOUNT_SOURCES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolumeMount;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner.LOGS_VOLUME_NAME;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/** @author Ilya Buziuk (ibuziuk) */
@Singleton
public class EphemeralWorkspaceAdapter {
  private final String EPHEMERAL_VOLUME_NAME_PREFIX = "ephemeral-che-workspace-";
  private WorkspaceManager workspaceManager;

  @Inject
  public EphemeralWorkspaceAdapter(WorkspaceManager workspaceManager) {
    this.workspaceManager = workspaceManager;
  }

  /**
   * @param workspaceId
   * @return true if workspace config contains `mountSources` attribute which is set to false. In
   *     this case regardless of the PVC strategy, workspace volumes would be created as `emptyDir`.
   *     When a workspace Pod is removed for any reason, the data in the `emptyDir` volume is
   *     deleted forever
   * @throws InternalInfrastructureException
   */
  public boolean isEphemeral(String workspaceId) throws InternalInfrastructureException {
    try {
      WorkspaceImpl workspace = workspaceManager.getWorkspace(workspaceId);
      return isEphemeral(workspace);
    } catch (NotFoundException | ServerException e) {
      throw new InternalInfrastructureException(
          "Failed to load workspace info" + e.getMessage(), e);
    }
  }

  /**
   * @param workspace
   * @return true if workspace config contains `mountSources` attribute which is set to false. In
   *     this case regardless of the PVC strategy, workspace volumes would be created as `emptyDir`.
   *     When a workspace Pod is removed for any reason, the data in the `emptyDir` volume is
   *     deleted forever
   * @throws InternalInfrastructureException
   */
  public boolean isEphemeral(Workspace workspace) {
    String mountSources = workspace.getConfig().getAttributes().get(MOUNT_SOURCES_ATTRIBUTE);
    return "false".equals(mountSources);
  }

  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    for (Pod pod : k8sEnv.getPods().values()) {
      PodSpec podSpec = pod.getSpec();
      for (Container container : podSpec.getContainers()) {
        String machineName = Names.machineName(pod, container);
        Map<String, Volume> volumes = k8sEnv.getMachines().get(machineName).getVolumes();
        addMachineVolumes(identity.getWorkspaceId(), pod, container, volumes);
      }
    }
  }

  private void addMachineVolumes(
      String workspaceId, Pod pod, Container container, Map<String, Volume> volumes)
      throws InfrastructureException {
    if (volumes.isEmpty()) {
      return;
    }

    for (Entry<String, Volume> volumeEntry : volumes.entrySet()) {
      final String volumePath = volumeEntry.getValue().getPath();
      final String volumeName =
          LOGS_VOLUME_NAME.equals(volumeEntry.getKey())
              ? volumeEntry.getKey() + '-' + pod.getMetadata().getName()
              : volumeEntry.getKey();

      final String uniqueVolumeName = Names.generateName(EPHEMERAL_VOLUME_NAME_PREFIX);

      // binds volume to pod and container
      container
          .getVolumeMounts()
          .add(
              newVolumeMount(
                  uniqueVolumeName,
                  volumePath,
                  getSubPath(workspaceId, volumeName, Names.machineName(pod, container))));
      addEmptyDirVolumeIfAbsent(pod.getSpec(), uniqueVolumeName);
    }
  }

  private void addEmptyDirVolumeIfAbsent(PodSpec podSpec, String uniqueVolumeName) {
    if (podSpec
        .getVolumes()
        .stream()
        .noneMatch(volume -> volume.getName().equals(uniqueVolumeName))) {
      podSpec
          .getVolumes()
          .add(
              new VolumeBuilder()
                  .withName(uniqueVolumeName)
                  .withNewEmptyDir()
                  .endEmptyDir()
                  .build());
    }
  }

  private String getSubPath(String workspaceId, String volumeName, String machineName) {
    // logs must be located inside the folder related to the machine because few machines can
    // contain the identical agents and in this case, a conflict is possible.
    if (LOGS_VOLUME_NAME.equals(volumeName)) {
      return workspaceId + '/' + volumeName + '/' + machineName;
    }
    return workspaceId + '/' + volumeName;
  }
}
