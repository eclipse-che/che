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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.factory.server.scm.PersonalAccessToken;
import org.eclipse.che.api.factory.server.scm.PersonalAccessTokenManager;
import org.eclipse.che.api.factory.server.scm.ScmPersonalAccessTokenFetcher;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmConfigurationPersistenceException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.UnknownScmProviderException;
import org.eclipse.che.api.factory.server.scm.exception.UnsatisfiedScmPreconditionException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.StringUtils;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

/** Manages personal access token secrets used for private repositories authentication. */
@Singleton
public class KubernetesPersonalAccessTokenManager implements PersonalAccessTokenManager {
  public static final Map<String, String> SECRET_LABELS =
      ImmutableMap.of(
          "app.kubernetes.io/part-of", "che.eclipse.org",
          "app.kubernetes.io/component", "scm-personal-access-token");
  public static final LabelSelector KUBERNETES_PERSONAL_ACCESS_TOKEN_LABEL_SELECTOR =
      new LabelSelectorBuilder().withMatchLabels(SECRET_LABELS).build();

  public static final String NAME_PATTERN = "personal-access-token-";

  public static final String ANNOTATION_CHE_USERID = "che.eclipse.org/che-userid";
  public static final String ANNOTATION_SCM_USERID = "che.eclipse.org/scm-userid";
  public static final String ANNOTATION_SCM_USERNAME = "che.eclipse.org/scm-username";
  public static final String ANNOTATION_SCM_PERSONAL_ACCESS_TOKEN_ID =
      "che.eclipse.org/scm-personal-access-token-id";
  public static final String ANNOTATION_SCM_PERSONAL_ACCESS_TOKEN_NAME =
      "che.eclipse.org/scm-personal-access-token-name";
  public static final String ANNOTATION_SCM_URL = "che.eclipse.org/scm-url";
  public static final String TOKEN_DATA_FIELD = "token";

  private final KubernetesNamespaceFactory namespaceFactory;
  private final KubernetesClientFactory clientFactory;
  private final ScmPersonalAccessTokenFetcher scmPersonalAccessTokenFetcher;

  @Inject
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
      throws UnsatisfiedScmPreconditionException, ScmConfigurationPersistenceException {
    try {
      String namespace = getFirstNamespace();
      ObjectMeta meta =
          new ObjectMetaBuilder()
              .withName(NameGenerator.generate(NAME_PATTERN, 5))
              .withAnnotations(
                  new ImmutableMap.Builder<String, String>()
                      .put(ANNOTATION_CHE_USERID, personalAccessToken.getCheUserId())
                      .put(ANNOTATION_SCM_USERID, personalAccessToken.getScmUserId())
                      .put(ANNOTATION_SCM_USERNAME, personalAccessToken.getScmUserName())
                      .put(ANNOTATION_SCM_URL, personalAccessToken.getScmProviderUrl())
                      .put(
                          ANNOTATION_SCM_PERSONAL_ACCESS_TOKEN_ID,
                          personalAccessToken.getScmTokenId())
                      .put(
                          ANNOTATION_SCM_PERSONAL_ACCESS_TOKEN_NAME,
                          personalAccessToken.getScmTokenName())
                      .build())
              .withLabels(SECRET_LABELS)
              .build();

      Secret secret =
          new SecretBuilder()
              .withMetadata(meta)
              .withData(
                  Map.of(
                      TOKEN_DATA_FIELD,
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
  public PersonalAccessToken fetchAndSave(Subject cheUser, String scmServerUrl)
      throws UnsatisfiedScmPreconditionException, ScmConfigurationPersistenceException,
          ScmUnauthorizedException, ScmCommunicationException, UnknownScmProviderException {
    PersonalAccessToken personalAccessToken =
        scmPersonalAccessTokenFetcher.fetchPersonalAccessToken(cheUser, scmServerUrl);
    save(personalAccessToken);
    return personalAccessToken;
  }

  @Override
  public Optional<PersonalAccessToken> get(Subject cheUser, String scmServerUrl)
      throws ScmConfigurationPersistenceException, ScmUnauthorizedException,
          ScmCommunicationException {

    try {
      for (KubernetesNamespaceMeta namespaceMeta : namespaceFactory.list()) {
        List<Secret> secrets =
            namespaceFactory
                .access(null, namespaceMeta.getName())
                .secrets()
                .get(KUBERNETES_PERSONAL_ACCESS_TOKEN_LABEL_SELECTOR);
        for (Secret secret : secrets) {
          Map<String, String> annotations = secret.getMetadata().getAnnotations();
          if (annotations.get(ANNOTATION_CHE_USERID).equals(cheUser.getUserId())
              && StringUtils.trimEnd(annotations.get(ANNOTATION_SCM_URL), '/')
                  .equals(StringUtils.trimEnd(scmServerUrl, '/'))) {
            PersonalAccessToken token =
                new PersonalAccessToken(
                    annotations.get(ANNOTATION_SCM_URL),
                    annotations.get(ANNOTATION_CHE_USERID),
                    annotations.get(ANNOTATION_SCM_USERNAME),
                    annotations.get(ANNOTATION_SCM_USERID),
                    annotations.get(ANNOTATION_SCM_PERSONAL_ACCESS_TOKEN_NAME),
                    annotations.get(ANNOTATION_SCM_PERSONAL_ACCESS_TOKEN_ID),
                    new String(Base64.getDecoder().decode(secret.getData().get("token"))));
            if (scmPersonalAccessTokenFetcher.isValid(token)) {
              return Optional.of(token);
            } else {
              // Removing token that is no longer valid. If several tokens exist the next one could
              // be valid. If no valid token can be found, the caller should react in the same way
              // as it reacts if no token exists. Usually, that means that process of new token
              // retrieval would be initiated.
              clientFactory.create().secrets().inNamespace(namespaceMeta.getName()).delete(secret);
            }
          }
        }
      }
    } catch (InfrastructureException | UnknownScmProviderException e) {
      throw new ScmConfigurationPersistenceException(e.getMessage(), e);
    }
    return Optional.empty();
  }

  private String getFirstNamespace()
      throws UnsatisfiedScmPreconditionException, ScmConfigurationPersistenceException {
    try {
      return namespaceFactory
          .list()
          .stream()
          .map(KubernetesNamespaceMeta::getName)
          .findFirst()
          .orElseThrow(
              () ->
                  new UnsatisfiedScmPreconditionException(
                      "No user namespace found. Cannot read SCM credentials."));
    } catch (InfrastructureException e) {
      throw new ScmConfigurationPersistenceException(e.getMessage(), e);
    }
  }
}
