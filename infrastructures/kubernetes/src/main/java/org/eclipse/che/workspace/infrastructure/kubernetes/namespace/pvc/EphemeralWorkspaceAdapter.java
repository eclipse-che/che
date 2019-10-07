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

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolumeMount;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner.LOGS_VOLUME_NAME;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EmptyDirVolumeSource;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to create ephemeral workspaces (with no PVC attached) based on workspace config
 * `persistVolumes` attribute. If `persistVolumes` attribute is set to false, workspace volumes
 * would be created as `emptyDir` regardless of the PVC strategy. User-defined PVCs will be removed
 * from environment and the corresponding PVC volumes in Pods will be replaced with `emptyDir`
 * volumes. When a workspace Pod is removed for any reason, the data in the `emptyDir` volume is
 * deleted forever.
 *
 * @see <a href="https://kubernetes.io/docs/concepts/storage/volumes/#emptydir">emptyDir</a>
 * @author Ilya Buziuk
 * @author Angel Misevski
 */
@Singleton
public class EphemeralWorkspaceAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(CommonPVCStrategy.class);

  private static final String EPHEMERAL_VOLUME_NAME_PREFIX = "ephemeral-che-workspace-";

  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    LOG.debug("Provisioning PVC strategy for workspace '{}'", identity.getWorkspaceId());
    k8sEnv.getPersistentVolumeClaims().clear();
    for (PodData pod : k8sEnv.getPodsData().values()) {
      PodSpec podSpec = pod.getSpec();

      // To ensure same volumes get mounted correctly in different containers, we need to track
      // which volumes have been "created"
      Map<String, String> cheVolumeNameToPodVolumeName = new HashMap<>();

      podSpec
          .getVolumes()
          .stream()
          .filter(v -> v.getPersistentVolumeClaim() != null)
          .forEach(
              v -> {
                String claimName = v.getPersistentVolumeClaim().getClaimName();
                cheVolumeNameToPodVolumeName.put(claimName, v.getName());
                v.setPersistentVolumeClaim(null);
                v.setEmptyDir(new EmptyDirVolumeSource());
              });

      List<Container> containers = new ArrayList<>();
      containers.addAll(podSpec.getContainers());
      containers.addAll(podSpec.getInitContainers());
      for (Container container : containers) {
        String machineName = Names.machineName(pod, container);
        InternalMachineConfig machineConfig = k8sEnv.getMachines().get(machineName);
        if (machineConfig == null) {
          return;
        }
        Map<String, Volume> volumes = machineConfig.getVolumes();
        if (volumes.isEmpty()) {
          continue;
        }

        for (Entry<String, Volume> volumeEntry : volumes.entrySet()) {
          final String volumePath = volumeEntry.getValue().getPath();
          final String cheVolumeName =
              LOGS_VOLUME_NAME.equals(volumeEntry.getKey())
                  ? volumeEntry.getKey() + '-' + pod.getMetadata().getName()
                  : volumeEntry.getKey();

          final String uniqueVolumeName;
          if (cheVolumeNameToPodVolumeName.containsKey(cheVolumeName)) {
            uniqueVolumeName = cheVolumeNameToPodVolumeName.get(cheVolumeName);
          } else {
            uniqueVolumeName = Names.generateName(EPHEMERAL_VOLUME_NAME_PREFIX);
            cheVolumeNameToPodVolumeName.put(cheVolumeName, uniqueVolumeName);
          }
          // binds volume to pod and container
          container
              .getVolumeMounts()
              .add(
                  newVolumeMount(
                      uniqueVolumeName,
                      volumePath,
                      getSubPath(identity.getWorkspaceId(), cheVolumeName, machineName)));
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
