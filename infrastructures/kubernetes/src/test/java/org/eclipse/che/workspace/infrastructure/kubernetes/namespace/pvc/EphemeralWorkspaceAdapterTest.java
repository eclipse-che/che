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

import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EmptyDirVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link EphemeralWorkspaceAdapter}.
 *
 * @author Ilya Buziuk
 * @author Angel Misevski
 */
@Listeners(MockitoTestNGListener.class)
public class EphemeralWorkspaceAdapterTest {
  private static final String EPHEMERAL_WORKSPACE_ID = "workspace123";
  private static final String NON_EPHEMERAL_WORKSPACE_ID = "workspace234";
  private static final String POD_NAME = "pod1";
  private static final String VOL1_ID = "vol1";
  private static final String VOL2_ID = "vol2";

  @Mock private Workspace nonEphemeralWorkspace;
  @Mock private Workspace ephemeralWorkspace;

  private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private Map<String, InternalMachineConfig> machines;
  @Mock private InternalMachineConfig machine;

  private EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;

  private Map<String, Volume> volumes;

  @BeforeMethod
  public void setup() throws Exception {
    ephemeralWorkspaceAdapter = new EphemeralWorkspaceAdapter();

    // ephemeral workspace configuration
    lenient().when(ephemeralWorkspace.getId()).thenReturn(EPHEMERAL_WORKSPACE_ID);
    WorkspaceConfig ephemeralWorkspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(ephemeralWorkspace.getConfig()).thenReturn(ephemeralWorkspaceConfig);
    Map<String, String> ephemeralConfigAttributes =
        Collections.singletonMap(PERSIST_VOLUMES_ATTRIBUTE, "false");
    lenient().when(ephemeralWorkspaceConfig.getAttributes()).thenReturn(ephemeralConfigAttributes);

    // regular / non-ephemeral workspace configuration
    lenient().when(nonEphemeralWorkspace.getId()).thenReturn(NON_EPHEMERAL_WORKSPACE_ID);
    WorkspaceConfig nonEphemeralWorkspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(nonEphemeralWorkspace.getConfig()).thenReturn(nonEphemeralWorkspaceConfig);
    Map<String, String> nonEphemeralConfigAttributes = Collections.emptyMap();
    lenient().when(nonEphemeralWorkspace.getAttributes()).thenReturn(nonEphemeralConfigAttributes);

    k8sEnv = KubernetesEnvironment.builder().build();

    k8sEnv.setMachines(machines);
    lenient().when(machines.get(anyString())).thenReturn(machine);

    volumes =
        ImmutableMap.of(
            VOL1_ID, new VolumeImpl().withPath(VOL1_ID),
            VOL2_ID, new VolumeImpl().withPath(VOL2_ID));
    lenient().when(machine.getVolumes()).thenReturn(volumes);
  }

  @Test
  public void testIsEphemeralWorkspace() throws Exception {
    assertTrue(EphemeralWorkspaceUtility.isEphemeral(ephemeralWorkspace));
    assertFalse(EphemeralWorkspaceUtility.isEphemeral(nonEphemeralWorkspace));
  }

  @Test
  public void testReplacingUserDefinedPVCsWithWithEmptyDirVolumes() throws Exception {
    // given
    k8sEnv.getPersistentVolumeClaims().put("pvc1", mock(PersistentVolumeClaim.class));
    k8sEnv.getPersistentVolumeClaims().put("pvc2", mock(PersistentVolumeClaim.class));

    io.fabric8.kubernetes.api.model.Volume configMapVolume =
        new VolumeBuilder().withNewConfigMap().withName("configMap").endConfigMap().build();
    io.fabric8.kubernetes.api.model.Volume emptyDirVolume =
        new VolumeBuilder().withNewEmptyDir().endEmptyDir().build();
    io.fabric8.kubernetes.api.model.Volume pvcVolume =
        new VolumeBuilder()
            .withNewPersistentVolumeClaim()
            .withClaimName("pvc1")
            .endPersistentVolumeClaim()
            .build();
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(POD_NAME)
            .endMetadata()
            .withNewSpec()
            .withVolumes(
                new VolumeBuilder(pvcVolume).build(),
                new VolumeBuilder(configMapVolume).build(),
                new VolumeBuilder(emptyDirVolume).build())
            .endSpec()
            .build();

    k8sEnv.addPod(pod);

    // when
    ephemeralWorkspaceAdapter.provision(k8sEnv, runtimeIdentity);

    // then
    assertTrue(k8sEnv.getPersistentVolumeClaims().isEmpty());
    assertNull(pod.getSpec().getVolumes().get(0).getPersistentVolumeClaim());
    assertEquals(pod.getSpec().getVolumes().get(0).getEmptyDir(), new EmptyDirVolumeSource());
    assertEquals(pod.getSpec().getVolumes().get(1), configMapVolume);
    assertEquals(pod.getSpec().getVolumes().get(2), emptyDirVolume);
  }

