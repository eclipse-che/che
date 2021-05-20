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
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.DEVFILE_COMPONENT_ALIAS_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_AUTOMOUNT;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_MOUNT_AS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_MOUNT_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.K8sVersion;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesSecrets;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class FileSecretApplierTest {

  @Mock private KubernetesEnvironment environment;

  @Mock private KubernetesSecrets secrets;

  @Mock private PodData podData;

  @Mock private PodSpec podSpec;

  @Mock private RuntimeIdentity runtimeIdentity;

  @Mock private K8sVersion kubernetesVersion;

  FileSecretApplier secretApplier;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(kubernetesVersion.newerOrEqualThan(1, 13)).thenReturn(true);
    lenient().when(kubernetesVersion.olderThan(1, 13)).thenReturn(false);
    secretApplier = new FileSecretApplier(kubernetesVersion);
    when(environment.getPodsData()).thenReturn(singletonMap("pod1", podData));
    when(podData.getRole()).thenReturn(PodRole.DEPLOYMENT);
    when(podData.getSpec()).thenReturn(podSpec);
  }

  @Test
  public void shouldProvisionAsFiles() throws Exception {
    Container container_match1 = new ContainerBuilder().withName("maven").build();
    Container container_match2 = new ContainerBuilder().withName("other").build();

    PodSpec localSpec =
        new PodSpecBuilder()
            .withContainers(ImmutableList.of(container_match1, container_match2))
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
                            ANNOTATION_AUTOMOUNT,
                            "true"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    secretApplier.applySecret(environment, runtimeIdentity, secret);

    // pod has volume created
    assertEquals(environment.getPodsData().get("pod1").getSpec().getVolumes().size(), 1);
    Volume volume = environment.getPodsData().get("pod1").getSpec().getVolumes().get(0);
    assertEquals(volume.getName(), "test_secret");
    assertEquals(volume.getSecret().getSecretName(), "test_secret");

    // both containers has mounts set
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

    assertEquals(
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(1)
            .getVolumeMounts()
            .size(),
        2);

    if ("settings.xml".equals(mount1.getSubPath())) {
      assertEquals(mount1.getSubPath(), "settings.xml");
      assertEquals(mount2.getSubPath(), "another.xml");
    } else {
      assertEquals(mount1.getSubPath(), "another.xml");
      assertEquals(mount2.getSubPath(), "settings.xml");
    }
  }

  @Test
  public void shouldProvisionAsFilesWithPathOverride() throws Exception {
    Container container = new ContainerBuilder().withName("maven").build();

    DevfileImpl mock_defvile = mock(DevfileImpl.class);
    ComponentImpl component = new ComponentImpl();
    component.setAlias("maven");
    component.getVolumes().add(new VolumeImpl("test_secret", "/path/to/override"));

    InternalMachineConfig internalMachineConfig = new InternalMachineConfig();
    internalMachineConfig.getAttributes().put(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE, "maven");
    when(environment.getMachines()).thenReturn(ImmutableMap.of("maven", internalMachineConfig));
    when(environment.getDevfile()).thenReturn(mock_defvile);
    when(mock_defvile.getComponents()).thenReturn(singletonList(component));

    PodSpec localSpec = new PodSpecBuilder().withContainers(ImmutableList.of(container)).build();

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
                            ANNOTATION_AUTOMOUNT,
                            "true"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    secretApplier.applySecret(environment, runtimeIdentity, secret);

    // pod has volume created
    assertEquals(environment.getPodsData().get("pod1").getSpec().getVolumes().size(), 1);
    Volume volume = environment.getPodsData().get("pod1").getSpec().getVolumes().get(0);
    assertEquals(volume.getName(), "test_secret");
    assertEquals(volume.getSecret().getSecretName(), "test_secret");

    // both containers has mounts set
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
    assertEquals(mount1.getMountPath(), "/path/to/override/settings.xml");
    assertTrue(mount1.getReadOnly());
  }

  @Test
  public void shouldProvisionContainersWithAutomountOverrideTrue() throws Exception {
    Container container_match1 = new ContainerBuilder().withName("maven").build();
    Container container_match2 = new ContainerBuilder().withName("other").build();
    DevfileImpl mock_defvile = mock(DevfileImpl.class);
    ComponentImpl component = new ComponentImpl();
    component.setAlias("maven");
    component.setAutomountWorkspaceSecrets(true);

    InternalMachineConfig internalMachineConfig = new InternalMachineConfig();
    internalMachineConfig.getAttributes().put(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE, "maven");
    when(environment.getMachines()).thenReturn(ImmutableMap.of("maven", internalMachineConfig));
    when(environment.getDevfile()).thenReturn(mock_defvile);
    when(mock_defvile.getComponents()).thenReturn(singletonList(component));

    PodSpec localSpec =
        new PodSpecBuilder()
            .withContainers(ImmutableList.of(container_match1, container_match2))
            .build();

    when(podData.getSpec()).thenReturn(localSpec);

    Secret secret =
        new SecretBuilder()
            .withData(singletonMap("foo", "random"))
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
                            "false"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    secretApplier.applySecret(environment, runtimeIdentity, secret);

    // pod has volume created
    assertEquals(environment.getPodsData().get("pod1").getSpec().getVolumes().size(), 1);
    Volume volume = environment.getPodsData().get("pod1").getSpec().getVolumes().get(0);
    assertEquals(volume.getName(), "test_secret");
    assertEquals(volume.getSecret().getSecretName(), "test_secret");

    // first container has mount set
    assertEquals(
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(0)
            .getVolumeMounts()
            .size(),
        1);
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
    assertEquals(mount1.getMountPath(), "/home/user/.m2/foo");
    assertTrue(mount1.getReadOnly());

    // second container has no mounts
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
  }

  @Test
  public void shouldNotProvisionContainersWithAutomountOverrideFalse() throws Exception {
    Container container_match1 = new ContainerBuilder().withName("maven").build();
    Container container_match2 = new ContainerBuilder().withName("other").build();
    DevfileImpl mock_defvile = mock(DevfileImpl.class);
    ComponentImpl component = new ComponentImpl();
    component.setAlias("maven");
    component.setAutomountWorkspaceSecrets(false);

    InternalMachineConfig internalMachineConfig = new InternalMachineConfig();
    internalMachineConfig.getAttributes().put(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE, "maven");
    when(environment.getMachines()).thenReturn(ImmutableMap.of("maven", internalMachineConfig));
    when(environment.getDevfile()).thenReturn(mock_defvile);
    when(mock_defvile.getComponents()).thenReturn(singletonList(component));

    PodSpec localSpec =
        new PodSpecBuilder()
            .withContainers(ImmutableList.of(container_match1, container_match2))
            .build();

    when(podData.getSpec()).thenReturn(localSpec);

    Secret secret =
        new SecretBuilder()
            .withData(singletonMap("foo", "random"))
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

    secretApplier.applySecret(environment, runtimeIdentity, secret);

    // only second container has mounts
    assertEquals(environment.getPodsData().get("pod1").getSpec().getVolumes().size(), 1);
    Volume volume = environment.getPodsData().get("pod1").getSpec().getVolumes().get(0);
    assertEquals(volume.getName(), "test_secret");
    assertEquals(volume.getSecret().getSecretName(), "test_secret");

    assertEquals(
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(0)
            .getVolumeMounts()
            .size(),
        0);

    assertEquals(
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(1)
            .getVolumeMounts()
            .size(),
        1);
    VolumeMount mount2 =
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(1)
            .getVolumeMounts()
            .get(0);
    assertEquals(mount2.getName(), "test_secret");
    assertEquals(mount2.getMountPath(), "/home/user/.m2/foo");
    assertTrue(mount2.getReadOnly());
  }

  @Test
  public void shouldNotProvisionAllContainersifAutomountDisabled() throws Exception {
    Container container_match1 = spy(new ContainerBuilder().withName("maven").build());
    Container container_match2 = spy(new ContainerBuilder().withName("other").build());

    when(podSpec.getContainers()).thenReturn(ImmutableList.of(container_match1, container_match2));

    Secret secret =
        new SecretBuilder()
            .withData(singletonMap("foo", "random"))
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
                            "false"))
                    .withLabels(emptyMap())
                    .build())
            .build();

    secretApplier.applySecret(environment, runtimeIdentity, secret);

    verify(container_match1).getName();
    verify(container_match2).getName();
    // both containers no actions
    verifyNoMoreInteractions(container_match1, container_match2);
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
    secretApplier.applySecret(environment, runtimeIdentity, secret);
  }

  @Test
  public void shouldNotUseSubpathForOlderK8s() throws InfrastructureException {
    lenient().when(kubernetesVersion.newerOrEqualThan(1, 13)).thenReturn(false);
    lenient().when(kubernetesVersion.olderThan(1, 13)).thenReturn(true);

    Container container_match1 = new ContainerBuilder().withName("maven").build();

    PodSpec localSpec =
        new PodSpecBuilder().withContainers(ImmutableList.of(container_match1)).build();

    when(podData.getSpec()).thenReturn(localSpec);

    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("settings.xml", "random"))
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

    secretApplier.applySecret(environment, runtimeIdentity, secret);

    // pod has volume created
    assertEquals(environment.getPodsData().get("pod1").getSpec().getVolumes().size(), 1);
    Volume volume = environment.getPodsData().get("pod1").getSpec().getVolumes().get(0);
    assertEquals(volume.getName(), "test_secret");
    assertEquals(volume.getSecret().getSecretName(), "test_secret");

    assertEquals(
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(0)
            .getVolumeMounts()
            .size(),
        1);
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
    assertEquals(mount1.getMountPath(), "/home/user/.m2");
    assertNull(mount1.getSubPath());
  }

  @Test
  public void shouldNotOverrideExistingVolumeMounts() throws InfrastructureException {
    Container container_match1 =
        new ContainerBuilder()
            .withName("maven")
            .withVolumeMounts(
                new VolumeMountBuilder()
                    .withName("existing-volume")
                    .withMountPath("/home/user/.m2")
                    .build())
            .build();

    PodSpec localSpec =
        new PodSpecBuilder().withContainers(ImmutableList.of(container_match1)).build();

    when(podData.getSpec()).thenReturn(localSpec);

    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("settings.xml", "random"))
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

    secretApplier.applySecret(environment, runtimeIdentity, secret);

    // pod has volume created
    assertEquals(environment.getPodsData().get("pod1").getSpec().getVolumes().size(), 1);
    Volume volume = environment.getPodsData().get("pod1").getSpec().getVolumes().get(0);
    assertEquals(volume.getName(), "test_secret");
    assertEquals(volume.getSecret().getSecretName(), "test_secret");

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
    assertEquals(mount1.getName(), "existing-volume");
    assertEquals(mount1.getMountPath(), "/home/user/.m2");
    assertNull(mount1.getSubPath());

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
    assertEquals(mount2.getMountPath(), "/home/user/.m2/settings.xml");
    assertEquals(mount2.getSubPath(), "settings.xml");
  }

  @Test
  public void shouldOverrideExistingVolumeMountsOnOlderK8s() throws InfrastructureException {
    lenient().when(kubernetesVersion.newerOrEqualThan(1, 13)).thenReturn(false);
    lenient().when(kubernetesVersion.olderThan(1, 13)).thenReturn(true);

    Container container_match1 =
        new ContainerBuilder()
            .withName("maven")
            .withVolumeMounts(
                new VolumeMountBuilder()
                    .withName("existing-volume")
                    .withMountPath("/home/user/.m2")
                    .build())
            .build();

    PodSpec localSpec =
        new PodSpecBuilder().withContainers(ImmutableList.of(container_match1)).build();

    when(podData.getSpec()).thenReturn(localSpec);

    Secret secret =
        new SecretBuilder()
            .withData(ImmutableMap.of("settings.xml", "random"))
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

    secretApplier.applySecret(environment, runtimeIdentity, secret);

    // pod has volume created
    assertEquals(environment.getPodsData().get("pod1").getSpec().getVolumes().size(), 1);
    Volume volume = environment.getPodsData().get("pod1").getSpec().getVolumes().get(0);
    assertEquals(volume.getName(), "test_secret");
    assertEquals(volume.getSecret().getSecretName(), "test_secret");

    assertEquals(
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(0)
            .getVolumeMounts()
            .size(),
        1);
    VolumeMount secretMount =
        environment
            .getPodsData()
            .get("pod1")
            .getSpec()
            .getContainers()
            .get(0)
            .getVolumeMounts()
            .get(0);
    assertEquals(secretMount.getName(), "test_secret");
    assertEquals(secretMount.getMountPath(), "/home/user/.m2");
    assertNull(secretMount.getSubPath());
  }
}
