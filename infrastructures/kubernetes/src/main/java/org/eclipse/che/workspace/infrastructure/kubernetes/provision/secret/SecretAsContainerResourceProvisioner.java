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
import static java.util.stream.Collectors.toMap;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import java.util.Arrays;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;

/**
 * Finds secrets with specific labels in namespace, and mount their values as file or environment
 * variable into all (or specified by "org.eclipse.che/target-container" annotation) workspace
 * containers. Secrets with annotation "org.eclipse.che/mount-as=env" are mount as env variables,
 * env name is read from "org.eclipse.che/env-name" annotation. Secrets which don't have
 * "org.eclipse.che/mount-as=env" or having "org.eclipse.che/mount-as=file" are mounted as file in
 * the folder specified by "org.eclipse.che/mount-path" annotation. Refer to che-docs for concrete
 * examples.
 */
@Beta
@Singleton
public class SecretAsContainerResourceProvisioner<E extends KubernetesEnvironment> {

  static final String ANNOTATION_PREFIX = "che.eclipse.org";
  static final String ANNOTATION_MOUNT_AS = ANNOTATION_PREFIX + "/" + "mount-as";
  private final FileSecretApplier fileSecretApplier;
  private final EnvironmentVariableSecretApplier environmentVariableSecretApplier;

  private final Map<String, String> secretLabels;

  @Inject
  public SecretAsContainerResourceProvisioner(
      FileSecretApplier fileSecretApplier,
      EnvironmentVariableSecretApplier environmentVariableSecretApplier,
      @Named("che.workspace.provision.secret.labels") String[] labels) {
    this.fileSecretApplier = fileSecretApplier;
    this.environmentVariableSecretApplier = environmentVariableSecretApplier;
    this.secretLabels =
        Arrays.stream(labels)
            .map(item -> item.split("=", 2))
            .collect(toMap(p -> p[0], p -> p.length == 1 ? "" : p[1]));
  }

  public void provision(E env, KubernetesNamespace namespace) throws InfrastructureException {
    LabelSelector selector = new LabelSelectorBuilder().withMatchLabels(secretLabels).build();
    for (Secret secret : namespace.secrets().get(selector)) {
      String mountType = secret.getMetadata().getAnnotations().get(ANNOTATION_MOUNT_AS);
      if ("env".equalsIgnoreCase(mountType)) {
        environmentVariableSecretApplier.applySecret(env, secret);
      } else if ("file".equalsIgnoreCase(mountType)) {
        fileSecretApplier.applySecret(env, secret);
      } else {
        throw new InfrastructureException(
            format(
                "Unable to mount secret '%s': it has missing or unknown type of the mount. Please make sure that '%s' annotation has value either 'env' or 'file'.",
                secret.getMetadata().getName(), ANNOTATION_MOUNT_AS));
      }
    }
  }
}
