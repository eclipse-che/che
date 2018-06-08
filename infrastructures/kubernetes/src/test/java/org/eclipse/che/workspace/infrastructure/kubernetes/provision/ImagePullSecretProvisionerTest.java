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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import io.fabric8.kubernetes.api.model.DoneableSecret;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.infrastructure.docker.auth.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfig;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfigs;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link ImagePullSecretProvisionner}.
 *
 * @author David Festal
 */
@Listeners(MockitoTestNGListener.class)
public class ImagePullSecretProvisionerTest {

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private KubernetesClientFactory clientFactory;

  @Mock private UserSpecificDockerRegistryCredentialsProvider credentialsProvider;

  @Mock private KubernetesNamespaceFactory namespaceFactory;
  @Mock private KubernetesNamespace namespace;
  @Mock private AuthConfigs authConfigs;
  @Mock private KubernetesClient kubernetesClient;

  @Mock
  private MixedOperation<Secret, SecretList, DoneableSecret, Resource<Secret, DoneableSecret>>
      mixedOperation;

  @Mock
  private NonNamespaceOperation<
          Secret, SecretList, DoneableSecret, Resource<Secret, DoneableSecret>>
      namespaceOperation;

  @Mock private Pod pod;
  @Mock private PodSpec podSpec;
  private LocalObjectReference existingImagePullSecretRef =
      new LocalObjectReferenceBuilder().withName("existing").build();
  private LocalObjectReference newImagePullSecretRef =
      new LocalObjectReferenceBuilder().withName(ImagePullSecretProvisioner.SECRET_NAME).build();

  private ImagePullSecretProvisioner imagePullSecretProvisioner;

  @BeforeMethod
  public void setup() throws InfrastructureException {
    when(credentialsProvider.getCredentials()).thenReturn(authConfigs);

    when(k8sEnv.getPods()).thenReturn(ImmutableMap.of("wksp", pod));
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getImagePullSecrets()).thenReturn(ImmutableList.of(existingImagePullSecretRef));

    when(runtimeIdentity.getWorkspaceId()).thenReturn("workspaceId");

    when(namespaceFactory.create(anyString())).thenReturn(namespace);
    when(namespace.getName()).thenReturn("namespaceName");

    when(clientFactory.create()).thenReturn(kubernetesClient);
    when(clientFactory.create(anyString())).thenReturn(kubernetesClient);

    doReturn(mixedOperation).when(kubernetesClient).secrets();
    when(mixedOperation.inNamespace(anyString())).thenReturn(namespaceOperation);

    imagePullSecretProvisioner =
        new ImagePullSecretProvisioner(credentialsProvider, clientFactory, namespaceFactory);
  }

  @Test
  public void dontDoAnythingIfNoPrivateRegistries() throws Exception {
    when(authConfigs.getConfigs()).thenReturn(Collections.emptyMap());

    imagePullSecretProvisioner.provision(k8sEnv, runtimeIdentity);

    verifyZeroInteractions(namespaceOperation);
    verifyZeroInteractions(podSpec);
  }

  @Test
  public void addSecretAndReferenceInPod() throws Exception {
    when(authConfigs.getConfigs())
        .thenReturn(
            ImmutableMap.of(
                "reg1",
                new TestAuthConfig().withUsername("username1").withPassword("password1"),
                "reg2",
                new TestAuthConfig().withUsername("username2").withPassword("password2")));

    imagePullSecretProvisioner.provision(k8sEnv, runtimeIdentity);

    verify(podSpec)
        .setImagePullSecrets(ImmutableList.of(newImagePullSecretRef, existingImagePullSecretRef));

    ArgumentCaptor<Secret> varArgs = ArgumentCaptor.forClass(Secret.class);
    verify(namespaceOperation).createOrReplace(varArgs.capture());

    List<Secret> addedSecrets = varArgs.getAllValues();

    assertEquals(addedSecrets.size(), 1);
    Secret secret = addedSecrets.get(0);

    Assert.assertEquals(secret.getType(), "kubernetes.io/dockercfg");
    String dockerCfgData = secret.getData().get(".dockercfg");
    Assert.assertNotNull(dockerCfgData);
    Gson gson = new Gson();
    assertEquals(
        gson.toJson(
            gson.fromJson(new String(Base64.getDecoder().decode(dockerCfgData)), Map.class)),
        gson.toJson(
            gson.fromJson(
                ""
                    + "{ \"https://reg1\": { \"username\": \"username1\", \"password\": \"password1\", \"email\": \"email@email\", \"auth\": \""
                    + buildAuth("username1", "password1")
                    + "\" }"
                    + ", \"https://reg2\": { \"username\": \"username2\", \"password\": \"password2\", \"email\": \"email@email\", \"auth\": \""
                    + buildAuth("username2", "password2")
                    + "\" }"
                    + "}",
                Map.class)));
  }

  private static class TestAuthConfig implements AuthConfig {
    private String username;
    private String password;

    @Override
    public String getUsername() {
      return username;
    }

    @Override
    public void setUsername(String username) {
      this.username = username;
    }

    @Override
    public AuthConfig withUsername(String username) {
      setUsername(username);
      return this;
    }

    @Override
    public String getPassword() {
      return password;
    }

    @Override
    public void setPassword(String password) {
      this.password = password;
    }

    @Override
    public AuthConfig withPassword(String password) {
      setPassword(password);
      return this;
    }
  }

  private String buildAuth(String username, String password) {
    return Base64.getEncoder()
        .encodeToString(
            new StringBuilder()
                .append(username)
                .append(':')
                .append(password)
                .toString()
                .getBytes());
  }
}
