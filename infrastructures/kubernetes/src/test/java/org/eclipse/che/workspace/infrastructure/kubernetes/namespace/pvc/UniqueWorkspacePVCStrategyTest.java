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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_VOLUME_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategyTest.mockName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
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
 */
@Listeners(MockitoTestNGListener.class)
public class UniqueWorkspacePVCStrategyTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final String NAMESPACE_NAME = "che";
  private static final String PVC_NAME_PREFIX = "che-claim";
  private static final String POD_NAME = "main";
  private static final String POD_NAME_2 = "second";
  private static final String CONTAINER_NAME = "app";
  private static final String CONTAINER_NAME_2 = "db";
  private static final String CONTAINER_NAME_3 = "app2";
  private static final String MACHINE_NAME = POD_NAME + '/' + CONTAINER_NAME;
  private static final String MACHINE_NAME_2 = POD_NAME + '/' + CONTAINER_NAME_2;
  private static final String MACHINE_NAME_3 = POD_NAME_2 + '/' + CONTAINER_NAME_3;
  private static final String PVC_QUANTITY = "10Gi";
  private static final String PVC_ACCESS_MODE = "RWO";
  private static final String VOLUME_1_NAME = "vol1";
  private static final String VOLUME_2_NAME = "vol2";

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "id1");

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private KubernetesClientFactory clientFactory;
  @Mock private KubernetesClient client;
  @Mock private KubernetesNamespaceFactory factory;
  @Mock private KubernetesNamespace k8sNamespace;
  @Mock private KubernetesPersistentVolumeClaims pvcs;
  @Mock private Pod pod;
  @Mock private Pod pod2;
  @Mock private PodSpec podSpec;
  @Mock private PodSpec podSpec2;
  @Mock private Container container;
  @Mock private Container container2;
  @Mock private Container container3;

  private UniqueWorkspacePVCStrategy strategy;

  @BeforeMethod
  public void setup() throws Exception {
    strategy =
        new UniqueWorkspacePVCStrategy(
            NAMESPACE_NAME, PVC_NAME_PREFIX, PVC_QUANTITY, PVC_ACCESS_MODE, factory, clientFactory);
    when(clientFactory.create()).thenReturn(client);
    when(clientFactory.create(anyString())).thenReturn(client);

    Map<String, InternalMachineConfig> machines = new HashMap<>();
    InternalMachineConfig machine1 = mock(InternalMachineConfig.class);
    Map<String, Volume> volumes1 = new HashMap<>();
    volumes1.put(VOLUME_1_NAME, new VolumeImpl().withPath("/path"));
    volumes1.put(VOLUME_2_NAME, new VolumeImpl().withPath("/path2"));
    when(machine1.getVolumes()).thenReturn(volumes1);
    machines.put(MACHINE_NAME, machine1);
    InternalMachineConfig machine2 = mock(InternalMachineConfig.class);
    Map<String, Volume> volumes2 = new HashMap<>();
    volumes2.put(VOLUME_2_NAME, new VolumeImpl().withPath("/path2"));
    when(machine2.getVolumes()).thenReturn(volumes2);
    machines.put(MACHINE_NAME_2, machine2);
    InternalMachineConfig machine3 = mock(InternalMachineConfig.class);
    Map<String, Volume> volumes3 = new HashMap<>();
    volumes3.put(VOLUME_1_NAME, new VolumeImpl().withPath("/path"));
    when(machine3.getVolumes()).thenReturn(volumes3);
    machines.put(MACHINE_NAME_3, machine3);
    when(k8sEnv.getMachines()).thenReturn(machines);

    Map<String, Pod> pods = new HashMap<>();
    pods.put(POD_NAME, pod);
    pods.put(POD_NAME_2, pod2);
    when(k8sEnv.getPods()).thenReturn(pods);

    when(pod.getSpec()).thenReturn(podSpec);
    when(pod2.getSpec()).thenReturn(podSpec2);
    when(podSpec.getContainers()).thenReturn(asList(container, container2));
    when(podSpec2.getContainers()).thenReturn(singletonList(container3));
    when(podSpec.getVolumes()).thenReturn(new ArrayList<>());
    when(podSpec2.getVolumes()).thenReturn(new ArrayList<>());
    when(container.getName()).thenReturn(CONTAINER_NAME);
    when(container2.getName()).thenReturn(CONTAINER_NAME_2);
    when(container3.getName()).thenReturn(CONTAINER_NAME_3);
    when(container.getVolumeMounts()).thenReturn(new ArrayList<>());
    when(container2.getVolumeMounts()).thenReturn(new ArrayList<>());
    when(container3.getVolumeMounts()).thenReturn(new ArrayList<>());

    when(factory.create(WORKSPACE_ID)).thenReturn(k8sNamespace);
    when(k8sNamespace.persistentVolumeClaims()).thenReturn(pvcs);

    mockName(pod, POD_NAME);
    mockName(pod2, POD_NAME_2);
  }

  @Test
  public void testProvisionPVCsForEachVolumeWithUniqueName() throws Exception {
    when(k8sEnv.getPersistentVolumeClaims()).thenReturn(new HashMap<>());

    strategy.provision(k8sEnv, IDENTITY);

    assertEquals(podSpec.getVolumes().size(), 2);
    assertEquals(podSpec2.getVolumes().size(), 1);
    assertEquals(container.getVolumeMounts().size(), 2);
    assertEquals(container2.getVolumeMounts().size(), 1);
    assertEquals(container3.getVolumeMounts().size(), 1);
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 2);
    for (PersistentVolumeClaim pvc : k8sEnv.getPersistentVolumeClaims().values()) {
      String volumeName = pvc.getMetadata().getLabels().get(CHE_VOLUME_NAME_LABEL);
      assertNotNull(volumeName);
      assertTrue(volumeName.equals(VOLUME_1_NAME) || volumeName.equals(VOLUME_2_NAME));
    }
  }

  @Test
  public void testDoNotProvisionPVCsWhenItIsAlreadyProvisionedForGivenVolumeAndWorkspace()
      throws Exception {
    final String pvcUniqueName1 = PVC_NAME_PREFIX + "-3121";
    PersistentVolumeClaim pvc1 =
        mockPVC(ImmutableMap.of(CHE_VOLUME_NAME_LABEL, VOLUME_1_NAME), pvcUniqueName1);
    final String pvcUniqueName2 = PVC_NAME_PREFIX + "-71333";
    PersistentVolumeClaim pvc2 =
        mockPVC(ImmutableMap.of(CHE_VOLUME_NAME_LABEL, VOLUME_2_NAME), pvcUniqueName2);
    when(k8sEnv.getPersistentVolumeClaims())
        .thenReturn(ImmutableMap.of(pvcUniqueName1, pvc1, pvcUniqueName2, pvc2));

    strategy.provision(k8sEnv, IDENTITY);

    assertEquals(podSpec.getVolumes().size(), 2);
    assertEquals(podSpec2.getVolumes().size(), 1);
    assertEquals(container.getVolumeMounts().size(), 2);
    assertEquals(container2.getVolumeMounts().size(), 1);
    assertEquals(container3.getVolumeMounts().size(), 1);
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 2);
  }

  @Test
  public void testDoNotProvisionPVCsWhenItIsAlreadyExistsForGivenVolumeAndWorkspace()
      throws Exception {
    final String pvcUniqueName1 = PVC_NAME_PREFIX + "-3121";
    PersistentVolumeClaim pvc1 =
        mockPVC(ImmutableMap.of(CHE_VOLUME_NAME_LABEL, VOLUME_1_NAME), pvcUniqueName1);
    final String pvcUniqueName2 = PVC_NAME_PREFIX + "-71333";
    PersistentVolumeClaim pvc2 =
        mockPVC(ImmutableMap.of(CHE_VOLUME_NAME_LABEL, VOLUME_2_NAME), pvcUniqueName2);
    when(pvcs.getByLabel(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID))
        .thenReturn(ImmutableList.of(pvc1, pvc2));

    strategy.provision(k8sEnv, IDENTITY);

    assertEquals(podSpec.getVolumes().size(), 2);
    assertEquals(podSpec2.getVolumes().size(), 1);
    assertEquals(container.getVolumeMounts().size(), 2);
    assertEquals(container2.getVolumeMounts().size(), 1);
    assertEquals(container3.getVolumeMounts().size(), 1);
    assertTrue(k8sEnv.getPersistentVolumeClaims().isEmpty());
  }

  @Test
  public void testCreatesProvisionedPVCsOnPrepare() throws Exception {
    final String uniqueName = PVC_NAME_PREFIX + "-3121";
    final PersistentVolumeClaim pvc = mockName(mock(PersistentVolumeClaim.class), uniqueName);
    when(k8sEnv.getPersistentVolumeClaims()).thenReturn(singletonMap(uniqueName, pvc));
    doReturn(pvc).when(pvcs).create(any());

    strategy.prepare(k8sEnv, WORKSPACE_ID);

    verify(pvcs).create(any());
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenFailedToCreatePVCs() throws Exception {
    final PersistentVolumeClaim pvc = mock(PersistentVolumeClaim.class);
    when(k8sEnv.getPersistentVolumeClaims()).thenReturn(singletonMap(PVC_NAME_PREFIX, pvc));
    doThrow(InfrastructureException.class).when(pvcs).create(any(PersistentVolumeClaim.class));

    strategy.prepare(k8sEnv, WORKSPACE_ID);
  }

  @Test
  public void testRemovesPVCWhenCleanupCalled() throws Exception {
    final MixedOperation mixedOperation = mock(MixedOperation.class);
    final NonNamespaceOperation namespace = mock(NonNamespaceOperation.class);
    final FilterWatchListDeletable filterList = mock(FilterWatchListDeletable.class);
    doReturn(mixedOperation).when(client).persistentVolumeClaims();
    doReturn(namespace).when(mixedOperation).inNamespace(NAMESPACE_NAME);
    doReturn(filterList).when(namespace).withLabel(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID);
    when(filterList.delete()).thenReturn(true);

    strategy.cleanup(WORKSPACE_ID);

    verify(filterList).delete();
  }

  @Test
  public void testDoNothingWhenNoPVCFoundInNamespaceOnCleanup() throws Exception {
    final MixedOperation mixedOperation = mock(MixedOperation.class);
    final NonNamespaceOperation namespace = mock(NonNamespaceOperation.class);
    final FilterWatchListDeletable filterList = mock(FilterWatchListDeletable.class);
    doReturn(mixedOperation).when(client).persistentVolumeClaims();
    doReturn(namespace).when(mixedOperation).inNamespace(NAMESPACE_NAME);
    doReturn(filterList).when(namespace).withLabel(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID);
    when(filterList.delete()).thenReturn(false);

    strategy.cleanup(WORKSPACE_ID);

    verify(filterList).delete();
  }

  private static PersistentVolumeClaim mockPVC(Map<String, String> labels, String name) {
    final PersistentVolumeClaim pvc = mock(PersistentVolumeClaim.class);
    final ObjectMeta metadata = mock(ObjectMeta.class);
    when(pvc.getMetadata()).thenReturn(metadata);
    when(metadata.getLabels()).thenReturn(labels);
    when(metadata.getName()).thenReturn(name);
    return pvc;
  }
}