  @Test
  public void testMatchingUserDefinedPVCsWithCheVolumes() throws Exception {
    // given
    k8sEnv.getPersistentVolumeClaims().put("pvc1", mock(PersistentVolumeClaim.class));

    io.fabric8.kubernetes.api.model.Volume pvcVolume =
        new VolumeBuilder()
            .withName("pvc1volume")
            .withNewPersistentVolumeClaim()
            .withClaimName("pvc1")
            .endPersistentVolumeClaim()
            .build();
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName("pod")
            .endMetadata()
            .withNewSpec()
            .withContainers(
                new ContainerBuilder()
                    .withName("c1")
                    .withVolumeMounts(new VolumeMountBuilder().withName("pvc1volume").build())
                    .build(),
                new ContainerBuilder().withName("c2").build())
            .withVolumes(new VolumeBuilder(pvcVolume).build())
            .endSpec()
            .build();
    k8sEnv.addPod(pod);

    k8sEnv.setMachines(new HashMap<>());
    k8sEnv.getMachines().put("pod/c1", new InternalMachineConfig());
    InternalMachineConfig machine2 = new InternalMachineConfig();
    machine2.getVolumes().put("pvc1", new VolumeImpl().withPath("/tmp/pvc1"));
    k8sEnv.getMachines().put("pod/c2", machine2);

    // when
    ephemeralWorkspaceAdapter.provision(k8sEnv, runtimeIdentity);

    // then
    assertTrue(k8sEnv.getPersistentVolumeClaims().isEmpty());
    List<io.fabric8.kubernetes.api.model.Volume> podVolumes = pod.getSpec().getVolumes();
    assertEquals(podVolumes.size(), 1);
    io.fabric8.kubernetes.api.model.Volume podVolume = podVolumes.get(0);
    assertNull(podVolume.getPersistentVolumeClaim());
    assertEquals(podVolume.getEmptyDir(), new EmptyDirVolumeSource());

    Container c1 = pod.getSpec().getContainers().get(0);
    VolumeMount c1VolumeMount = c1.getVolumeMounts().get(0);
    assertEquals(c1VolumeMount.getName(), podVolume.getName());

    Container c2 = pod.getSpec().getContainers().get(1);
    VolumeMount c2VolumeMount = c2.getVolumeMounts().get(0);
    assertEquals(c2VolumeMount.getName(), podVolume.getName());
  }

  @Test
  public void testEmptyDirVolumeMountsAdded() throws Exception {
    Container container = new Container();
    Pod pod = buildPod(POD_NAME, container);
    k8sEnv.addPod(pod);

    ephemeralWorkspaceAdapter.provision(k8sEnv, runtimeIdentity);

    Container podContainer = pod.getSpec().getContainers().get(0);
    assertEquals(podContainer.getVolumeMounts().size(), volumes.values().size());
    assertEquals(pod.getSpec().getVolumes().size(), volumes.values().size());
    assertTrue(pod.getSpec().getVolumes().stream().allMatch(vol -> (vol.getEmptyDir() != null)));
  }

  @Test
  public void testEmptyDirVolumeMountsSharedBetweenContainers() throws Exception {
    Container container1 = new Container();
    Container container2 = new Container();
    Pod pod = buildPod(POD_NAME, container1, container2);
    k8sEnv.addPod(pod);

    ephemeralWorkspaceAdapter.provision(k8sEnv, runtimeIdentity);

    List<Container> podContainers = pod.getSpec().getContainers();
    List<VolumeMount> container1Mounts = podContainers.get(0).getVolumeMounts();
    List<VolumeMount> container2Mounts = podContainers.get(1).getVolumeMounts();
    assertTrue(container1Mounts.stream().allMatch(container2Mounts::contains));
    assertEquals(pod.getSpec().getVolumes().size(), volumes.size());

    List<String> podVolumeNames =
        pod.getSpec()
            .getVolumes()
            .stream()
            .map(io.fabric8.kubernetes.api.model.Volume::getName)
            .collect(Collectors.toList());
    assertTrue(
        container1Mounts.stream().map(VolumeMount::getName).allMatch(podVolumeNames::contains));
  }

  @Test
  public void testInitContainerMountsIncluded() throws Exception {
    Container container1 = new Container();
    Container container2 = new Container();
    Container initContainer = new Container();
    Pod pod = buildPod(POD_NAME, container1, container2);
    pod.getSpec().setInitContainers(ImmutableList.of(initContainer));

    k8sEnv.addPod(pod);

    ephemeralWorkspaceAdapter.provision(k8sEnv, runtimeIdentity);

    List<Container> podInitContainers = pod.getSpec().getInitContainers();
    assertEquals(podInitContainers.size(), 1);
    Container podInitContainer = podInitContainers.iterator().next();
    List<VolumeMount> initContainerVolumeMounts = podInitContainer.getVolumeMounts();
    assertEquals(initContainerVolumeMounts.size(), 2);
  }

  private Pod buildPod(String name, Container... containers) throws Exception {
    return new PodBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withContainers(containers)
        .endSpec()
        .build();
  }
}
