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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_VOLUME_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategyTest.mockName;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.TestObjects.newContainer;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.TestObjects.newPod;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link UniqueWorkspacePVCStrategy}.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class UniqueWorkspacePVCStrategyTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final String PVC_NAME_PREFIX = "che-claim";

  private static final String POD_1_NAME = "main";
  private static final String CONTAINER_1_NAME = "app";
  private static final String CONTAINER_2_NAME = "db";
  private static final String MACHINE_1_NAME = POD_1_NAME + '/' + CONTAINER_1_NAME;
  private static final String MACHINE_2_NAME = POD_1_NAME + '/' + CONTAINER_2_NAME;

  private static final String POD_2_NAME = "second";
  private static final String CONTAINER_3_NAME = "app2";
  private static final String MACHINE_3_NAME = POD_2_NAME + '/' + CONTAINER_3_NAME;

  private static final String VOLUME_1_NAME = "vol1";
  private static final String VOLUME_2_NAME = "vol2";

  private static final String PVC_QUANTITY = "10Gi";
  private static final String PVC_ACCESS_MODE = "RWO";

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "id1");

  private KubernetesEnvironment k8sEnv;
  @Mock private KubernetesNamespaceFactory factory;
  @Mock private KubernetesNamespace k8sNamespace;
  @Mock private KubernetesPersistentVolumeClaims pvcs;
  private Pod pod;
  private Pod pod2;
  @Mock private EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;

  private UniqueWorkspacePVCStrategy strategy;

  @BeforeMethod
  public void setup() throws Exception {
    strategy =
        new UniqueWorkspacePVCStrategy(
            PVC_NAME_PREFIX, PVC_QUANTITY, PVC_ACCESS_MODE, factory, ephemeralWorkspaceAdapter);

    k8sEnv = KubernetesEnvironment.builder().build();

    k8sEnv
        .getMachines()
        .put(
            MACHINE_1_NAME,
            TestObjects.newMachineConfig()
                .withVolume(VOLUME_1_NAME, "/path")
                .withVolume(VOLUME_2_NAME, "/path2")
                .build());

    k8sEnv
        .getMachines()
        .put(
            MACHINE_2_NAME,
            TestObjects.newMachineConfig().withVolume(VOLUME_2_NAME, "/path2").build());

    k8sEnv
        .getMachines()
        .put(
            MACHINE_3_NAME,
            TestObjects.newMachineConfig().withVolume(VOLUME_1_NAME, "/path").build());

    pod =
        newPod(POD_1_NAME)
            .withContainers(
                newContainer(CONTAINER_1_NAME).build(), newContainer(CONTAINER_2_NAME).build())
            .build();

    pod2 = newPod(POD_2_NAME).withContainers(newContainer(CONTAINER_3_NAME).build()).build();

    k8sEnv.addPod(pod);
    k8sEnv.addPod(pod2);

    when(factory.create(WORKSPACE_ID)).thenReturn(k8sNamespace);
    when(k8sNamespace.persistentVolumeClaims()).thenReturn(pvcs);
  }

  @Test
  public void testProvisionPVCsForEachVolumeWithUniqueName() throws Exception {
    // given
    k8sEnv.getPersistentVolumeClaims().clear();

    // when
    strategy.provision(k8sEnv, IDENTITY);

    // then
    assertEquals(pod.getSpec().getVolumes().size(), 2);
    assertEquals(pod.getSpec().getContainers().get(0).getVolumeMounts().size(), 2);
    assertEquals(pod.getSpec().getContainers().get(1).getVolumeMounts().size(), 1);

    assertEquals(pod2.getSpec().getVolumes().size(), 1);
    assertEquals(pod2.getSpec().getContainers().get(0).getVolumeMounts().size(), 1);
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 2);

    PersistentVolumeClaim pvcForVolume1 =
        findPvc(VOLUME_1_NAME, k8sEnv.getPersistentVolumeClaims());
    assertNotNull(pvcForVolume1);
    assertTrue(pvcForVolume1.getMetadata().getName().startsWith(PVC_NAME_PREFIX));

    PersistentVolumeClaim pvcForVolume2 =
        findPvc(VOLUME_2_NAME, k8sEnv.getPersistentVolumeClaims());
    assertNotNull(pvcForVolume2);
    assertTrue(pvcForVolume2.getMetadata().getName().startsWith(PVC_NAME_PREFIX));
  }

  @Test
  public void testProcessingUserDefinedPVCsBoundToMultiplyContainers() throws Exception {
    // given
    k8sEnv.getMachines().values().forEach(m -> m.getVolumes().clear());

    k8sEnv.getPersistentVolumeClaims().put("userDataPVC", newPVC("userDataPVC"));

    pod.getSpec()
        .getInitContainers()
        .add(
            new ContainerBuilder()
                .withName("userInitContainer")
                .withVolumeMounts(
                    new VolumeMountBuilder()
                        .withName("userData")
                        .withSubPath("/tmp/init/userData")
                        .build())
                .build());

    pod.getSpec()
        .getContainers()
        .get(0)
        .getVolumeMounts()
        .add(new VolumeMountBuilder().withName("userData").withSubPath("/home/user/data").build());

    pod.getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("userData")
                .withPersistentVolumeClaim(
                    new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName("userDataPVC")
                        .build())
                .build());

    // when
    strategy.provision(k8sEnv, IDENTITY);

    // then
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    PersistentVolumeClaim pvcForUserData =
        findPvc("userDataPVC", k8sEnv.getPersistentVolumeClaims());
    assertNotNull(pvcForUserData);
    assertTrue(pvcForUserData.getMetadata().getName().startsWith(PVC_NAME_PREFIX));
    assertEquals(
        pvcForUserData.getMetadata().getLabels().get(CHE_WORKSPACE_ID_LABEL), WORKSPACE_ID);

    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    io.fabric8.kubernetes.api.model.Volume userPodVolume = podSpec.getVolumes().get(0);
    assertEquals(
        userPodVolume.getPersistentVolumeClaim().getClaimName(),
        pvcForUserData.getMetadata().getName());
    assertEquals(
        podSpec.getVolumes().get(0).getPersistentVolumeClaim().getClaimName(),
        pvcForUserData.getMetadata().getName());

    Container initContainer = podSpec.getInitContainers().get(0);
    VolumeMount initVolumeMount = initContainer.getVolumeMounts().get(0);
    assertEquals(initVolumeMount.getSubPath(), WORKSPACE_ID + "/userDataPVC/tmp/init/userData");
    assertEquals(initVolumeMount.getName(), userPodVolume.getName());

    Container container = podSpec.getContainers().get(0);
    VolumeMount volumeMount = container.getVolumeMounts().get(0);
    assertEquals(volumeMount.getSubPath(), WORKSPACE_ID + "/userDataPVC/home/user/data");
    assertEquals(volumeMount.getName(), userPodVolume.getName());
  }

  @Test
  public void testProcessingUserDefinedNonPVCPodsVolumes() throws Exception {
    // given
    k8sEnv.getMachines().values().forEach(m -> m.getVolumes().clear());

    pod.getSpec()
        .getContainers()
        .get(0)
        .getVolumeMounts()
        .add(
            new VolumeMountBuilder()
                .withName("configMapVolume")
                .withSubPath("/home/user/config")
                .build());

    ConfigMapVolumeSource configMapVolumeSource =
        new ConfigMapVolumeSourceBuilder().withName("configMap").build();
    pod.getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("configMapVolume")
                .withConfigMap(configMapVolumeSource)
                .build());

    // when
    strategy.provision(k8sEnv, IDENTITY);

    // then
    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    io.fabric8.kubernetes.api.model.Volume userPodVolume = podSpec.getVolumes().get(0);
    assertNull(userPodVolume.getPersistentVolumeClaim());
    assertEquals(userPodVolume.getConfigMap(), configMapVolumeSource);

    Container container = podSpec.getContainers().get(0);
    VolumeMount volumeMount = container.getVolumeMounts().get(0);
    assertEquals(volumeMount.getName(), "configMapVolume");
    assertEquals(volumeMount.getSubPath(), "/home/user/config");
  }

  @Test
  public void testMatchingUserDefinedPVCWithCheVolume() throws Exception {
    // given
    k8sEnv.getPersistentVolumeClaims().put("userDataPVC", newPVC("userDataPVC"));

    pod.getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("userData")
                .withPersistentVolumeClaim(
                    new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName("userDataPVC")
                        .build())
                .build());

    pod.getSpec()
        .getContainers()
        .get(0)
        .getVolumeMounts()
        .add(new VolumeMountBuilder().withName("userData").withSubPath("/home/user/data").build());

    k8sEnv.getMachines().values().forEach(m -> m.getVolumes().clear());
    k8sEnv
        .getMachines()
        .get(MACHINE_2_NAME)
        .getVolumes()
        .put("userDataPVC", new VolumeImpl().withPath("/"));

    // when
    strategy.provision(k8sEnv, IDENTITY);

    // then
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    PersistentVolumeClaim pvcForUserData =
        findPvc("userDataPVC", k8sEnv.getPersistentVolumeClaims());
    assertNotNull(pvcForUserData);
    assertTrue(pvcForUserData.getMetadata().getName().startsWith(PVC_NAME_PREFIX));
    assertEquals(
        pvcForUserData.getMetadata().getLabels().get(CHE_WORKSPACE_ID_LABEL), WORKSPACE_ID);

    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    io.fabric8.kubernetes.api.model.Volume userPodVolume = podSpec.getVolumes().get(0);
    assertEquals(
        userPodVolume.getPersistentVolumeClaim().getClaimName(),
        pvcForUserData.getMetadata().getName());
    assertEquals(
        podSpec.getVolumes().get(0).getPersistentVolumeClaim().getClaimName(),
        pvcForUserData.getMetadata().getName());

    // check container bound to user-defined PVC
    Container container1 = podSpec.getContainers().get(0);
    assertEquals(container1.getVolumeMounts().size(), 1);
    VolumeMount volumeMount = container1.getVolumeMounts().get(0);
    assertEquals(volumeMount.getSubPath(), WORKSPACE_ID + "/userDataPVC/home/user/data");
    assertEquals(volumeMount.getName(), userPodVolume.getName());

    // check container that is bound to Che Volume via Machine configuration
    Container container2 = podSpec.getContainers().get(1);
    VolumeMount cheVolumeMount2 = container2.getVolumeMounts().get(0);
    assertEquals(cheVolumeMount2.getSubPath(), WORKSPACE_ID + "/userDataPVC");
    assertEquals(cheVolumeMount2.getName(), userPodVolume.getName());
  }

  @Test
  public void testProcessingDifferentVolumeMountsBoundToTheSameVolume() throws Exception {
    // given
    k8sEnv = KubernetesEnvironment.builder().build();
    k8sEnv.getPersistentVolumeClaims().put("appStorage", newPVC("appStorage"));

    Pod pod =
        newPod(POD_1_NAME)
            .withContainers(
                newContainer(CONTAINER_1_NAME)
                    .withVolumeMount("appStorage", "/data", "data")
                    .withVolumeMount("appStorage", "/config", "config")
                    .build())
            .withPVCVolume("appStorage", "appStorage")
            .build();

    k8sEnv.addPod(pod);

    k8sEnv
        .getMachines()
        .put(
            MACHINE_1_NAME,
            TestObjects.newMachineConfig().withVolume("appStorage", "/app-storage").build());

    // when
    strategy.provision(k8sEnv, IDENTITY);

    // then
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    PersistentVolumeClaim pvcForUserData =
        findPvc("appStorage", k8sEnv.getPersistentVolumeClaims());
    assertNotNull(pvcForUserData);
    assertTrue(pvcForUserData.getMetadata().getName().startsWith(PVC_NAME_PREFIX));
    assertEquals(
        pvcForUserData.getMetadata().getLabels().get(CHE_WORKSPACE_ID_LABEL), WORKSPACE_ID);

    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    assertEquals(podSpec.getVolumes().size(), 1);
    io.fabric8.kubernetes.api.model.Volume userPodVolume = podSpec.getVolumes().get(0);
    assertEquals(
        userPodVolume.getPersistentVolumeClaim().getClaimName(),
        pvcForUserData.getMetadata().getName());
    assertEquals(
        podSpec.getVolumes().get(0).getPersistentVolumeClaim().getClaimName(),
        pvcForUserData.getMetadata().getName());

    // check container bound to user-defined PVC
    Container container1 = podSpec.getContainers().get(0);
    assertEquals(container1.getVolumeMounts().size(), 3);

    VolumeMount dataVolumeMount = container1.getVolumeMounts().get(0);
    assertEquals(dataVolumeMount.getSubPath(), WORKSPACE_ID + "/appStorage/data");
    assertEquals(dataVolumeMount.getMountPath(), "/data");
    assertEquals(dataVolumeMount.getName(), userPodVolume.getName());

    VolumeMount configVolumeMount = container1.getVolumeMounts().get(1);
    assertEquals(configVolumeMount.getSubPath(), WORKSPACE_ID + "/appStorage/config");
    assertEquals(configVolumeMount.getMountPath(), "/config");
    assertEquals(configVolumeMount.getName(), userPodVolume.getName());

    VolumeMount appStorage = container1.getVolumeMounts().get(2);
    assertEquals(appStorage.getSubPath(), WORKSPACE_ID + "/appStorage");
    assertEquals(appStorage.getMountPath(), "/app-storage");
    assertEquals(appStorage.getName(), userPodVolume.getName());
  }

  @Test
  public void testDoNotProvisionPVCsWhenItIsAlreadyProvisionedForGivenVolumeAndWorkspace()
      throws Exception {
    final String pvcUniqueName1 = PVC_NAME_PREFIX + "-3121";
    PersistentVolumeClaim pvc1 =
        newPVC(pvcUniqueName1, ImmutableMap.of(CHE_VOLUME_NAME_LABEL, VOLUME_1_NAME));
    pvc1.getAdditionalProperties().put("CHE_PROVISIONED", true);
    final String pvcUniqueName2 = PVC_NAME_PREFIX + "-71333";
    PersistentVolumeClaim pvc2 =
        newPVC(pvcUniqueName2, ImmutableMap.of(CHE_VOLUME_NAME_LABEL, VOLUME_2_NAME));
    pvc2.getAdditionalProperties().put("CHE_PROVISIONED", true);
    k8sEnv.getPersistentVolumeClaims().clear();
    k8sEnv.getPersistentVolumeClaims().put(pvcUniqueName1, pvc1);
    k8sEnv.getPersistentVolumeClaims().put(pvcUniqueName2, pvc2);

    strategy.provision(k8sEnv, IDENTITY);

    assertNotNull(k8sEnv.getPersistentVolumeClaims().get(pvcUniqueName1));
    assertNotNull(k8sEnv.getPersistentVolumeClaims().get(pvcUniqueName2));
    assertEquals(pod.getSpec().getVolumes().size(), 2);
    assertEquals(pod.getSpec().getContainers().get(0).getVolumeMounts().size(), 2);
    assertEquals(pod.getSpec().getContainers().get(1).getVolumeMounts().size(), 1);
    assertEquals(pod2.getSpec().getVolumes().size(), 1);
    assertEquals(pod2.getSpec().getContainers().get(0).getVolumeMounts().size(), 1);
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 2);
  }

  @Test
  public void testDoNotProvisionPVCsWhenItAlreadyExistsForGivenVolumeAndWorkspace()
      throws Exception {
    final String pvcUniqueName1 = PVC_NAME_PREFIX + "-3121";
    PersistentVolumeClaim pvc1 =
        newPVC(pvcUniqueName1, ImmutableMap.of(CHE_VOLUME_NAME_LABEL, VOLUME_1_NAME));
    pvc1.getAdditionalProperties().put("CHE_PROVISIONED", true);
    final String pvcUniqueName2 = PVC_NAME_PREFIX + "-71333";
    PersistentVolumeClaim pvc2 =
        newPVC(pvcUniqueName2, ImmutableMap.of(CHE_VOLUME_NAME_LABEL, VOLUME_2_NAME));
    pvc2.getAdditionalProperties().put("CHE_PROVISIONED", true);

    when(pvcs.getByLabel(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID))
        .thenReturn(ImmutableList.of(pvc1, pvc2));

    strategy.provision(k8sEnv, IDENTITY);

    assertEquals(pod.getSpec().getVolumes().size(), 2);
    assertEquals(pod.getSpec().getContainers().get(0).getVolumeMounts().size(), 2);
    assertEquals(pod.getSpec().getContainers().get(1).getVolumeMounts().size(), 1);
    assertEquals(pod2.getSpec().getVolumes().size(), 1);
    assertEquals(pod2.getSpec().getContainers().get(0).getVolumeMounts().size(), 1);
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 2);
    assertTrue(k8sEnv.getPersistentVolumeClaims().containsKey(pvcUniqueName1));
    assertTrue(k8sEnv.getPersistentVolumeClaims().containsKey(pvcUniqueName2));
  }

  @Test
  public void testCreatesProvisionedPVCsOnPrepare() throws Exception {
    final String uniqueName = PVC_NAME_PREFIX + "-3121";
    final PersistentVolumeClaim pvc = mockName(mock(PersistentVolumeClaim.class), uniqueName);
    k8sEnv.getPersistentVolumeClaims().clear();
    k8sEnv.getPersistentVolumeClaims().putAll(singletonMap(uniqueName, pvc));
    doReturn(pvc).when(pvcs).create(any());

    strategy.prepare(k8sEnv, WORKSPACE_ID, 100);

    verify(pvcs).createIfNotExist(any());
    verify(pvcs).waitBound(uniqueName, 100);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenFailedToCreatePVCs() throws Exception {
    final PersistentVolumeClaim pvc = mock(PersistentVolumeClaim.class);
    when(pvc.getMetadata()).thenReturn(new ObjectMetaBuilder().withName(PVC_NAME_PREFIX).build());
    k8sEnv.getPersistentVolumeClaims().clear();
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME_PREFIX, pvc);
    doThrow(InfrastructureException.class).when(pvcs).createIfNotExist(any());

    strategy.prepare(k8sEnv, WORKSPACE_ID, 100);
  }

  @Test
  public void shouldDeletePVCsIfThereIsNoPersistAttributeInWorkspaceConfigWhenCleanupCalled()
      throws Exception {
    // given
    Workspace workspace = mock(Workspace.class);
    lenient().when(workspace.getId()).thenReturn(WORKSPACE_ID);

    WorkspaceConfig workspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(workspace.getConfig()).thenReturn(workspaceConfig);

    Map<String, String> workspaceConfigAttributes = new HashMap<>();
    lenient().when(workspaceConfig.getAttributes()).thenReturn(workspaceConfigAttributes);

    // when
    strategy.cleanup(workspace);

    // then
    verify(pvcs).delete(ImmutableMap.of(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID));
  }

  @Test
  public void shouldDeletePVCsIfPersistAttributeIsSetToTrueInWorkspaceConfigWhenCleanupCalled()
      throws Exception {
    // given
    Workspace workspace = mock(Workspace.class);
    lenient().when(workspace.getId()).thenReturn(WORKSPACE_ID);

    WorkspaceConfig workspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(workspace.getConfig()).thenReturn(workspaceConfig);

    Map<String, String> workspaceConfigAttributes = new HashMap<>();
    lenient().when(workspaceConfig.getAttributes()).thenReturn(workspaceConfigAttributes);
    workspaceConfigAttributes.put(PERSIST_VOLUMES_ATTRIBUTE, "true");

    // when
    strategy.cleanup(workspace);

    // then
    verify(pvcs).delete(any());
  }

  @Test
  public void shouldDoNothingIfPersistAttributeIsSetToFalseInWorkspaceConfigWhenCleanupCalled()
      throws Exception {
    // given
    Workspace workspace = mock(Workspace.class);
    lenient().when(workspace.getId()).thenReturn(WORKSPACE_ID);

    WorkspaceConfig workspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(workspace.getConfig()).thenReturn(workspaceConfig);

    Map<String, String> workspaceConfigAttributes = new HashMap<>();
    lenient().when(workspaceConfig.getAttributes()).thenReturn(workspaceConfigAttributes);
    workspaceConfigAttributes.put(PERSIST_VOLUMES_ATTRIBUTE, "false");

    // when
    strategy.cleanup(workspace);

    // then
    verify(pvcs, never()).delete(any());
  }

  private static PersistentVolumeClaim newPVC(String name) {
    return newPVC(name, new HashMap<>());
  }

  private static PersistentVolumeClaim newPVC(String name, Map<String, String> labels) {
    return new PersistentVolumeClaimBuilder()
        .withNewMetadata()
        .withName(name)
        .withLabels(labels)
        .endMetadata()
        .withNewSpec()
        .endSpec()
        .build();
  }

  private PersistentVolumeClaim findPvc(
      String volumeName, Map<String, PersistentVolumeClaim> claims) {
    return claims
        .values()
        .stream()
        .filter(c -> volumeName.equals(c.getMetadata().getLabels().get(CHE_VOLUME_NAME_LABEL)))
        .findAny()
        .orElse(null);
  }
}
