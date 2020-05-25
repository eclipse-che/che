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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretKeySelectorBuilder;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;

/**
 * Finds secrets with specific labels in namespace, and mount their values as file or environment
 * variable into all (or specified by "org.eclipse.che/target-container" annotation) workspace containers.
 * Secrets with annotation "org.eclipse.che/mount-as=env" are mount as env variables, env name is read from
 * "org.eclipse.che/env-name" annotation. Secrets which don't have it, are mounted as file in the
 * folder specified by "org.eclipse.che/mount-path" annotation. Refer to che-docs for concrete examples.
 */
@Beta
@Singleton
public class SecretAsContainerResourceProvisioner<E extends KubernetesEnvironment> {

   private static final String ANNOTATION_PREFIX = "org.eclipse.che";
   static final String ANNOTATION_MOUNT_AS = ANNOTATION_PREFIX + "/" + "mount-as";
   static final String ANNOTATION_TARGET_CONTAINER = ANNOTATION_PREFIX + "/" + "target-container";
   static final String ANNOTATION_ENV_NAME = ANNOTATION_PREFIX + "/" + "env-name";
   static final String ANNOTATION_ENV_NAME_TEMPLATE = ANNOTATION_PREFIX + "/%s_" + "env-name";
   static final String ANNOTATION_MOUNT_PATH = ANNOTATION_PREFIX + "/" + "mount-path";

  private final Map<String, String> secretLabels;

  @Inject
  public SecretAsContainerResourceProvisioner(
      @Named("che.workspace.provision.secret.labels") String[] labels) {
    this.secretLabels = new HashMap<>();
    for (String keyValue : labels) {
      String[] pair = keyValue.split("=", 2);
      secretLabels.put(pair[0], pair.length == 1 ? "" : pair[1]);
    }
  }

  public void provision(E env, KubernetesNamespace namespace) throws InfrastructureException {
    LabelSelector selector = new LabelSelectorBuilder().withMatchLabels(secretLabels).build();
    for (Secret secret : namespace.secrets().get(selector)) {
      boolean mountAsEnv =
          secret.getMetadata().getAnnotations().getOrDefault(ANNOTATION_MOUNT_AS, "file")
              .equals("env");
      String targetContainerName = secret.getMetadata().getAnnotations().get(ANNOTATION_TARGET_CONTAINER);
      if (mountAsEnv) {
        mountAsEnv(env, secret, targetContainerName);
      } else {
        mountAsFile(env, secret, targetContainerName);
      }
    }
  }

  private void mountAsEnv(E env, Secret secret, String targetContainerName)
      throws InfrastructureException {
    for (PodData pd : env.getPodsData().values()) {
      if (!pd.getRole().equals(PodRole.DEPLOYMENT)) {
        continue;
      }
      for (Container c : pd.getSpec().getContainers()) {
        if (targetContainerName != null && !c.getName().equals(targetContainerName)) {
          continue;
        }
        for (Entry<String, String> entry : secret.getData().entrySet()) {
          final String mountEnvName = envName(secret, entry.getKey());
          c.getEnv()
              .add(
                  new EnvVarBuilder()
                      .withName(mountEnvName)
                      .withValueFrom(
                          new EnvVarSourceBuilder()
                              .withSecretKeyRef(
                                  new SecretKeySelectorBuilder()
                                      .withName(secret.getMetadata().getName())
                                      .withKey(entry.getKey())
                                      .build())
                              .build())
                      .build());
        }
      }
    }
  }

  private void mountAsFile(E env, Secret secret, String targetContainerName)
      throws InfrastructureException {

    final String mountPath = secret.getMetadata().getAnnotations().get(ANNOTATION_MOUNT_PATH);
    if (mountPath == null) {
      throw new InfrastructureException(
          format(
              "Unable to mount secret '%s': it has to be mounted as file, but mountPath is not specified."
                  + "Please make sure you specified '%s' annotation of secret.",
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

    for (PodData pd : env.getPodsData().values()) {
      if (!pd.getRole().equals(PodRole.DEPLOYMENT)) {
        continue;
      }
      if (pd.getSpec()
          .getVolumes()
          .stream()
          .anyMatch(v -> v.getName().equals(volumeFromSecret.getName()))) {
        throw new InfrastructureException(
            format(
                "Volume name '%s' provisioned from secret, clashes with existing volume name.",
                volumeFromSecret.getName()));
      }

      pd.getSpec().getVolumes().add(volumeFromSecret);

      for (Container c : pd.getSpec().getContainers()) {
        if (targetContainerName != null && !c.getName().equals(targetContainerName)) {
          continue;
        }
        if (c.getVolumeMounts().stream().anyMatch(vm -> vm.getMountPath().equals(mountPath))) {
          throw new InfrastructureException(
              format(
                  "Volume Mount path '%s' provisioned from secret, clashes with existing volume mount path.",
                  mountPath));
        }
        c.getVolumeMounts()
            .add(
                new VolumeMountBuilder()
                    .withName(volumeFromSecret.getName())
                    .withMountPath(mountPath)
                    .withReadOnly(true)
                    .build());
      }
    }
  }

  private String envName(Secret secret, String key) throws InfrastructureException {
    String mountEnvName;
    if (secret.getData().size() == 1) {
      try {
        mountEnvName =
            firstNonNull(
                secret.getMetadata().getAnnotations().get(ANNOTATION_ENV_NAME),
                secret.getMetadata().getAnnotations().get(format(ANNOTATION_ENV_NAME_TEMPLATE, key)));
      } catch (NullPointerException e) {
        throw new InfrastructureException(
            format(
                "Unable to mount secret '%s': it has to be mounted as Env, but env name is not specified."
                    + "Please make sure you specified '%s' annotation of secret.",
                secret.getMetadata().getName(), ANNOTATION_ENV_NAME));
      }
    } else {
      mountEnvName = secret.getMetadata().getAnnotations().get(format(ANNOTATION_ENV_NAME_TEMPLATE, key));
      if (mountEnvName == null) {
        throw new InfrastructureException(
            format(
                "Unable to mount key '%s' of secret '%s': it has to be mounted as Env, but env name is not specified."
                    + "Please make sure you specified '%s' annotation of secret.",
                key, secret.getMetadata().getName(), format(ANNOTATION_ENV_NAME_TEMPLATE, key)));
      }
    }
    return mountEnvName;
  }
}
