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

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;

@Singleton
public class BitbucketServerGitCredentialsSecretProvisioner {

  private final KubernetesClientFactory clientFactory;

  private static final String SECRET_NAME = "bitbucket-git-credentials-secret";

  final Map<String, String> labels =
      Map.of(
          "app.kubernetes.io/part-of", "che.eclipse.org",
          "app.kubernetes.io/component", "workspace-secret");

  final Map<String, String> annotations =
      Map.of(
          "che.eclipse.org/automount-workspace-secret",
          "true",
          "che.eclipse.org/mount-path",
          "/home/theia/.git-credentials",
          "che.eclipse.org/mount-as",
          "file",
          "che.eclipse.org/git-credential",
          "true");

  @Inject
  public BitbucketServerGitCredentialsSecretProvisioner(KubernetesClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  public void provision(
      String namespace, String bitbucketHost, String bitbucketUsername, String token)
      throws DevfileException {

    ObjectMeta meta =
        new ObjectMetaBuilder()
            .withName(SECRET_NAME)
            .withAnnotations(annotations)
            .withLabels(labels)
            .build();

    URL bitbucketURL;
    try {
      bitbucketURL = new URL(bitbucketHost);
    } catch (MalformedURLException e) {
      throw new DevfileException(e.getMessage());
    }

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
                                    bitbucketURL.getProtocol(),
                                    bitbucketUsername,
                                    token,
                                    bitbucketURL.getHost())
                                .getBytes())))
            .build();

    try {
      clientFactory.create().secrets().inNamespace(namespace).createOrReplace(secret);
    } catch (InfrastructureException e) {
      throw new DevfileException(e.getMessage());
    }
  }
}
