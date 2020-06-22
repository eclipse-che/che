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

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.SecretAsContainerResourceProvisioner.ANNOTATION_PREFIX;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretKeySelectorBuilder;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
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

  /**
   * Applies secret as environment variable into workspace containers, respecting automount
   * attribute and optional devfile automount property override.
   *
   * @param env kubernetes environment with workspace containers configuration
   * @param secret source secret to apply
   * @throws InfrastructureException on misconfigured secrets or other apply error
   */
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
        if ((component.isPresent() && isComponentAutomountFalse(component.get()))
            || (!secretAutomount
                && !(component.isPresent() && isComponentAutomountTrue(component.get())))) {
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
      mountEnvName =
          Stream.of(
                  secret.getMetadata().getAnnotations().get(ANNOTATION_ENV_NAME),
                  secret
                      .getMetadata()
                      .getAnnotations()
                      .get(format(ANNOTATION_ENV_NAME_TEMPLATE, key)))
              .filter(Objects::nonNull)
              .findFirst()
              .orElseThrow(
                  () ->
                      new InfrastructureException(
                          format(
                              "Unable to mount secret '%s': It is configured to be mount as a environment variable, but its name was not specified. Please define the '%s' annotation on the secret to specify it.",
                              secret.getMetadata().getName(), ANNOTATION_ENV_NAME)));
    } else {
      mountEnvName =
          secret.getMetadata().getAnnotations().get(format(ANNOTATION_ENV_NAME_TEMPLATE, key));
      if (mountEnvName == null) {
        throw new InfrastructureException(
            format(
                "Unable to mount key '%s'  of secret '%s': It is configured to be mount as a environment variable, but its was name not specified. Please define the '%s' annotation on the secret to specify it.",
                key, secret.getMetadata().getName(), format(ANNOTATION_ENV_NAME_TEMPLATE, key)));
      }
    }
    return mountEnvName;
  }
}
