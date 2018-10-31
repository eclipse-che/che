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

import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolumeMount;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner.LOGS_VOLUME_NAME;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Allows to create ephemeral workspaces (with no PVC attached) based on workspace config
 * `persistVolumes` attribute. If `persistVolumes` attribute is set to false, workspace volumes
 * would be created as `emptyDir` regardless of the PVC strategy. When a workspace Pod is removed
 * for any reason, the data in the `emptyDir` volume is deleted forever.
 *
 * @see <a href="https://kubernetes.io/docs/concepts/storage/volumes/#emptydir">emptyDir</a>
 * @author Ilya Buziuk
 * @author Angel Misevski
 */
@Singleton
public class EphemeralWorkspaceAdapter {
  private static final String EPHEMERAL_VOLUME_NAME_PREFIX = "ephemeral-che-workspace-";

  /**
   * @param workspace
   * @return true if workspace config contains `persistVolumes` attribute which is set to false. In
   *     this case regardless of the PVC strategy, workspace volumes would be created as `emptyDir`.
   *     When a workspace Pod is removed for any reason, the data in the `emptyDir` volume is
   *     deleted forever
   */
  public static boolean isEphemeral(Workspace workspace) {
    return isEphemeral(workspace.getConfig().getAttributes());
  }

  /**
   * @param workspaceAttributes
   * @return true if `persistVolumes` attribute exists and set to 'false'. In this case regardless
   *     of the PVC strategy, workspace volumes would be created as `emptyDir`. When a workspace Pod
   *     is removed for any reason, the data in the `emptyDir` volume is deleted forever
   */
  public static boolean isEphemeral(Map<String, String> workspaceAttributes) {
    String persistVolumes = workspaceAttributes.get(PERSIST_VOLUMES_ATTRIBUTE);
    return "false".equals(persistVolumes);
  }

  /**
   * Change workspace attributes such that future calls to {@link
   * EphemeralWorkspaceAdapter#isEphemeral} will return true.
   *
   * @param workspaceAttributes
   */
  public static void makeEphemeral(Map<String, String> workspaceAttributes) {
    workspaceAttributes.put(PERSIST_VOLUMES_ATTRIBUTE, "false");
  }

  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    for (Pod pod : k8sEnv.getPods().values()) {
      PodSpec podSpec = pod.getSpec();

      // To ensure same volumes get mounted correctly in different containers, we need to track
      // which volumes have been "created"
      Map<String, String> volumeKeyToNameCache = new HashMap<>();

      List<Container> containers = new ArrayList<>();
      containers.addAll(podSpec.getContainers());
      containers.addAll(podSpec.getInitContainers());
      for (Container container : containers) {
        String machineName = Names.machineName(pod, container);
        Map<String, Volume> volumes = k8sEnv.getMachines().get(machineName).getVolumes();
        if (volumes.isEmpty()) {
          continue;
        }

        for (Entry<String, Volume> volumeEntry : volumes.entrySet()) {
          final String volumePath = volumeEntry.getValue().getPath();
          final String volumeKey =
              LOGS_VOLUME_NAME.equals(volumeEntry.getKey())
                  ? volumeEntry.getKey() + '-' + pod.getMetadata().getName()
                  : volumeEntry.getKey();

          final String uniqueVolumeName;
          if (volumeKeyToNameCache.containsKey(volumeKey)) {
            uniqueVolumeName = volumeKeyToNameCache.get(volumeKey);
          } else {
            uniqueVolumeName = Names.generateName(EPHEMERAL_VOLUME_NAME_PREFIX);
            volumeKeyToNameCache.put(volumeKey, uniqueVolumeName);
          }
          // binds volume to pod and container
          container
              .getVolumeMounts()
              .add(
                  newVolumeMount(
                      uniqueVolumeName,
                      volumePath,
                      getSubPath(identity.getWorkspaceId(), volumeKey, machineName)));
          addEmptyDirVolumeIfAbsent(pod.getSpec(), uniqueVolumeName);
        }
      }
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
