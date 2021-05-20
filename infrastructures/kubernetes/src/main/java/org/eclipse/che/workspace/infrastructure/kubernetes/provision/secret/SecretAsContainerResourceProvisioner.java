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

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_GIT_CREDENTIALS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_MOUNT_AS;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import java.util.Arrays;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;

/**
 * Finds secrets with specific labels in namespace, and mount their values as file or environment
 * variable into workspace containers. Secrets annotated with "che.eclipse.org/mount-as=env" are
 * mount as env variables, env name is read from "che.eclipse.org/env-name" annotation. Secrets
 * which having "che.eclipse.org/mount-as=file" are mounted as file in the folder specified by
 * "che.eclipse.org/mount-path" annotation. Refer to che docs for concrete examples.
 */
@Beta
@Singleton
public class SecretAsContainerResourceProvisioner<E extends KubernetesEnvironment> {

  private final FileSecretApplier fileSecretApplier;
  private final EnvironmentVariableSecretApplier environmentVariableSecretApplier;

  private final GitCredentialStorageFileSecretApplier gitCredentialStorageFileSecretApplier;
  private final Map<String, String> secretLabels;

  @Inject
  public SecretAsContainerResourceProvisioner(
      FileSecretApplier fileSecretApplier,
      EnvironmentVariableSecretApplier environmentVariableSecretApplier,
      GitCredentialStorageFileSecretApplier gitCredentialStorageFileSecretApplier,
      @Named("che.workspace.provision.secret.labels") String[] labels) {
    this.fileSecretApplier = fileSecretApplier;
    this.environmentVariableSecretApplier = environmentVariableSecretApplier;
    this.gitCredentialStorageFileSecretApplier = gitCredentialStorageFileSecretApplier;
    this.secretLabels =
        Arrays.stream(labels)
            .map(item -> item.split("=", 2))
            .collect(toMap(p -> p[0], p -> p.length == 1 ? "" : p[1]));
  }

  public void provision(E env, RuntimeIdentity runtimeIdentity, KubernetesNamespace namespace)
      throws InfrastructureException {
    LabelSelector selector = new LabelSelectorBuilder().withMatchLabels(secretLabels).build();
    for (Secret secret : namespace.secrets().get(selector)) {
      if (secret.getMetadata().getAnnotations() == null) {
        throw new InfrastructureException(
            format(
                "Unable to mount secret '%s': it has missing required annotations. Please check documentation for secret format guide.",
                secret.getMetadata().getName()));
      }
      String mountType = secret.getMetadata().getAnnotations().get(ANNOTATION_MOUNT_AS);
      if ("env".equalsIgnoreCase(mountType)) {
        environmentVariableSecretApplier.applySecret(env, runtimeIdentity, secret);
      } else if ("file".equalsIgnoreCase(mountType)) {
        if (parseBoolean(secret.getMetadata().getAnnotations().get(ANNOTATION_GIT_CREDENTIALS))) {
          gitCredentialStorageFileSecretApplier.applySecret(env, runtimeIdentity, secret);
        } else {
          fileSecretApplier.applySecret(env, runtimeIdentity, secret);
        }

      } else {
        throw new InfrastructureException(
            format(
                "Unable to mount secret '%s': it has missing or unknown type of the mount. Please make sure that '%s' annotation has value either 'env' or 'file'.",
                secret.getMetadata().getName(), ANNOTATION_MOUNT_AS));
      }
    }
  }
}
