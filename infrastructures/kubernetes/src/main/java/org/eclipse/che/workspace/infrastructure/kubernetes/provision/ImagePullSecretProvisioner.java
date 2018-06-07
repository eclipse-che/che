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
import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.infrastructure.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.infrastructure.docker.client.dto.AuthConfigs;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

/** @author David Festal */
public class ImagePullSecretProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  private final UserSpecificDockerRegistryCredentialsProvider
      userSpecificDockerRegistryCredentialsProvider;
  private final KubernetesClientFactory clientFactory;
  private final KubernetesNamespaceFactory namespaceFactory;

  @Inject
  public ImagePullSecretProvisioner(
      UserSpecificDockerRegistryCredentialsProvider userSpecificDockerRegistryCredentialsProvider,
      KubernetesClientFactory clientFactory,
      KubernetesNamespaceFactory namespaceFactory) {
    this.userSpecificDockerRegistryCredentialsProvider =
        userSpecificDockerRegistryCredentialsProvider;
    this.clientFactory = clientFactory;
    this.namespaceFactory = namespaceFactory;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    AuthConfigs authConfigs = userSpecificDockerRegistryCredentialsProvider.getCredentials();
    Gson gson = new Gson();

    Base64.Encoder encoder = Base64.getEncoder();

    String config;
    try (StringWriter strWriter = new StringWriter();
        JsonWriter jsonWriter = gson.newJsonWriter(strWriter)) {
      jsonWriter.beginObject();

      authConfigs
          .getConfigs()
          .forEach(
              (name, authConfig) -> {
                try {
                  if (!name.startsWith("https://")) {
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
                  throw new RuntimeException(e);
                }
              });

      jsonWriter.endObject();
      jsonWriter.flush();

      config = strWriter.toString();

      String encodedConfig = encoder.encodeToString(config.getBytes());

      Secret secret =
          new SecretBuilder()
              .addToData(".dockercfg", encodedConfig)
              .withType("kubernetes.io/dockercfg")
              .withNewMetadata()
              .withName("workspace-private-registries")
              .endMetadata()
              .build();

      clientFactory
          .create(identity.getWorkspaceId())
          .secrets()
          .inNamespace(namespaceFactory.create(identity.getWorkspaceId()).getName())
          .createOrReplace(secret);
    } catch (Exception e) {
      throw new InfrastructureException(e);
    }

    k8sEnv.getPods().values().forEach(this::provision);
  }

  public void provision(Pod pod) {
    List<LocalObjectReference> imagePullSecrets = pod.getSpec().getImagePullSecrets();
    pod.getSpec()
        .setImagePullSecrets(
            ImmutableList.<LocalObjectReference>builder()
                .add(
                    new LocalObjectReferenceBuilder()
                        .withName("workspace-private-registries")
                        .build())
                .addAll(imagePullSecrets)
                .build());
  }
}
