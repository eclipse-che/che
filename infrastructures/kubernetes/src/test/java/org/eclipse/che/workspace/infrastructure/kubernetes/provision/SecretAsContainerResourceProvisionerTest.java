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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecretAsContainerResourceProvisioner.ANNOTATION_ENV_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecretAsContainerResourceProvisioner.ANNOTATION_ENV_NAME_TEMPLATE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecretAsContainerResourceProvisioner.ANNOTATION_MOUNT_AS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecretAsContainerResourceProvisioner.ANNOTATION_MOUNT_PATH;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecretAsContainerResourceProvisioner.ANNOTATION_TARGET_CONTAINER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesSecrets;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class SecretAsContainerResourceProvisionerTest {

  private SecretAsContainerResourceProvisioner<KubernetesEnvironment> provisioner =
      new SecretAsContainerResourceProvisioner<>(new String[] {"app:che"});

  @Mock private KubernetesEnvironment environment;

  @Mock private KubernetesNamespace namespace;

  @Mock private KubernetesSecrets secrets;

  @Mock private PodData podData;

  @Mock private PodSpec podSpec;

  @BeforeMethod
  public void setUp() throws Exception {
    when(namespace.secrets()).thenReturn(secrets);
    when(environment.getPodsData()).thenReturn(singletonMap("pod1", podData));

    when(podData.getRole()).thenReturn(PodRole.DEPLOYMENT);
    when(podData.getSpec()).thenReturn(podSpec);
  }

  @Test
  public void shouldProvisionSingleEnvVariable() throws Exception {
    Container container_match = new ContainerBuilder().withName("maven").build();
    Container container_unmatch = spy(new ContainerBuilder().withName("other").build());

    when(podSpec.getContainers()).thenReturn(ImmutableList.of(container_match, container_unmatch));

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
                            ANNOTATION_TARGET_CONTAINER,
                            "maven"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    provisioner.provision(environment, namespace);

    // nothing to do with unmatched container
    verify(container_unmatch).getName();
    verifyNoMoreInteractions(container_unmatch);

    // matched container has env set
    assertEquals(container_match.getEnv().size(), 1);
    EnvVar var = container_match.getEnv().get(0);
    assertEquals(var.getName(), "MY_FOO");
    assertEquals(var.getValueFrom().getSecretKeyRef().getName(), "test_secret");
    assertEquals(var.getValueFrom().getSecretKeyRef().getKey(), "foo");
  }

  @Test
  public void shouldProvisionMultiEnvVariable() throws Exception {
    Container container_match = new ContainerBuilder().withName("maven").build();
    Container container_unmatch = spy(new ContainerBuilder().withName("other").build());

    when(podSpec.getContainers()).thenReturn(ImmutableList.of(container_match, container_unmatch));

    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("foo", "random", "bar", "freedom"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(
                        ImmutableMap.of(
                            format(ANNOTATION_ENV_NAME_TEMPLATE, "foo"),
                            "MY_FOO",
                            format(ANNOTATION_ENV_NAME_TEMPLATE, "bar"),
                            "MY_BAR",
                            ANNOTATION_MOUNT_AS,
                            "env",
                            ANNOTATION_TARGET_CONTAINER,
                            "maven"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    provisioner.provision(environment, namespace);

    // nothing to do with unmatched container
    verify(container_unmatch).getName();
    verifyNoMoreInteractions(container_unmatch);

    // matched container has env set
    assertEquals(container_match.getEnv().size(), 2);
    EnvVar var = container_match.getEnv().get(0);
    assertEquals(var.getName(), "MY_FOO");
    assertEquals(var.getValueFrom().getSecretKeyRef().getName(), "test_secret");
    assertEquals(var.getValueFrom().getSecretKeyRef().getKey(), "foo");

    EnvVar var2 = container_match.getEnv().get(1);
    assertEquals(var2.getName(), "MY_BAR");
    assertEquals(var2.getValueFrom().getSecretKeyRef().getName(), "test_secret");
    assertEquals(var2.getValueFrom().getSecretKeyRef().getKey(), "bar");
  }

  @Test
  public void shouldProvisionAllContainersIfNotSpecifyOne() throws Exception {
    Container container_match1 = new ContainerBuilder().withName("maven").build();
    Container container_match2 = new ContainerBuilder().withName("other").build();

    when(podSpec.getContainers()).thenReturn(ImmutableList.of(container_match1, container_match2));

    Secret secret =
        new SecretBuilder()
            .withData(singletonMap("foo", "random"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(
                        ImmutableMap.of(ANNOTATION_ENV_NAME, "MY_FOO", ANNOTATION_MOUNT_AS, "env"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    provisioner.provision(environment, namespace);

    // both containers has env set
    assertEquals(container_match1.getEnv().size(), 1);
    EnvVar var = container_match1.getEnv().get(0);
    assertEquals(var.getName(), "MY_FOO");
    assertEquals(var.getValueFrom().getSecretKeyRef().getName(), "test_secret");
    assertEquals(var.getValueFrom().getSecretKeyRef().getKey(), "foo");

    assertEquals(container_match2.getEnv().size(), 1);
    EnvVar var2 = container_match2.getEnv().get(0);
    assertEquals(var2.getName(), "MY_FOO");
    assertEquals(var2.getValueFrom().getSecretKeyRef().getName(), "test_secret");
    assertEquals(var2.getValueFrom().getSecretKeyRef().getKey(), "foo");
  }

  @Test
  public void shouldProvisionAsFiles() throws Exception {
    Container container_match = new ContainerBuilder().withName("maven").build();
    Container container_unmatch = new ContainerBuilder().withName("other").build();

    PodSpec localSpec =
        new PodSpecBuilder()
            .withContainers(ImmutableList.of(container_match, container_unmatch))
            .build();

    when(podData.getSpec()).thenReturn(localSpec);

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
                            ANNOTATION_TARGET_CONTAINER,
                            "maven"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    provisioner.provision(environment, namespace);

    // pod has volume created
    assertEquals(environment.getPodsData().get("pod1").getSpec().getVolumes().size(), 1);
    Volume volume = environment.getPodsData().get("pod1").getSpec().getVolumes().get(0);
    assertEquals(volume.getName(), "test_secret");
    assertEquals(volume.getSecret().getSecretName(), "test_secret");

    // matched container has mounts set
    assertEquals(
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(0)
            .getVolumeMounts()
            .size(),
        2);
    VolumeMount mount1 =
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(0)
            .getVolumeMounts()
            .get(0);
    assertEquals(mount1.getName(), "test_secret");
    assertEquals(mount1.getMountPath(), "/home/user/.m2/" + mount1.getSubPath());
    assertFalse(mount1.getSubPath().isEmpty());
    assertTrue(mount1.getReadOnly());

    VolumeMount mount2 =
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(0)
            .getVolumeMounts()
            .get(1);
    assertEquals(mount2.getName(), "test_secret");
    assertEquals(mount2.getMountPath(), "/home/user/.m2/" + mount2.getSubPath());
    assertFalse(mount2.getSubPath().isEmpty());
    assertTrue(mount2.getReadOnly());

    // unmatched container has no mounts
    assertEquals(
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(1)
            .getVolumeMounts()
            .size(),
        0);

    if ("settings.xml".equals(mount1.getSubPath())) {
      assertEquals(mount1.getSubPath(), "settings.xml");
      assertEquals(mount2.getSubPath(), "another.xml");
    } else {
      assertEquals(mount1.getSubPath(), "another.xml");
      assertEquals(mount2.getSubPath(), "settings.xml");
    }
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Unable to mount secret 'test_secret': It is configured to be mounted as a file but the mount path was not specified. Please define the 'che.eclipse.org/mount-path' annotation on the secret to specify it.")
  public void shouldThrowExceptionWhenNoMountPathSpecifiedForFiles() throws Exception {
    Container container_match = new ContainerBuilder().withName("maven").build();

    PodSpec localSpec =
        new PodSpecBuilder().withContainers(ImmutableList.of(container_match)).build();

    when(podData.getSpec()).thenReturn(localSpec);
    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("settings.xml", "random", "another.xml", "freedom"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(singletonMap(ANNOTATION_MOUNT_AS, "file"))
                    .withLabels(emptyMap())
                    .build())
            .build();
    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    provisioner.provision(environment, namespace);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Unable to mount secret 'test_secret': it has missing or unknown type of the mount. Please make sure that 'che.eclipse.org/mount-as' annotation has value either 'env' or 'file'.")
  public void shouldThrowExceptionWhenNoMountTypeSpecified() throws Exception {
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
    provisioner.provision(environment, namespace);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Unable to mount secret 'test_secret': It is configured to be mount as a environment variable, but its was not specified. Please define the 'che.eclipse.org/env-name' annotation on the secret to specify it.")
  public void shouldThrowExceptionWhenNoEnvNameSpecifiedSingleValue() throws Exception {
    Container container_match = new ContainerBuilder().withName("maven").build();

    when(podSpec.getContainers()).thenReturn(ImmutableList.of(container_match));

    Secret secret =
        new SecretBuilder()
            .withData(singletonMap("foo", "random"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(ImmutableMap.of(ANNOTATION_MOUNT_AS, "env"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    provisioner.provision(environment, namespace);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Unable to mount key 'foo'  of secret 'test_secret': It is configured to be mount as a environment variable, but its was not specified. Please define the 'che.eclipse.org/foo_env-name' annotation on the secret to specify it.")
  public void shouldThrowExceptionWhenNoEnvNameSpecifiedMultiValue() throws Exception {
    Container container_match = new ContainerBuilder().withName("maven").build();

    when(podSpec.getContainers()).thenReturn(ImmutableList.of(container_match));

    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("foo", "random", "bar", "test"))
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName("test_secret")
                    .withAnnotations(ImmutableMap.of(ANNOTATION_MOUNT_AS, "env"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    when(secrets.get(any(LabelSelector.class))).thenReturn(singletonList(secret));
    provisioner.provision(environment, namespace);
  }
}
