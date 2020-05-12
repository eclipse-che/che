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

import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.singletonMap;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;

public class SecretAsMavenVolumeProvisioner<E extends KubernetesEnvironment> {

  public void provision(E env, KubernetesNamespace namespace) throws InfrastructureException {
    LabelSelector selector = new LabelSelector(new ArrayList<>(), singletonMap("app", "che"));
    List<Secret> secrets = namespace.secrets().get(selector);
    if (secrets.size() > 1) {
      throw new InfrastructureException("Too many secrets with label app:che");
    }
    Secret secret = secrets.get(0);
    final boolean mountAsEnv =
        parseBoolean(secret.getMetadata().getAnnotations().getOrDefault("useSecretAsEnv", "false"));
    final String targetContainerName = secret.getMetadata().getLabels().get("targetContainer");

    if (!mountAsEnv) {
      mountAsFile(env, secret, targetContainerName);
    } else {
      mountAsEnv(env, secret, targetContainerName);
    }
  }

  private void mountAsEnv(E env, Secret secret, String targetContainerName)
      throws InfrastructureException {
    final String mountEnvName = secret.getMetadata().getAnnotations().get("envName");
    if (mountEnvName == null) {
      throw new InfrastructureException(
          "Secret has to be mounted as Env, but env name is not specified.");
    }
    env.getPodsData()
        .values()
        .stream()
        .filter(pd -> pd.getRole().equals(PodRole.DEPLOYMENT))
        .forEach(
            pd ->
                pd.getSpec()
                    .getContainers()
                    .forEach(
                        c -> {
                          if (targetContainerName == null
                              || c.getName().equals(targetContainerName)) {
                            c.getEnv()
                                .add(
                                    new EnvVar(
                                        mountEnvName,
                                        secret.getData().get("settings.xml"),
                                        new EnvVarSource()));
                          }
                        }));
  }

  private void mountAsFile(E env, Secret secret, String targetContainerName) {
    final String mountPath =
        secret.getMetadata().getAnnotations().getOrDefault("mountPath", "/home/user/.m2/");

    Volume volumeFromSecret =
        new VolumeBuilder()
            .withName(secret.getMetadata().getName())
            .withSecret(
                new SecretVolumeSourceBuilder()
                    .withNewSecretName(secret.getMetadata().getName())
                    .build())
            .build();

    env.getPodsData()
        .values()
        .stream()
        .filter(pd -> pd.getRole().equals(PodRole.DEPLOYMENT))
        .forEach(
            pd -> {
              pd.getSpec().getVolumes().add(volumeFromSecret);
              pd.getSpec()
                  .getContainers()
                  .forEach(
                      c -> {
                        if (targetContainerName == null
                            || c.getName().equals(targetContainerName)) {
                          c.getVolumeMounts()
                              .add(
                                  new VolumeMountBuilder()
                                      .withName(volumeFromSecret.getName())
                                      .withMountPath(mountPath)
                                      .withReadOnly(true)
                                      .build());
                        }
                      });
            });
  }
}
