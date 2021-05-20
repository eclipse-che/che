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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.dto.DockerAuthConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.dto.DockerAuthConfigs;
import org.eclipse.che.workspace.infrastructure.kubernetes.docker.auth.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * This class allows workspace-related pods the pull images from the private docker registries
 * defined in the Dashboard 'Administration' page, even with the Kubernetes and OpenShift
 * infrastructures. <br>
 * <br>
 * <strong>How it works</strong> <br>
 * When starting a workspace, this provisioner adds an <a
 * href="https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod">{@code
 * imagePullSecret}</a> into environment that reflects the user private registries settings.
 *
 * <p>Then a reference to the created {@code imagePullSecret} is added in each workspace POD
 * specification.
 *
 * @author David Festal
 */
public class ImagePullSecretProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  static final String SECRET_NAME_SUFFIX = "-private-registries";

  private final UserSpecificDockerRegistryCredentialsProvider credentialsProvider;

  @Inject
  public ImagePullSecretProvisioner(
      UserSpecificDockerRegistryCredentialsProvider credentialsProvider) {
    this.credentialsProvider = credentialsProvider;
  }

  @Override
  @Traced
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    TracingTags.WORKSPACE_ID.set(identity::getWorkspaceId);

    DockerAuthConfigs credentials = credentialsProvider.getCredentials();
    if (credentials == null) {
      return;
    }

    Map<String, DockerAuthConfig> authConfigs = credentials.getConfigs();

    if (authConfigs == null || authConfigs.isEmpty()) {
      return;
    }

    String encodedConfig =
        Base64.getEncoder().encodeToString(generateDockerCfg(authConfigs).getBytes());

    Secret secret =
        new SecretBuilder()
            .addToData(".dockercfg", encodedConfig)
            .withType("kubernetes.io/dockercfg")
            .withNewMetadata()
            .withName(identity.getWorkspaceId() + SECRET_NAME_SUFFIX)
            .endMetadata()
            .build();

    k8sEnv.getSecrets().put(secret.getMetadata().getName(), secret);

    k8sEnv
        .getPodsData()
        .values()
        .forEach(p -> addImagePullSecret(secret.getMetadata().getName(), p.getSpec()));
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
  private String generateDockerCfg(Map<String, DockerAuthConfig> authConfigs)
      throws InfrastructureException {
    try (StringWriter strWriter = new StringWriter();
        JsonWriter jsonWriter = new Gson().newJsonWriter(strWriter)) {
      Base64.Encoder encoder = Base64.getEncoder();

      jsonWriter.beginObject();
      for (Map.Entry<String, DockerAuthConfig> entry : authConfigs.entrySet()) {
        String name = entry.getKey();
        DockerAuthConfig dockerAuthConfig = entry.getValue();
        try {
          if (!name.startsWith("https://") && !name.startsWith("http://")) {
            name = "https://" + name;
          }
          jsonWriter.name(name);
          jsonWriter.beginObject();
          jsonWriter.name("username");
          jsonWriter.value(dockerAuthConfig.getUsername());
          jsonWriter.name("password");
          jsonWriter.value(dockerAuthConfig.getPassword());
          jsonWriter.name("email");
          jsonWriter.value("email@email");
          String auth = dockerAuthConfig.getUsername() + ':' + dockerAuthConfig.getPassword();
          jsonWriter.name("auth");
          jsonWriter.value(encoder.encodeToString(auth.getBytes()));
          jsonWriter.endObject();
        } catch (IOException e) {
          throw new InfrastructureException(
              "Unexpected exception occurred while building the 'ImagePullSecret' from private docker registry user preferences",
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

  private void addImagePullSecret(String secretName, PodSpec podSpec) {
    List<LocalObjectReference> imagePullSecrets = podSpec.getImagePullSecrets();
    podSpec.setImagePullSecrets(
        ImmutableList.<LocalObjectReference>builder()
            .add(new LocalObjectReferenceBuilder().withName(secretName).build())
            .addAll(imagePullSecrets)
            .build());
  }
}
