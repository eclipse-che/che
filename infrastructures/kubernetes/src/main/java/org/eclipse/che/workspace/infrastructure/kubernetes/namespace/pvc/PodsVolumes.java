/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Helps to works with Pods Volumes, like reference them to another PVC.
 *
 * @author Sergii Leshchenko
 */
public class PodsVolumes {

  /**
   * Changes all pods volumes witch referenced the specified PVC to reference new PVC.
   *
   * @param pods pods to change
   * @param currentPVCName current PVC name for filtering pods volumes
   * @param newPVCName new PVC name that should be used
   */
  public void changePVCReferences(
      Collection<PodData> pods, String currentPVCName, String newPVCName) {
    pods.stream()
        .flatMap(p -> p.getSpec().getVolumes().stream())
        .filter(
            v ->
                v.getPersistentVolumeClaim() != null
                    && v.getPersistentVolumeClaim().getClaimName().equals(currentPVCName))
        .forEach(v -> v.getPersistentVolumeClaim().setClaimName(newPVCName));
  }

  /**
   * Replaces all pods PVC sourced volumes with the specified one.
   *
   * @param pods pods to change
   * @param commonPVCName PVC name that should be referenced in all existing PVC sources volumes
   */
  public void replacePVCVolumesWithCommon(Map<String, PodData> pods, String commonPVCName) {
    for (PodData pod : pods.values()) {
      Set<String> pvcSourcedVolumes = reducePVCSourcedVolumes(pod.getSpec().getVolumes());

      if (pvcSourcedVolumes.isEmpty()) {
        continue;
      }

      // add common PVC sourced volume instead of removed
      pod.getSpec()
          .getVolumes()
          .add(
              new VolumeBuilder()
                  .withName(commonPVCName)
                  .withNewPersistentVolumeClaim()
                  .withClaimName(commonPVCName)
                  .endPersistentVolumeClaim()
                  .build());

      Stream.concat(
              pod.getSpec().getContainers().stream(), pod.getSpec().getInitContainers().stream())
          .flatMap(c -> c.getVolumeMounts().stream())
          .filter(vm -> pvcSourcedVolumes.contains(vm.getName()))
          .forEach(vm -> vm.setName(commonPVCName));
    }
  }

  private static Set<String> reducePVCSourcedVolumes(List<Volume> volumes) {
    Set<String> pvcSourcedVolumes = new HashSet<>();
    Iterator<Volume> volumeIterator = volumes.iterator();
    while (volumeIterator.hasNext()) {
      Volume volume = volumeIterator.next();
      if (volume.getPersistentVolumeClaim() != null) {
        pvcSourcedVolumes.add(volume.getName());
        volumeIterator.remove();
      }
    }
    return pvcSourcedVolumes;
  }
}
