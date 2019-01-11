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

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.SUBPATHS_PROPERTY_FMT;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.TestObjects.newContainer;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.TestObjects.newPod;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
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
import java.util.concurrent.CompletableFuture;
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
 * Tests {@link CommonPVCStrategy}.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class CommonPVCStrategyTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final String PVC_NAME = "che-claim";

  private static final String POD_1_NAME = "main";
  private static final String CONTAINER_1_NAME = "app";
  private static final String CONTAINER_2_NAME = "db";
  private static final String MACHINE_1_NAME = POD_1_NAME + '/' + CONTAINER_1_NAME;
  private static final String MACHINE_2_NAME = POD_1_NAME + '/' + CONTAINER_2_NAME;

  private static final String POD_2_NAME = "second";
  private static final String CONTAINER_NAME_3 = "app2";
  private static final String MACHINE_3_NAME = POD_2_NAME + '/' + CONTAINER_NAME_3;

  private static final String VOLUME_1_NAME = "vol1";
  private static final String VOLUME_2_NAME = "vol2";

  private static final String PVC_QUANTITY = "10Gi";
  private static final String PVC_ACCESS_MODE = "RWO";

  private static final String[] WORKSPACE_SUBPATHS = {"/projects", "/logs"};

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "id1");

  private KubernetesEnvironment k8sEnv;

  private Pod pod;
  private Pod pod2;

  @Mock private PVCSubPathHelper pvcSubPathHelper;

  @Mock private KubernetesNamespaceFactory factory;
  @Mock private KubernetesNamespace k8sNamespace;
  @Mock private KubernetesPersistentVolumeClaims pvcs;

  @Mock private EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;

  private CommonPVCStrategy commonPVCStrategy;

  @BeforeMethod
  public void setup() throws Exception {
    commonPVCStrategy =
        new CommonPVCStrategy(
            PVC_NAME,
            PVC_QUANTITY,
            PVC_ACCESS_MODE,
            true,
            pvcSubPathHelper,
            factory,
            ephemeralWorkspaceAdapter);

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

    pod2 = newPod(POD_2_NAME).withContainers(newContainer(CONTAINER_NAME_3).build()).build();

    k8sEnv.addPod(pod);
    k8sEnv.addPod(pod2);

    lenient().doNothing().when(pvcSubPathHelper).execute(any(), any(), any());
    lenient()
        .doReturn(CompletableFuture.completedFuture(null))
        .when(pvcSubPathHelper)
        .removeDirsAsync(anyString(), any(String.class));

    lenient().when(factory.create(WORKSPACE_ID)).thenReturn(k8sNamespace);
    lenient().when(k8sNamespace.persistentVolumeClaims()).thenReturn(pvcs);
  }

  @Test
  public void testProvisionVolumesIntoKubernetesEnvironment() throws Exception {
    commonPVCStrategy.provision(k8sEnv, IDENTITY);

    // 2 volumes in machine1
    assertFalse(pod.getSpec().getVolumes().isEmpty());
    assertFalse(pod.getSpec().getContainers().get(0).getVolumeMounts().isEmpty());
    assertFalse(pod.getSpec().getContainers().get(1).getVolumeMounts().isEmpty());
    assertFalse(pod2.getSpec().getVolumes().isEmpty());
    assertFalse(pod2.getSpec().getContainers().get(0).getVolumeMounts().isEmpty());
    assertFalse(k8sEnv.getPersistentVolumeClaims().isEmpty());
    assertTrue(k8sEnv.getPersistentVolumeClaims().containsKey(PVC_NAME));
  }

  @Test
  public void testReplacePVCWhenItsAlreadyInKubernetesEnvironment() throws Exception {
    final PersistentVolumeClaim provisioned = mock(PersistentVolumeClaim.class);
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME, provisioned);

    commonPVCStrategy.provision(k8sEnv, IDENTITY);

    assertNotEquals(k8sEnv.getPersistentVolumeClaims().get(PVC_NAME), provisioned);
  }

  @Test
  public void testProvisionVolumesWithSubpathsIntoKubernetesEnvironment() throws Exception {
    commonPVCStrategy.provision(k8sEnv, IDENTITY);

    final Map<String, PersistentVolumeClaim> actual = k8sEnv.getPersistentVolumeClaims();
    assertFalse(actual.isEmpty());
    assertTrue(actual.containsKey(PVC_NAME));
    assertEquals(
        (String[])
            actual
                .get(PVC_NAME)
                .getAdditionalProperties()
                .get(format(SUBPATHS_PROPERTY_FMT, WORKSPACE_ID)),
        new String[] {expectedVolumeDir(VOLUME_1_NAME), expectedVolumeDir(VOLUME_2_NAME)});
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
    commonPVCStrategy.provision(k8sEnv, IDENTITY);

    // then
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    assertNotNull(k8sEnv.getPersistentVolumeClaims().get(PVC_NAME));

    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    io.fabric8.kubernetes.api.model.Volume userPodVolume = podSpec.getVolumes().get(0);
    assertEquals(userPodVolume.getPersistentVolumeClaim().getClaimName(), PVC_NAME);
    assertEquals(podSpec.getVolumes().get(0).getPersistentVolumeClaim().getClaimName(), PVC_NAME);

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
    commonPVCStrategy.provision(k8sEnv, IDENTITY);

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
    commonPVCStrategy.provision(k8sEnv, IDENTITY);

    // then
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    assertNotNull(k8sEnv.getPersistentVolumeClaims().get(PVC_NAME));

    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    io.fabric8.kubernetes.api.model.Volume userPodVolume = podSpec.getVolumes().get(0);
    assertEquals(userPodVolume.getPersistentVolumeClaim().getClaimName(), PVC_NAME);
    assertEquals(podSpec.getVolumes().get(0).getPersistentVolumeClaim().getClaimName(), PVC_NAME);

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
    commonPVCStrategy.provision(k8sEnv, IDENTITY);

    // then
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    assertNotNull(k8sEnv.getPersistentVolumeClaims().get(PVC_NAME));

    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    assertEquals(podSpec.getVolumes().size(), 1);
    io.fabric8.kubernetes.api.model.Volume userPodVolume = podSpec.getVolumes().get(0);
    assertEquals(userPodVolume.getPersistentVolumeClaim().getClaimName(), PVC_NAME);
    assertEquals(podSpec.getVolumes().get(0).getPersistentVolumeClaim().getClaimName(), PVC_NAME);

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
  public void testDoNotAddsSubpathsWhenPreCreationIsNotNeeded() throws Exception {
    commonPVCStrategy =
        new CommonPVCStrategy(
            PVC_NAME,
            PVC_QUANTITY,
            PVC_ACCESS_MODE,
            false,
            pvcSubPathHelper,
            factory,
            ephemeralWorkspaceAdapter);

    commonPVCStrategy.provision(k8sEnv, IDENTITY);

    final Map<String, PersistentVolumeClaim> actual = k8sEnv.getPersistentVolumeClaims();
    assertFalse(actual.isEmpty());
    assertTrue(actual.containsKey(PVC_NAME));
    assertFalse(
        actual
            .get(PVC_NAME)
            .getAdditionalProperties()
            .containsKey(format(SUBPATHS_PROPERTY_FMT, WORKSPACE_ID)));
  }

  @Test
  public void testCreatesPVCsWithSubpathsOnPrepare() throws Exception {
    final PersistentVolumeClaim pvc = mockName(mock(PersistentVolumeClaim.class), PVC_NAME);
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME, pvc);
    final Map<String, Object> subPaths = new HashMap<>();
    subPaths.put(format(SUBPATHS_PROPERTY_FMT, WORKSPACE_ID), WORKSPACE_SUBPATHS);
    when(pvc.getAdditionalProperties()).thenReturn(subPaths);
    doNothing().when(pvcSubPathHelper).createDirs(WORKSPACE_ID, WORKSPACE_SUBPATHS);

    commonPVCStrategy.prepare(k8sEnv, WORKSPACE_ID, 100);

    verify(pvcs).get();
    verify(pvcs).create(pvc);
    verify(pvcs).waitBound(PVC_NAME, 100);
    verify(pvcSubPathHelper).createDirs(any(), any());
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenFailedToGetExistingPVCs() throws Exception {
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME, mock(PersistentVolumeClaim.class));
    doThrow(InfrastructureException.class).when(pvcs).get();

    commonPVCStrategy.prepare(k8sEnv, WORKSPACE_ID, 100);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenPVCCreationFailed() throws Exception {
    final PersistentVolumeClaim claim = mockName(mock(PersistentVolumeClaim.class), PVC_NAME);
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME, claim);
    when(pvcs.get()).thenReturn(emptyList());
    doThrow(InfrastructureException.class).when(pvcs).create(any());

    commonPVCStrategy.prepare(k8sEnv, WORKSPACE_ID, 100);
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
    commonPVCStrategy.cleanup(workspace);

    // then
    verify(pvcSubPathHelper).removeDirsAsync(WORKSPACE_ID, WORKSPACE_ID);
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
    commonPVCStrategy.cleanup(workspace);

    // then
    verify(pvcSubPathHelper).removeDirsAsync(WORKSPACE_ID, WORKSPACE_ID);
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
    commonPVCStrategy.cleanup(workspace);

    // then
    verify(pvcSubPathHelper, never()).removeDirsAsync(WORKSPACE_ID, WORKSPACE_ID);
  }

  private static PersistentVolumeClaim newPVC(String name) {
    return new PersistentVolumeClaimBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .endSpec()
        .build();
  }

  static <T extends HasMetadata> T mockName(T obj, String name) {
    final ObjectMeta objectMeta = mock(ObjectMeta.class);
    lenient().when(obj.getMetadata()).thenReturn(objectMeta);
    lenient().when(objectMeta.getName()).thenReturn(name);
    return obj;
  }

  private static String expectedVolumeDir(String volumeName) {
    return WORKSPACE_ID + '/' + volumeName;
  }
}
