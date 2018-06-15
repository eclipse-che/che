/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.infrastructure.docker.auth.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfig;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfigs;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

/**
 * This class allows workspace-related pods the pull images from the private docker registries
 * defined in the Dashboard 'Administration' page, even with the Kubernetes and OpenShift
 * infrastructures. <br>
 * <br>
 * <strong>How it works</strong> <br>
 * When starting a workspace, this provisioner first creates or replaces, in the K8S namespace of
 * the workspace, an <a
 * href="https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod">{@code
 * imagePullSecret}</a> that reflects the user private registries settings.
 *
 * <p>Then a reference to the created {@code imagePullSecret} is added in each workspace POD
 * specification.
 *
 * @author David Festal
 */
public class ImagePullSecretProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  static final String SECRET_NAME = "workspace-private-registries";

  private final UserSpecificDockerRegistryCredentialsProvider credentialsProvider;
  private final KubernetesClientFactory clientFactory;
  private final KubernetesNamespaceFactory namespaceFactory;

  @Inject
  public ImagePullSecretProvisioner(
      UserSpecificDockerRegistryCredentialsProvider credentialsProvider,
      KubernetesClientFactory clientFactory,
      KubernetesNamespaceFactory namespaceFactory) {
    this.credentialsProvider = credentialsProvider;
    this.clientFactory = clientFactory;
    this.namespaceFactory = namespaceFactory;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    AuthConfigs credentials = credentialsProvider.getCredentials();
    if (credentials == null) {
      return;
    }

    Map<String, AuthConfig> authConfigs = credentials.getConfigs();

    if (authConfigs == null || authConfigs.isEmpty()) {
      return;
    }

    try {
      String encodedConfig =
          Base64.getEncoder().encodeToString(generateDockerCfg(authConfigs).getBytes());

      Secret secret =
          new SecretBuilder()
              .addToData(".dockercfg", encodedConfig)
              .withType("kubernetes.io/dockercfg")
              .withNewMetadata()
              .withName(SECRET_NAME)
              .endMetadata()
              .build();

      clientFactory
          .create(identity.getWorkspaceId())
          .secrets()
          .inNamespace(namespaceFactory.create(identity.getWorkspaceId()).getName())
          .createOrReplace(secret);
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(
          "Unexpected exception occured while adding the 'ImagePullSecret' built from private docker registry user preferences",
          e);
    }

    k8sEnv.getPods().values().forEach(this::provision);
  }

  /**
   * Generates a dockercfg file with the authentication information contained in the given {@code
   * authConfigs} parameter.
   *
   * <p>The syntax of the produced <code>dockercfg</code> file is as follow:
   *
   * <pre><code>
   *   {
   *     "private.repo.1" : {
   *        "username": "theUsername1",
   *        "password": "thePassword1",
   *        "email": "theEmail1",
   *        "auth": "<base64 encoding of username:password>",
   *      },
   *     "private.repo.2" : {
   *        "username": "theUsername2",
   *        "password": "thePassword2",
   *        "email": "theEmail2",
   *        "auth": "<base64 encoding of username:password>",
   *      }
   *   }
   * </code></pre>
   */
  private String generateDockerCfg(Map<String, AuthConfig> authConfigs)
      throws InfrastructureException {
    try (StringWriter strWriter = new StringWriter();
        JsonWriter jsonWriter = new Gson().newJsonWriter(strWriter)) {
      Base64.Encoder encoder = Base64.getEncoder();

      jsonWriter.beginObject();
      for (Map.Entry<String, AuthConfig> entry : authConfigs.entrySet()) {
        String name = entry.getKey();
        AuthConfig authConfig = entry.getValue();
        try {
          if (!name.startsWith("https://") && !name.startsWith("http://")) {
            name = "https://" + name;
          }
          jsonWriter.name(name);
          jsonWriter.beginObject();
          jsonWriter.name("username");
          jsonWriter.value(authConfig.getUsername());
          jsonWriter.name("password");
          jsonWriter.value(authConfig.getPassword());
          jsonWriter.name("email");
          jsonWriter.value("email@email");
          String auth =
              new StringBuilder(authConfig.getUsername())
                  .append(':')
                  .append(authConfig.getPassword())
                  .toString();
          jsonWriter.name("auth");
          jsonWriter.value(encoder.encodeToString(auth.getBytes()));
          jsonWriter.endObject();
        } catch (IOException e) {
          throw new InfrastructureException(
              "Unexpected exception occured while building the 'ImagePullSecret' from private docker registry user preferences",
              e);
        }
      }

      jsonWriter.endObject();
      jsonWriter.flush();
      return strWriter.toString();
    } catch (IOException e) {
      throw new InfrastructureException(e);
    }
  }

  private void provision(Pod pod) {
    List<LocalObjectReference> imagePullSecrets = pod.getSpec().getImagePullSecrets();
    pod.getSpec()
        .setImagePullSecrets(
            ImmutableList.<LocalObjectReference>builder()
                .add(new LocalObjectReferenceBuilder().withName(SECRET_NAME).build())
                .addAll(imagePullSecrets)
                .build());
  }
}
