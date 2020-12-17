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
package org.eclipse.che.api.factory.server.bitbucket;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_PREFIX;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

public class BitbucketServerAuthorizingFileContentProvider implements FileContentProvider {

  public static final String BITBUCKET_HOSTNAME_ANNOTATION = ANNOTATION_PREFIX + "/bitbucket-hostname";
  public static final String BITBUCKET_USERNAME_ANNOTATION = ANNOTATION_PREFIX + "/bitbucket-username";
  public static final String CHE_USERID_ANNOTATION = ANNOTATION_PREFIX+ "/che-userid";
  private final URLFetcher urlFetcher;
  private final BitbucketUrl bitbucketUrl;
  private final KubernetesNamespaceFactory namespaceFactory;
  private final KubernetesClientFactory clientFactory;
  private final BitbucketServerGitCredentialsSecretProvisioner gitCredentialsSecretProvisioner;

  public BitbucketServerAuthorizingFileContentProvider(
      BitbucketUrl bitbucketUrl,
      URLFetcher urlFetcher,
      KubernetesNamespaceFactory namespaceFactory,
      KubernetesClientFactory clientFactory,
      BitbucketServerGitCredentialsSecretProvisioner gitCredentialsSecretProvisioner) {
    this.bitbucketUrl = bitbucketUrl;
    this.urlFetcher = urlFetcher;
    this.namespaceFactory = namespaceFactory;
    this.clientFactory = clientFactory;
    this.gitCredentialsSecretProvisioner = gitCredentialsSecretProvisioner;
  }

  @Override
  public String fetchContent(String fileURL) throws IOException, DevfileException {
    URI fileURI;
    String requestURL;
    try {
      fileURI = new URI(fileURL);
    } catch (URISyntaxException e) {
      throw new DevfileException(e.getMessage(), e);
    }

    if (fileURI.isAbsolute()) {
      requestURL = fileURL;
    } else {
      // check me
      try {
        requestURL = new URI(bitbucketUrl.rawFileLocation("test.file")).resolve(fileURI).toString();
      } catch (URISyntaxException e) {
        throw new DevfileException(e.getMessage(), e);
      }
    }

    Optional<String> token =
        readTokenSecret(
            bitbucketUrl.getHostName(), EnvironmentContext.getCurrent().getSubject().getUserId());
    try {
      return token.isPresent()
          ? urlFetcher.fetch(requestURL, "Bearer " + token.get())
          : urlFetcher.fetch(requestURL);
    } catch (IOException e) {
      throw new IOException(
          format(
              "Failed to fetch a content from URL %s. Make sure the URL"
                  + " is correct. Additionally, if you're using "
                  + " relative form, make sure the referenced files are actually stored"
                  + " relative to the devfile on the same host,"
                  + " or try to specify URL in absolute form. The current attempt to download"
                  + " the file failed with the following error message: %s",
              fileURL, e.getMessage()),
          e);
    }
  }

  private Optional<String> readTokenSecret(String hostname, String cheUserId)
      throws DevfileException {
    List<KubernetesNamespaceMeta> availableNamespaces;
    KubernetesClient client;
    try {
      availableNamespaces = namespaceFactory.list();
      client = clientFactory.create();
    } catch (InfrastructureException e) {
      throw new DevfileException("Unable to read user namespaces. Cause is:", e);
    }
    for (KubernetesNamespaceMeta meta : availableNamespaces) {
      List<Secret> bitbucketTokenSecrets =
          client
              .secrets()
              .inNamespace(meta.getName())
              .withLabel("app.kubernetes.io/component", "bitbucket-personal-access-token")
              .list()
              .getItems();
      if (bitbucketTokenSecrets.isEmpty()) {
        continue;
      }
      Optional<Secret> matchedSecret =
          bitbucketTokenSecrets
              .stream()
              .filter(
                  s ->
                      s.getMetadata()
                              .getAnnotations()
                              .get(BITBUCKET_HOSTNAME_ANNOTATION)
                              .equals(hostname)
                          && s.getMetadata()
                              .getAnnotations()
                              .get(CHE_USERID_ANNOTATION)
                              .equals(cheUserId))
              .findFirst();
      if (matchedSecret.isPresent()) {
        final String tokenValue =
            new String(Base64.getDecoder().decode(matchedSecret.get().getData().get("token")));
        gitCredentialsSecretProvisioner.provision(
            meta.getName(),
            hostname,
            matchedSecret.get().getMetadata().getAnnotations().get(BITBUCKET_USERNAME_ANNOTATION),
            tokenValue);
        return Optional.of(tokenValue);
      }
    }
    return Optional.empty();
  }
}
