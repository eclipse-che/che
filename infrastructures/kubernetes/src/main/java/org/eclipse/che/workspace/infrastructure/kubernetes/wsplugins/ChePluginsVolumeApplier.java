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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolumeMount;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import java.util.Collection;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.wsplugins.model.Volume;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Components for applying workspace plugin volumes to the kubernetes {@link
 * io.fabric8.kubernetes.api.model.Container}.
 */
@Singleton
public class ChePluginsVolumeApplier {

  private final String pvcQuantity;
  private final String pvcAccessMode;
  private final String pvcStorageClassName;

  @Inject
  public ChePluginsVolumeApplier(
      @Named("che.infra.kubernetes.pvc.quantity") String pvcQuantity,
      @Named("che.infra.kubernetes.pvc.access_mode") String pvcAccessMode,
      @Named("che.infra.kubernetes.pvc.storage_class_name") String pvcStorageClassName) {
    this.pvcQuantity = pvcQuantity;
    this.pvcAccessMode = pvcAccessMode;
    this.pvcStorageClassName = pvcStorageClassName;
  }

  public void applyVolumes(
      KubernetesEnvironment.PodData pod,
      Container container,
      Collection<Volume> volumes,
      KubernetesEnvironment k8sEnv) {
    for (Volume volume : volumes) {
      String podVolumeName = provisionPodVolume(volume, pod, k8sEnv);

      container.getVolumeMounts().add(newVolumeMount(podVolumeName, volume.getMountPath(), ""));
    }
  }

  private String provisionPodVolume(
      Volume volume, KubernetesEnvironment.PodData pod, KubernetesEnvironment k8sEnv) {
    if (volume.isEphemeral()) {
      addEmptyDirVolumeIfAbsent(pod.getSpec(), volume.getName());
      return volume.getName();
    } else {
      return provisionPVCPodVolume(volume, pod, k8sEnv).getName();
    }
  }

  private io.fabric8.kubernetes.api.model.Volume provisionPVCPodVolume(
      Volume volume, KubernetesEnvironment.PodData pod, KubernetesEnvironment k8sEnv) {
    String pvcName = volume.getName();

    if (!k8sEnv.getPersistentVolumeClaims().containsKey(pvcName)) {
      final PersistentVolumeClaim pvc =
          newPVC(pvcName, pvcAccessMode, pvcQuantity, pvcStorageClassName);
      k8sEnv.getPersistentVolumeClaims().put(pvcName, pvc);
    }

    PodSpec podSpec = pod.getSpec();
    Optional<io.fabric8.kubernetes.api.model.Volume> volumeOpt =
        podSpec
            .getVolumes()
            .stream()
            .filter(
                vm ->
                    vm.getPersistentVolumeClaim() != null
                        && pvcName.equals(vm.getPersistentVolumeClaim().getClaimName()))
            .findAny();
    io.fabric8.kubernetes.api.model.Volume podVolume;
    if (volumeOpt.isPresent()) {
      podVolume = volumeOpt.get();
    } else {
      podVolume = newVolume(pvcName, pvcName);
      podSpec.getVolumes().add(podVolume);
    }
    return podVolume;
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
}
