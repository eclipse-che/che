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
package org.eclipse.che.api.factory.server.scm.kubernetes;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_AUTOMOUNT;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_GIT_CREDENTIALS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_MOUNT_AS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_MOUNT_PATH;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.eclipse.che.api.factory.server.scm.GitCredentialManager;
import org.eclipse.che.api.factory.server.scm.PersonalAccessToken;
import org.eclipse.che.api.factory.server.scm.exception.ScmConfigurationPersistenceException;
import org.eclipse.che.api.factory.server.scm.exception.UnsatisfiedPreconditionException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

public class KubernetesGitCredentialManager implements GitCredentialManager {
  public static final String NAME_PATTERN = "%s-git-credentials-secret";

  private static final Map<String, String> LABELS =
      ImmutableMap.of(
          "app.kubernetes.io/part-of", "che.eclipse.org",
          "app.kubernetes.io/component", "workspace-secret");

  private static final Map<String, String> ANNOTATIONS =
      ImmutableMap.of(
          ANNOTATION_AUTOMOUNT,
          "true",
          ANNOTATION_MOUNT_PATH,
          "/home/theia/.git-credentials",
          ANNOTATION_MOUNT_AS,
          "file",
          ANNOTATION_GIT_CREDENTIALS,
          "true");

  private final KubernetesNamespaceFactory namespaceFactory;
  private final KubernetesClientFactory clientFactory;

  @Inject
  public KubernetesGitCredentialManager(
      KubernetesNamespaceFactory namespaceFactory, KubernetesClientFactory clientFactory) {
    this.namespaceFactory = namespaceFactory;
    this.clientFactory = clientFactory;
  }

  @Override
  public void createOrReplace(PersonalAccessToken personalAccessToken)
      throws UnsatisfiedPreconditionException, ScmConfigurationPersistenceException {
    try {
      String namespace = getFirstNamespace();
      ObjectMeta meta =
          new ObjectMetaBuilder()
              .withName(String.format(NAME_PATTERN, personalAccessToken.getScmProviderHost()))
              .withAnnotations(ANNOTATIONS)
              .withLabels(LABELS)
              .build();

      Secret secret =
          new SecretBuilder()
              .withApiVersion("1")
              .withMetadata(meta)
              .withData(
                  Map.of(
                      "credentials",
                      Base64.getEncoder()
                          .encodeToString(
                              format(
                                      "%s://%s:%s@%s",
                                      personalAccessToken.getScmProviderProtocol(),
                                      personalAccessToken.getUserName(),
                                      personalAccessToken.getToken(),
                                      personalAccessToken.getScmProviderHost())
                                  .getBytes())))
              .build();
      clientFactory.create().secrets().inNamespace(namespace).createOrReplace(secret);
    } catch (InfrastructureException e) {
      throw new ScmConfigurationPersistenceException(e.getMessage(), e);
    }
  }

  private String getFirstNamespace()
      throws UnsatisfiedPreconditionException, ScmConfigurationPersistenceException {
    try {
      Optional<String> namespace =
          namespaceFactory.list().stream().map(KubernetesNamespaceMeta::getName).findFirst();
      if (namespace.isEmpty()) {
        throw new UnsatisfiedPreconditionException("No namespace found");
      }
      return namespace.get();
    } catch (InfrastructureException e) {
      throw new ScmConfigurationPersistenceException(e.getMessage(), e);
    }
  }
}
