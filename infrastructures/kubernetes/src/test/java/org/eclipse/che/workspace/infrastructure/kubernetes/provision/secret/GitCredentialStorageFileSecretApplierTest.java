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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.GitConfigProvisioner.GIT_CONFIG_MAP_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_AUTOMOUNT;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_GIT_CREDENTIALS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_MOUNT_AS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_MOUNT_PATH;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.K8sVersion;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.GitConfigProvisioner;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class GitCredentialStorageFileSecretApplierTest {

  @Mock private KubernetesEnvironment environment;

  @Mock private KubernetesEnvironment.PodData podData;

  @Mock private PodSpec podSpec;

  @Mock private RuntimeIdentity runtimeIdentity;

  @Mock private K8sVersion kubernetesVersion;

  GitCredentialStorageFileSecretApplier secretApplier;

  public static final String GIT_CONFIG_CONTENT =
      "[user]\n\tname = Michelangelo Merisi da Caravaggio\n\tmail = mcaravag@email.not.exists.com";

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(kubernetesVersion.newerOrEqualThan(1, 13)).thenReturn(true);
    lenient().when(kubernetesVersion.olderThan(1, 13)).thenReturn(false);
    secretApplier = new GitCredentialStorageFileSecretApplier(kubernetesVersion);
    when(environment.getPodsData()).thenReturn(singletonMap("pod1", podData));
    when(podData.getRole()).thenReturn(KubernetesEnvironment.PodRole.DEPLOYMENT);
    when(podData.getSpec()).thenReturn(podSpec);
    lenient().when(runtimeIdentity.getWorkspaceId()).thenReturn("ws-1234598");
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Invalid git credential secret data. It should contain only 1 data item but it have 2")
  public void shouldThrowInfrastructureExceptionIfSecretsHasMoreOrLessWhen1Data()
      throws InfrastructureException {
    // given
    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("credentials", "random", "credentials2", "random"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(
                        ImmutableMap.of(
                            ANNOTATION_MOUNT_AS,
                            "file",
                            ANNOTATION_MOUNT_PATH,
                            "/home/user/.git",
                            ANNOTATION_GIT_CREDENTIALS,
                            "true",
                            ANNOTATION_AUTOMOUNT,
                            "true"))
                    .withLabels(emptyMap())
                    .build())
            .build();
    // when
    secretApplier.applySecret(environment, runtimeIdentity, secret);
  }

  @Test
  public void shouldBeAbleToAdjustGiConfigConfigMap() throws InfrastructureException {
    // given
    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("credentials", "random"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(
                        ImmutableMap.of(
                            ANNOTATION_MOUNT_AS,
                            "file",
                            ANNOTATION_MOUNT_PATH,
                            "/home/user/.git",
                            ANNOTATION_GIT_CREDENTIALS,
                            "true",
                            ANNOTATION_AUTOMOUNT,
                            "true"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    ConfigMap configMap =
        new ConfigMapBuilder()
            .withData(ImmutableMap.of(GitConfigProvisioner.GIT_CONFIG, GIT_CONFIG_CONTENT))
            .build();
    when(environment.getConfigMaps()).thenReturn(ImmutableMap.of(GIT_CONFIG_MAP_NAME, configMap));
    // when
    secretApplier.applySecret(environment, runtimeIdentity, secret);
    // then
    String data = configMap.getData().get(GitConfigProvisioner.GIT_CONFIG);
    assertTrue(
        data.endsWith("[credential]\n\thelper = store --file /home/user/.git/credentials\n"));
    assertTrue(data.startsWith(GIT_CONFIG_CONTENT));
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Multiple git credentials secrets found. Please remove duplication.")
  public void shouldThrowInfrastructureExceptionIfGitConfigAlreadyContainsSecretConfig()
      throws InfrastructureException {
    // given
    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("credentials", "random"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(
                        ImmutableMap.of(
                            ANNOTATION_MOUNT_AS,
                            "file",
                            ANNOTATION_MOUNT_PATH,
                            "/home/user/.git",
                            ANNOTATION_GIT_CREDENTIALS,
                            "true",
                            ANNOTATION_AUTOMOUNT,
                            "true"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    ConfigMap configMap =
        new ConfigMapBuilder()
            .withData(
                ImmutableMap.of(
                    GitConfigProvisioner.GIT_CONFIG,
                    GIT_CONFIG_CONTENT
                        + "[credential]\n\thelper = store --file /home/user/.git/credentials\n"))
            .build();
    when(environment.getConfigMaps()).thenReturn(ImmutableMap.of(GIT_CONFIG_MAP_NAME, configMap));
    // when
    secretApplier.applySecret(environment, runtimeIdentity, secret);
  }
}
