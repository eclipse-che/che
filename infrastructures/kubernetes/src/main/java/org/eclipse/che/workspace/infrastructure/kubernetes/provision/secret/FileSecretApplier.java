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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.SecretAsContainerResourceProvisioner.ANNOTATION_PREFIX;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.nio.file.Paths;
import java.util.Optional;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;

/**
 * Mounts Kubernetes secret with specific annotations as an file in workspace containers. Via
 * devfile, allows per-component control of secret applying and path overrides using specific
 * property.
 */
@Beta
@Singleton
public class FileSecretApplier extends KubernetesSecretApplier<KubernetesEnvironment> {

  static final String ANNOTATION_MOUNT_PATH = ANNOTATION_PREFIX + "/" + "mount-path";

  /**
   * Applies secret as file into workspace containers, respecting automount attribute and optional
   * devfile automount property and/or mount path override.
   *
   * @param env kubernetes environment with workspace containers configuration
   * @param secret source secret to apply
   * @throws InfrastructureException on misconfigured secrets or other apply error
   */
  @Override
  public void applySecret(KubernetesEnvironment env, Secret secret) throws InfrastructureException {
    String mountPath = secret.getMetadata().getAnnotations().get(ANNOTATION_MOUNT_PATH);
    boolean secretAutomount =
        Boolean.parseBoolean(secret.getMetadata().getAnnotations().get(ANNOTATION_AUTOMOUNT));
    if (mountPath == null) {
      throw new InfrastructureException(
          format(
              "Unable to mount secret '%s': It is configured to be mounted as a file but the mount path was not specified. Please define the '%s' annotation on the secret to specify it.",
              secret.getMetadata().getName(), ANNOTATION_MOUNT_PATH));
    }

    Volume volumeFromSecret =
        new VolumeBuilder()
            .withName(secret.getMetadata().getName())
            .withSecret(
                new SecretVolumeSourceBuilder()
                    .withNewSecretName(secret.getMetadata().getName())
                    .build())
            .build();

    for (PodData podData : env.getPodsData().values()) {
      if (!podData.getRole().equals(PodRole.DEPLOYMENT)) {
        continue;
      }
      if (podData
          .getSpec()
          .getVolumes()
          .stream()
          .anyMatch(v -> v.getName().equals(volumeFromSecret.getName()))) {
        volumeFromSecret.setName(volumeFromSecret.getName() + "_" + NameGenerator.generate("", 6));
      }

      podData.getSpec().getVolumes().add(volumeFromSecret);

      for (Container container : podData.getSpec().getContainers()) {
        Optional<ComponentImpl> component = getComponent(env, container.getName());
        if ((component.isPresent() && isComponentAutomountFalse(component.get()))
            || (!secretAutomount
                && !(component.isPresent() && isComponentAutomountTrue(component.get())))) {
          continue;
        }
        // find path override if any
        if (component.isPresent()) {
          Optional<VolumeImpl> matchedVolume =
              component
                  .get()
                  .getVolumes()
                  .stream()
                  .filter(v -> v.getName().equals(secret.getMetadata().getName()))
                  .findFirst();

          if (matchedVolume.isPresent() && !isNullOrEmpty(matchedVolume.get().getContainerPath())) {
            mountPath = matchedVolume.get().getContainerPath();
          }
        }

        String finalMountPath = mountPath;
        container
            .getVolumeMounts()
            .removeIf(vm -> Paths.get(vm.getMountPath()).equals(Paths.get(finalMountPath)));

        secret
            .getData()
            .keySet()
            .forEach(
                secretFile ->
                    container
                        .getVolumeMounts()
                        .add(
                            new VolumeMountBuilder()
                                .withName(volumeFromSecret.getName())
                                .withMountPath(finalMountPath + "/" + secretFile)
                                .withSubPath(secretFile)
                                .withReadOnly(true)
                                .build()));
      }
    }
  }
}
