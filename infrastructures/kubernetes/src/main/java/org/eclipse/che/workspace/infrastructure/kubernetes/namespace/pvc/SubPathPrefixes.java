/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_VOLUME_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner.LOGS_VOLUME_NAME;

import com.google.common.base.Strings;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Is responsible for prefixing sub-paths of volume mounts and should be used by all PVCs
 * strategies.
 *
 * @author Sergii Leshchenko
 */
public class SubPathPrefixes {

  /**
   * Prefixes volumes mounts of containers inside of the specified kubernetes environment.
   *
   * <p>Subpaths have the following format: '{workspaceId}/{Che Volume name|PVC name}'.<br>
   * Where Che Volume is used if it is present in PVC labels, otherwise PVC name will be used.<br>
   * Note that logs volume has the special format: '{workspaceId}/{volumeName}/{machineName}'. It is
   * done in this way to avoid conflicts e.g. two identical agents inside different machines produce
   * the same log file.
   *
   * @param k8sEnv environment to process
   * @param workspaceId workspace id that should be used as prefix
   */
  public void prefixVolumeMountsSubpaths(KubernetesEnvironment k8sEnv, String workspaceId) {
    for (PodData pod : k8sEnv.getPodsData().values()) {
      Map<String, String> volumeToCheVolumeName = new HashMap<>();
      for (io.fabric8.kubernetes.api.model.Volume volume : pod.getSpec().getVolumes()) {
        if (volume.getPersistentVolumeClaim() == null) {
          continue;
        }
        PersistentVolumeClaim pvc =
            k8sEnv
                .getPersistentVolumeClaims()
                .get(volume.getPersistentVolumeClaim().getClaimName());

        String cheVolumeName = pvc.getMetadata().getLabels().get(CHE_VOLUME_NAME_LABEL);
        if (cheVolumeName == null) {
          cheVolumeName = pvc.getMetadata().getName();
          pvc.getMetadata().getLabels().put(CHE_VOLUME_NAME_LABEL, cheVolumeName);
        }
        volumeToCheVolumeName.put(volume.getName(), cheVolumeName);
      }

      if (volumeToCheVolumeName.isEmpty()) {
        // Pod does not have any volume that references PVC
        continue;
      }

      Stream.concat(
              pod.getSpec().getContainers().stream(), pod.getSpec().getInitContainers().stream())
          .forEach(
              c -> {
                for (VolumeMount volumeMount : c.getVolumeMounts()) {
                  String pvcName = volumeToCheVolumeName.get(volumeMount.getName());
                  if (pvcName == null) {
                    // should not happens since Volume<>PVC links are checked during recipe
                    // validation
                    continue;
                  }

                  String volumeSubPath =
                      getVolumeMountSubpath(
                          volumeMount, pvcName, workspaceId, Names.machineName(pod, c));
                  volumeMount.setSubPath(volumeSubPath);
                }
              });
    }
  }

  /** Get sub-path for particular Volume Mount in a particular workspace */
  private String getVolumeMountSubpath(
      VolumeMount volumeMount, String volumeName, String workspaceId, String machineName) {
    String volumeMountSubPath = Strings.nullToEmpty(volumeMount.getSubPath());
    if (!volumeMountSubPath.startsWith("/")) {
      volumeMountSubPath = '/' + volumeMountSubPath;
    }

    return getVolumeSubpath(workspaceId, volumeName, machineName) + volumeMountSubPath;
  }

  private String getVolumeSubpath(String workspaceId, String volumeName, String machineName) {
    // logs must be located inside the folder related to the machine because few machines can
    // contain the identical agents and in this case, a conflict is possible.
    if (LOGS_VOLUME_NAME.equals(volumeName)) {
      return getWorkspaceSubPath(workspaceId) + '/' + volumeName + '/' + machineName;
    }
    return getWorkspaceSubPath(workspaceId) + '/' + volumeName;
  }

  /** Get sub-path that holds all the volumes of a particular workspace */
  public String getWorkspaceSubPath(String workspaceId) {
    return workspaceId;
  }
}
