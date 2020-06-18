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

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.SecretAsContainerResourceProvisioner.ANNOTATION_PREFIX;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretKeySelectorBuilder;
import java.util.Map.Entry;
import java.util.Optional;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;

/**
 * Mounts Kubernetes secret with specific annotations as an environment variable(s) in workspace
 * containers. Allows per-component control of secret applying in devfile.
 */
@Beta
@Singleton
public class EnvironmentVariableSecretApplier
    extends KubernetesSecretApplier<KubernetesEnvironment> {

  static final String ANNOTATION_ENV_NAME = ANNOTATION_PREFIX + "/" + "env-name";
  static final String ANNOTATION_ENV_NAME_TEMPLATE = ANNOTATION_PREFIX + "/%s_" + "env-name";

  @Override
  public void applySecret(KubernetesEnvironment env, Secret secret) throws InfrastructureException {
    boolean secretAutomount =
        Boolean.parseBoolean(secret.getMetadata().getAnnotations().get(ANNOTATION_AUTOMOUNT));
    for (PodData podData : env.getPodsData().values()) {
      if (!podData.getRole().equals(PodRole.DEPLOYMENT)) {
        continue;
      }
      for (Container container : podData.getSpec().getContainers()) {
        Optional<ComponentImpl> component = getComponent(env, container.getName());
        if ((component.isPresent() && isOverridenByFalse(component.get()))
            || (!secretAutomount
                && !(component.isPresent() && isOverridenByTrue(component.get())))) {
          continue;
        }
        for (Entry<String, String> secretDataEntry : secret.getData().entrySet()) {
          final String mountEnvName = envName(secret, secretDataEntry.getKey());
          container
              .getEnv()
              .add(
                  new EnvVarBuilder()
                      .withName(mountEnvName)
                      .withValueFrom(
                          new EnvVarSourceBuilder()
                              .withSecretKeyRef(
                                  new SecretKeySelectorBuilder()
                                      .withName(secret.getMetadata().getName())
                                      .withKey(secretDataEntry.getKey())
                                      .build())
                              .build())
                      .build());
        }
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
                secret
                    .getMetadata()
                    .getAnnotations()
                    .get(format(ANNOTATION_ENV_NAME_TEMPLATE, key)));
      } catch (NullPointerException e) {
        throw new InfrastructureException(
            format(
                "Unable to mount secret '%s': It is configured to be mount as a environment variable, but its was not specified. Please define the '%s' annotation on the secret to specify it.",
                secret.getMetadata().getName(), ANNOTATION_ENV_NAME));
      }
    } else {
      mountEnvName =
          secret.getMetadata().getAnnotations().get(format(ANNOTATION_ENV_NAME_TEMPLATE, key));
      if (mountEnvName == null) {
        throw new InfrastructureException(
            format(
                "Unable to mount key '%s'  of secret '%s': It is configured to be mount as a environment variable, but its was not specified. Please define the '%s' annotation on the secret to specify it.",
                key, secret.getMetadata().getName(), format(ANNOTATION_ENV_NAME_TEMPLATE, key)));
      }
    }
    return mountEnvName;
  }
}
