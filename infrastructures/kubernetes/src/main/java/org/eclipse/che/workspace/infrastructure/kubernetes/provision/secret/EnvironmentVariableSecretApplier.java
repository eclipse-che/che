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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_AUTOMOUNT;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_ENV_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_ENV_NAME_TEMPLATE;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretKeySelectorBuilder;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mounts Kubernetes secret with specific annotations as an environment variable(s) in workspace
 * containers. Allows per-component control of secret applying in devfile.
 */
@Beta
@Singleton
public class EnvironmentVariableSecretApplier
    extends KubernetesSecretApplier<KubernetesEnvironment> {

  @Inject private RuntimeEventsPublisher runtimeEventsPublisher;

  private static final Logger LOG = LoggerFactory.getLogger(EnvironmentVariableSecretApplier.class);

  /**
   * Applies secret as environment variable into workspace containers, respecting automount
   * attribute and optional devfile automount property override.
   *
   * @param env kubernetes environment with workspace containers configuration
   * @param runtimeIdentity identity of current runtime
   * @param secret source secret to apply
   * @throws InfrastructureException on misconfigured secrets or other apply error
   */
  @Override
  public void applySecret(KubernetesEnvironment env, RuntimeIdentity runtimeIdentity, Secret secret)
      throws InfrastructureException {
    boolean secretAutomount =
        Boolean.parseBoolean(secret.getMetadata().getAnnotations().get(ANNOTATION_AUTOMOUNT));
    for (PodData podData : env.getPodsData().values()) {
      if (!podData.getRole().equals(PodRole.DEPLOYMENT)) {
        continue;
      }
      for (Container container : podData.getSpec().getContainers()) {
        Optional<ComponentImpl> component = getComponent(env, container.getName());
        // skip components that explicitly disable automount
        if (component.isPresent() && isComponentAutomountFalse(component.get())) {
          continue;
        }
        // if automount disabled globally and not overridden in component
        if (!secretAutomount
            && (!component.isPresent() || !isComponentAutomountTrue(component.get()))) {
          continue;
        }
        for (Entry<String, String> secretDataEntry : secret.getData().entrySet()) {
          final String mountEnvName = envName(secret, secretDataEntry.getKey(), runtimeIdentity);
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

  private String envName(Secret secret, String key, RuntimeIdentity runtimeIdentity)
      throws InfrastructureException {
    String mountEnvName;
    if (secret.getData().size() == 1) {
      List<String> providedNames =
          Stream.of(
                  secret.getMetadata().getAnnotations().get(ANNOTATION_ENV_NAME),
                  secret
                      .getMetadata()
                      .getAnnotations()
                      .get(format(ANNOTATION_ENV_NAME_TEMPLATE, key)))
              .filter(Objects::nonNull)
              .collect(Collectors.toList());
      if (providedNames.isEmpty()) {
        throw new InfrastructureException(
            format(
                "Unable to mount secret '%s': It is configured to be mount as a environment variable, but its name was not specified. Please define the '%s' annotation on the secret to specify it.",
                secret.getMetadata().getName(), ANNOTATION_ENV_NAME));
      }

      if (providedNames.size() > 1) {
        String msg =
            String.format(
                "Secret '%s' defines multiple environment variable name annotations, but contains only one data entry. That may cause inconsistent behavior and needs to be corrected.",
                secret.getMetadata().getName());
        LOG.warn(msg);
        runtimeEventsPublisher.sendRuntimeLogEvent(
            msg, ZonedDateTime.now().toString(), runtimeIdentity);
      }
      mountEnvName = providedNames.get(0);
    } else {
      mountEnvName =
          secret.getMetadata().getAnnotations().get(format(ANNOTATION_ENV_NAME_TEMPLATE, key));
      if (mountEnvName == null) {
        throw new InfrastructureException(
            format(
                "Unable to mount key '%s'  of secret '%s': It is configured to be mount as a environment variable, but its name was not specified. Please define the '%s' annotation on the secret to specify it.",
                key, secret.getMetadata().getName(), format(ANNOTATION_ENV_NAME_TEMPLATE, key)));
      }
    }
    return mountEnvName;
  }
}
