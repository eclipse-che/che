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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.factory.server.scm.PersonalAccessToken;
import org.eclipse.che.api.factory.server.scm.PersonalAccessTokenManager;
import org.eclipse.che.api.factory.server.scm.ScmPersonalAccessTokenFetcher;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmConfigurationPersistenceException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.UnknownScmProviderException;
import org.eclipse.che.api.factory.server.scm.exception.UnsatisfiedPreconditionException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

public class KubernetesPersonalAccessTokenManager implements PersonalAccessTokenManager {
  public static final Map<String, String> SECRET_LABELS =
      Map.of(
          "app.kubernetes.io/part-of",
          "che.eclipse.org",
          "app.kubernetes.io/component",
          "scm-personal-access-token");
  public static final LabelSelector KPAT_LABEL_SELECTOR =
      new LabelSelectorBuilder().withMatchLabels(SECRET_LABELS).build();

  public static final String NAME_PATTERN = "%s-personal-access-token";

  public static final String ANNOTATION_CHE_USERID = "che.eclipse.org/che-userid";
  public static final String ANNOTATION_SCM_USERID = "che.eclipse.org/scm-userid";
  public static final String ANNOTATION_SCM_USERNAME = "che.eclipse.org/scm-username";
  public static final String ANNOTATION_SCM_URL = "che.eclipse.org/scm-url";

  private final KubernetesNamespaceFactory namespaceFactory;
  private final KubernetesClientFactory clientFactory;
  private final ScmPersonalAccessTokenFetcher scmPersonalAccessTokenFetcher;

  public KubernetesPersonalAccessTokenManager(
      KubernetesNamespaceFactory namespaceFactory,
      KubernetesClientFactory clientFactory,
      ScmPersonalAccessTokenFetcher scmPersonalAccessTokenFetcher) {
    this.namespaceFactory = namespaceFactory;
    this.clientFactory = clientFactory;
    this.scmPersonalAccessTokenFetcher = scmPersonalAccessTokenFetcher;
  }

  @VisibleForTesting
  void save(PersonalAccessToken personalAccessToken)
      throws UnsatisfiedPreconditionException, ScmConfigurationPersistenceException {
    try {
      String namespace = getFirstNamespace();
      ObjectMeta meta =
          new ObjectMetaBuilder()
              .withName(String.format(NAME_PATTERN, personalAccessToken.getScmProviderHost()))
              .withAnnotations(
                  ImmutableMap.of(
                      ANNOTATION_CHE_USERID,
                      personalAccessToken.getCheUserId(),
                      ANNOTATION_SCM_USERID,
                      personalAccessToken.getUserId(),
                      ANNOTATION_SCM_USERNAME,
                      personalAccessToken.getUserName(),
                      ANNOTATION_SCM_URL,
                      personalAccessToken.getScmProviderUrl()))
              .withLabels(SECRET_LABELS)
              .build();

      Secret secret =
          new SecretBuilder()
              .withApiVersion("1")
              .withMetadata(meta)
              .withData(
                  Map.of(
                      "token",
                      Base64.getEncoder()
                          .encodeToString(
                              personalAccessToken.getToken().getBytes(StandardCharsets.UTF_8))))
              .build();

      clientFactory.create().secrets().inNamespace(namespace).createOrReplace(secret);
    } catch (KubernetesClientException | InfrastructureException e) {
      throw new ScmConfigurationPersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public PersonalAccessToken fetchAndSave(String cheUserId, String scmServerUrl)
      throws UnsatisfiedPreconditionException, ScmConfigurationPersistenceException,
          ScmUnauthorizedException, ScmCommunicationException, UnknownScmProviderException {
    PersonalAccessToken personalAccessToken =
        scmPersonalAccessTokenFetcher.fetchPersonalAccessToken(cheUserId, scmServerUrl);
    save(personalAccessToken);
    return personalAccessToken;
  }

  @Override
  public Optional<PersonalAccessToken> get(String cheUserId, String scmServerUrl)
      throws ScmConfigurationPersistenceException {

    try {
      for (KubernetesNamespaceMeta namespaceMeta : namespaceFactory.list()) {
        List<Secret> secrets =
            namespaceFactory
                .access(null, namespaceMeta.getName())
                .secrets()
                .get(KPAT_LABEL_SELECTOR);
        for (Secret secret : secrets) {
          Map<String, String> annotations = secret.getMetadata().getAnnotations();
          if (annotations.get(ANNOTATION_CHE_USERID).equals(cheUserId)
              && annotations.get(ANNOTATION_SCM_URL).equals(scmServerUrl)) {
            return Optional.of(
                new PersonalAccessToken(
                    annotations.get(ANNOTATION_SCM_URL),
                    annotations.get(ANNOTATION_CHE_USERID),
                    annotations.get(ANNOTATION_SCM_USERNAME),
                    annotations.get(ANNOTATION_SCM_USERID),
                    new String(Base64.getDecoder().decode(secret.getData().get("token")))));
          }
        }
      }
    } catch (InfrastructureException e) {
      throw new ScmConfigurationPersistenceException(e.getMessage(), e);
    }
    return Optional.empty();
  }

  //  @Override
  //  public void delete(String cheUserId, String scmServerUrl)
  //      throws UnsatisfiedCondition, ScmConfigurationPersistenceException,
  //          InvalidScmProviderUrlException {
  //    try {
  //      String namespace = getFirstNamespace();
  //      URL scmUrl = new URL(scmServerUrl);
  //      clientFactory
  //          .create()
  //          .secrets()
  //          .inNamespace(namespace)
  //          .withName(String.format(NAME_PATTERN, scmUrl.getHost()))
  //          .withPropagationPolicy("Background")
  //          .delete();
  //    } catch (KubernetesClientException | InfrastructureException e) {
  //      throw new ScmConfigurationPersistenceException(e.getMessage(), e);
  //    } catch (MalformedURLException e) {
  //      throw new InvalidScmProviderUrlException(e.getMessage());
  //    }
  //  }

  private String getFirstNamespace()
      throws UnsatisfiedPreconditionException, ScmConfigurationPersistenceException {
    try {
      Optional<String> namespace =
          namespaceFactory.list().stream().map(m -> m.getName()).findFirst();
      if (namespace.isEmpty()) {
        throw new UnsatisfiedPreconditionException("No namespace found");
      }
      return namespace.get();
    } catch (InfrastructureException e) {
      throw new ScmConfigurationPersistenceException(e.getMessage(), e);
    }
  }
}
