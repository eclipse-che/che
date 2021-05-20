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
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_AUTOMOUNT;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_ENV_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_GIT_CREDENTIALS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_MOUNT_AS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_MOUNT_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesSecrets;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class SecretAsContainerResourceProvisionerTest {

  @Mock EnvironmentVariableSecretApplier environmentVariableSecretApplier;
  @Mock GitCredentialStorageFileSecretApplier gitCredentialStorageFileSecretApplier;
  @Mock FileSecretApplier fileSecretApplier;

  private SecretAsContainerResourceProvisioner<KubernetesEnvironment> provisioner;

  @Mock private KubernetesEnvironment environment;

  @Mock private KubernetesNamespace namespace;

  @Mock private KubernetesSecrets secrets;

  @Mock private RuntimeIdentity runtimeIdentity;

  @BeforeMethod
  public void setUp() throws Exception {
    when(namespace.secrets()).thenReturn(secrets);
    provisioner =
        new SecretAsContainerResourceProvisioner<>(
            fileSecretApplier,
            environmentVariableSecretApplier,
            gitCredentialStorageFileSecretApplier,
            new String[] {"app:che"});
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Unable to mount secret 'test_secret': it has missing or unknown type of the mount. Please make sure that 'che.eclipse.org/mount-as' annotation has value either 'env' or 'file'.")
  public void shouldThrowExceptionWhenNoMountTypeSpecified() throws Exception {
    // given
    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("settings.xml", "random", "another.xml", "freedom"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(emptyMap())
                    .withLabels(emptyMap())
                    .build())
            .build();
    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    // when
    provisioner.provision(environment, runtimeIdentity, namespace);
  }

  @Test
  public void shouldCallEnvironmentVariableSecretApplier() throws InfrastructureException {
    // given
    Secret secret =
        new SecretBuilder()
            .withData(singletonMap("foo", "random"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(
                        ImmutableMap.of(
                            ANNOTATION_ENV_NAME,
                            "MY_FOO",
                            ANNOTATION_MOUNT_AS,
                            "env",
                            ANNOTATION_AUTOMOUNT,
                            "true"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    // when
    provisioner.provision(environment, runtimeIdentity, namespace);
    // then
    verify(environmentVariableSecretApplier)
        .applySecret(eq(environment), eq(runtimeIdentity), eq(secret));
    verifyZeroInteractions(fileSecretApplier);
    verifyZeroInteractions(gitCredentialStorageFileSecretApplier);
  }

  @Test
  public void shouldCallFileSecretApplier() throws InfrastructureException {
    // given
    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("settings.xml", "random", "another.xml", "freedom"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(
                        ImmutableMap.of(
                            ANNOTATION_MOUNT_AS,
                            "file",
                            ANNOTATION_MOUNT_PATH,
                            "/home/user/.m2",
                            ANNOTATION_AUTOMOUNT,
                            "true"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    // when
    provisioner.provision(environment, runtimeIdentity, namespace);
    // then
    verify(fileSecretApplier).applySecret(eq(environment), eq(runtimeIdentity), eq(secret));
    verifyZeroInteractions(environmentVariableSecretApplier);
    verifyZeroInteractions(gitCredentialStorageFileSecretApplier);
  }

  @Test
  public void shouldCallGitCredentialStorageFileSecretApplier() throws InfrastructureException {
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

    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    // when
    provisioner.provision(environment, runtimeIdentity, namespace);
    // then
    verify(gitCredentialStorageFileSecretApplier)
        .applySecret(eq(environment), eq(runtimeIdentity), eq(secret));
    verifyZeroInteractions(environmentVariableSecretApplier);
    verifyZeroInteractions(fileSecretApplier);
  }
}
