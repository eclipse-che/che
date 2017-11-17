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

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.workspace.infrastructure.openshift.project.pvc.CommonPVCStrategyTest.mockName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
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
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
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
  private static final String PVC_NAME = "che-claim";
  private static final String PVC_UNIQUE_NAME = PVC_NAME + '-' + WORKSPACE_ID;
  private static final String POD_NAME = "main";
  private static final String CONTAINER_NAME = "app";
  private static final String MACHINE_NAME = POD_NAME + '/' + CONTAINER_NAME;
  private static final String PVC_QUANTITY = "10Gi";
  private static final String PVC_ACCESS_MODE = "RWO";
  private static final String PROJECT_FOLDER_PATH = "/projects";

  @Mock private InternalEnvironment env;
  @Mock private OpenShiftEnvironment osEnv;
  @Mock private OpenShiftClientFactory clientFactory;
  @Mock private OpenShiftClient client;
  @Mock private OpenShiftProjectFactory factory;
  @Mock private OpenShiftProject osProject;
  @Mock private OpenShiftPersistentVolumeClaims osPVCs;
  @Mock private Pod pod;
  @Mock private PodSpec podSpec;
  @Mock private Container container;

  private UniqueWorkspacePVCStrategy uniqueWorkspacePVCStrategy;

  @BeforeMethod
  public void setup() throws Exception {
    uniqueWorkspacePVCStrategy =
        new UniqueWorkspacePVCStrategy(
            PROJECT_NAME,
            PVC_NAME,
            PVC_QUANTITY,
            PVC_ACCESS_MODE,
            PROJECT_FOLDER_PATH,
            clientFactory,
            factory);
    when(clientFactory.create()).thenReturn(client);
    final InternalMachineConfig machine = mock(InternalMachineConfig.class);
    when(machine.getServers())
        .thenReturn(singletonMap(SERVER_WS_AGENT_HTTP_REFERENCE, mock(ServerConfig.class)));
    when(env.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machine));
    when(factory.create(WORKSPACE_ID)).thenReturn(osProject);
    when(osProject.persistentVolumeClaims()).thenReturn(osPVCs);
  }

  @Test
  public void testReplacePVCWhenItsAlreadyInOsEnvironment() throws Exception {
    final Map<String, PersistentVolumeClaim> claims = new HashMap<>();
    final PersistentVolumeClaim provisioned = mock(PersistentVolumeClaim.class);
    claims.put(PVC_UNIQUE_NAME, provisioned);
    when(osEnv.getPersistentVolumeClaims()).thenReturn(claims);

    uniqueWorkspacePVCStrategy.prepare(env, osEnv, WORKSPACE_ID);

    verify(factory).create(WORKSPACE_ID);
    assertNotEquals(osEnv.getPersistentVolumeClaims().get(PVC_UNIQUE_NAME), provisioned);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwInfrastructureExceptionWhenPVCCreationFailed() throws Exception {
    doThrow(InfrastructureException.class).when(osPVCs).createIfNotExist(any());

    uniqueWorkspacePVCStrategy.prepare(env, osEnv, WORKSPACE_ID);
  }

  @Test
  public void addPVCWithUniqueNameToOsEnv() throws Exception {
    mockName(pod, POD_NAME);
    when(pod.getSpec()).thenReturn(podSpec);
    when(osEnv.getPersistentVolumeClaims()).thenReturn(new HashMap<>());
    when(osEnv.getPods()).thenReturn(singletonMap(POD_NAME, pod));
    when(podSpec.getContainers()).thenReturn(singletonList(container));
    when(podSpec.getVolumes()).thenReturn(new ArrayList<>());
    when(container.getName()).thenReturn(CONTAINER_NAME);
    when(container.getVolumeMounts()).thenReturn(new ArrayList<>());

    uniqueWorkspacePVCStrategy.prepare(env, osEnv, WORKSPACE_ID);

    verify(container).getVolumeMounts();
    verify(podSpec).getVolumes();
    assertFalse(podSpec.getVolumes().isEmpty());
    assertFalse(container.getVolumeMounts().isEmpty());
    assertFalse(osEnv.getPersistentVolumeClaims().isEmpty());
    assertTrue(osEnv.getPersistentVolumeClaims().containsKey(PVC_UNIQUE_NAME));
  }

  @Test
  public void testDoNothingWhenNoMachineWithWsAgentFoundInOsEnvironment() throws Exception {
    final Pod pod = mock(Pod.class);
    final PodSpec podSpec = mock(PodSpec.class);
    final Container container = mock(Container.class);
    mockName(pod, "pod1");
    when(pod.getSpec()).thenReturn(podSpec);
    when(osEnv.getPods()).thenReturn(singletonMap("pod1", pod));
    when(podSpec.getContainers()).thenReturn(singletonList(container));
    when(container.getName()).thenReturn("test");

    uniqueWorkspacePVCStrategy.prepare(env, osEnv, WORKSPACE_ID);

    verify(container, never()).getVolumeMounts();
    verify(podSpec, never()).getVolumes();
  }

  @Test
  public void testRemovesPVCWhenCleanupCalled() throws Exception {
    final MixedOperation mixedOperation = mock(MixedOperation.class);
    final NonNamespaceOperation namespace = mock(NonNamespaceOperation.class);
    final Resource resource = mock(Resource.class);
    doReturn(mixedOperation).when(client).persistentVolumeClaims();
    doReturn(namespace).when(mixedOperation).inNamespace(PROJECT_NAME);
    doReturn(resource).when(namespace).withName(PVC_NAME + '-' + WORKSPACE_ID);
    when(resource.delete()).thenReturn(true);

    uniqueWorkspacePVCStrategy.cleanup(WORKSPACE_ID);

    verify(resource).delete();
  }

  @Test
  public void testDoNothingWhenNoPVCFoundInNamespaceOnCleanup() throws Exception {
    final MixedOperation mixedOperation = mock(MixedOperation.class);
    final NonNamespaceOperation namespace = mock(NonNamespaceOperation.class);
    final Resource resource = mock(Resource.class);
    doReturn(mixedOperation).when(client).persistentVolumeClaims();
    doReturn(namespace).when(mixedOperation).inNamespace(PROJECT_NAME);
    doReturn(resource).when(namespace).withName(PVC_NAME + '-' + WORKSPACE_ID);
    when(resource.delete()).thenReturn(false);

    uniqueWorkspacePVCStrategy.cleanup(WORKSPACE_ID);

    verify(resource).delete();
  }
}
