/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project.pvc;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.openshift.project.pvc.CommonPVCStrategyTest.mockName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftPersistentVolumeClaims;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;
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
  private static final String PROJECT_NAME = "che";
  private static final String PVC_NAME_PREFIX = "che-claim";
  private static final String PVC_UNIQUE_NAME = PVC_NAME_PREFIX + '-' + WORKSPACE_ID;
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
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "usr1");

  @Mock private OpenShiftEnvironment osEnv;
  @Mock private OpenShiftClientFactory clientFactory;
  @Mock private OpenShiftClient client;
  @Mock private OpenShiftProjectFactory factory;
  @Mock private OpenShiftProject osProject;
  @Mock private OpenShiftPersistentVolumeClaims pvcs;
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
            PROJECT_NAME, PVC_NAME_PREFIX, PVC_QUANTITY, PVC_ACCESS_MODE, factory, clientFactory);
    when(clientFactory.create()).thenReturn(client);

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
    when(osEnv.getMachines()).thenReturn(machines);

    Map<String, Pod> pods = new HashMap<>();
    pods.put(POD_NAME, pod);
    pods.put(POD_NAME_2, pod2);
    when(osEnv.getPods()).thenReturn(pods);

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

    when(factory.create(WORKSPACE_ID)).thenReturn(osProject);
    when(osProject.persistentVolumeClaims()).thenReturn(pvcs);

    mockName(pod, POD_NAME);
    mockName(pod2, POD_NAME_2);
  }

  @Test
  public void testReplacePVCWhenItsAlreadyInOpenShiftEnvironment() throws Exception {
    final Map<String, PersistentVolumeClaim> claims = new HashMap<>();
    final PersistentVolumeClaim provisioned = mock(PersistentVolumeClaim.class);
    claims.put(PVC_UNIQUE_NAME + '-' + VOLUME_1_NAME, provisioned);
    when(osEnv.getPersistentVolumeClaims()).thenReturn(claims);

    strategy.provision(osEnv, IDENTITY);

    assertNotEquals(
        osEnv.getPersistentVolumeClaims().get(PVC_UNIQUE_NAME + '-' + VOLUME_1_NAME), provisioned);
  }

  @Test
  public void testProvisionPVCWithUniqueName() throws Exception {
    when(osEnv.getPersistentVolumeClaims()).thenReturn(new HashMap<>());

    strategy.provision(osEnv, IDENTITY);

    assertEquals(podSpec.getVolumes().size(), 2);
    assertEquals(podSpec2.getVolumes().size(), 1);
    assertEquals(container.getVolumeMounts().size(), 2);
    assertEquals(container2.getVolumeMounts().size(), 1);
    assertEquals(container3.getVolumeMounts().size(), 1);
    assertEquals(osEnv.getPersistentVolumeClaims().size(), 2);
    assertTrue(
        osEnv.getPersistentVolumeClaims().containsKey(PVC_UNIQUE_NAME + '-' + VOLUME_1_NAME));
    assertTrue(
        osEnv.getPersistentVolumeClaims().containsKey(PVC_UNIQUE_NAME + '-' + VOLUME_2_NAME));
  }

  @Test
  public void testCreatesPVCsOnPrepare() throws Exception {
    final PersistentVolumeClaim pvc = mockName(mock(PersistentVolumeClaim.class), PVC_NAME_PREFIX);
    when(osEnv.getPersistentVolumeClaims()).thenReturn(singletonMap(PVC_NAME_PREFIX, pvc));
    doNothing().when(pvcs).createIfNotExist(any());

    strategy.prepare(osEnv, WORKSPACE_ID);

    verify(pvcs).createIfNotExist(any());
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenFailedToCreatePVCs() throws Exception {
    final PersistentVolumeClaim pvc = mockName(mock(PersistentVolumeClaim.class), PVC_NAME_PREFIX);
    when(osEnv.getPersistentVolumeClaims()).thenReturn(singletonMap(PVC_NAME_PREFIX, pvc));
    doThrow(InfrastructureException.class).when(pvcs).createIfNotExist(any());

    strategy.prepare(osEnv, WORKSPACE_ID);
  }

  @Test
  public void testRemovesPVCWhenCleanupCalled() throws Exception {
    final MixedOperation mixedOperation = mock(MixedOperation.class);
    final NonNamespaceOperation namespace = mock(NonNamespaceOperation.class);
    final Resource resource = mock(Resource.class);
    doReturn(mixedOperation).when(client).persistentVolumeClaims();
    doReturn(namespace).when(mixedOperation).inNamespace(PROJECT_NAME);
    doReturn(resource).when(namespace).withName(PVC_NAME_PREFIX + '-' + WORKSPACE_ID);
    when(resource.delete()).thenReturn(true);

    strategy.cleanup(WORKSPACE_ID);

    verify(resource).delete();
  }

  @Test
  public void testDoNothingWhenNoPVCFoundInNamespaceOnCleanup() throws Exception {
    final MixedOperation mixedOperation = mock(MixedOperation.class);
    final NonNamespaceOperation namespace = mock(NonNamespaceOperation.class);
    final Resource resource = mock(Resource.class);
    doReturn(mixedOperation).when(client).persistentVolumeClaims();
    doReturn(namespace).when(mixedOperation).inNamespace(PROJECT_NAME);
    doReturn(resource).when(namespace).withName(PVC_NAME_PREFIX + '-' + WORKSPACE_ID);
    when(resource.delete()).thenReturn(false);

    strategy.cleanup(WORKSPACE_ID);

    verify(resource).delete();
  }
}
