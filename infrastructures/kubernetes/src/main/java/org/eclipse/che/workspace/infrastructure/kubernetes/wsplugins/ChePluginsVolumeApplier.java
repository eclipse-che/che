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

package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static java.util.stream.Collectors.toList;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.wsplugins.model.Volume;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

@Singleton
public class ChePluginsVolumeApplier {

  @Inject
  public ChePluginsVolumeApplier() {}

  public void applyVolumes(
      Container container,
      Collection<Volume> volumes,
      KubernetesEnvironment kubernetesEnvironment,
      KubernetesEnvironment.PodData pod) {
    List<Volume> ephemeralVolumes = new ArrayList<>();
    List<Volume> persistedVolumes = new ArrayList<>();
    for (Volume volume : volumes) {
      if (volume.isPersistVolume()) {
        persistedVolumes.add(volume);
      } else {
        ephemeralVolumes.add(volume);
      }
    }

    applyEphemeralVolumes(container, ephemeralVolumes, pod);
    applyPersistedVolumes(container, persistedVolumes, pod, kubernetesEnvironment);
  }

  private void applyEphemeralVolumes(
      Container container, List<Volume> volumes, KubernetesEnvironment.PodData pod) {
    List<VolumeMount> ephemeralVolumeMounts =
        volumes
            .stream()
            .map(
                volume ->
                    new VolumeMountBuilder()
                        .withName(volume.getName())
                        .withMountPath(volume.getMountPath())
                        .build())
            .collect(toList());

    container.getVolumeMounts().addAll(ephemeralVolumeMounts);

    for (VolumeMount ephemeralVolumeMount : ephemeralVolumeMounts) {
      addEmptyDirVolumeIfAbsent(pod.getSpec(), ephemeralVolumeMount.getName());
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

  private void applyPersistedVolumes(
      Container container,
      List<Volume> volumes,
      KubernetesEnvironment.PodData pod,
      KubernetesEnvironment kubernetesEnvironment) {
    List<VolumeMount> volumeMounts =
        volumes
            .stream()
            .map(
                volume ->
                    new VolumeMountBuilder()
                        .withName(volume.getName())
                        .withMountPath(volume.getMountPath())
                        .build())
            .collect(toList());

    container.getVolumeMounts().addAll(volumeMounts);

    for (Volume volume : volumes) {
      pod.getSpec()
          .getVolumes()
          .add(
              new VolumeBuilder()
                  .withName(volume.getName())
                  .withPersistentVolumeClaim(
                      new PersistentVolumeClaimVolumeSourceBuilder()
                          .withClaimName(volume.getName())
                          .build())
                  .build());

      PersistentVolumeClaim pluginsPVC =
          new PersistentVolumeClaimBuilder()
              .withNewMetadata()
              .withName(volume.getName())
              .endMetadata()
              .build();
      kubernetesEnvironment.getPersistentVolumeClaims().put(volume.getName(), pluginsPVC);
    }
  }
}
