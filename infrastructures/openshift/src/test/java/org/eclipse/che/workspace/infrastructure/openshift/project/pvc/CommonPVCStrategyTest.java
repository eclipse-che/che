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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.workspace.infrastructure.openshift.Names.machineName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
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
 * Tests {@link CommonPVCStrategy}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class CommonPVCStrategyTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final String PVC_NAME = "che-claim";
  private static final String POD_NAME = "main";
  private static final String CONTAINER_NAME = "app";
  private static final String MACHINE_NAME = machineName(POD_NAME, CONTAINER_NAME);
  private static final String PVC_QUANTITY = "10Gi";
  private static final String PVC_ACCESS_MODE = "RWO";
  private static final String PROJECT_FOLDER_PATH = "/projects";

  @Mock private Pod pod;
  @Mock private PodSpec podSpec;
  @Mock private Container container;
  @Mock private InternalEnvironment env;
  @Mock private OpenShiftEnvironment osEnv;
  @Mock private PVCSubPathHelper pvcSubPathHelper;
  @Mock private OpenShiftProjectFactory factory;
  @Mock private OpenShiftProject osProject;
  @Mock private OpenShiftPersistentVolumeClaims osPVCs;

  private CommonPVCStrategy commonPVCStrategy;

  @BeforeMethod
  public void setup() throws Exception {
    commonPVCStrategy =
        new CommonPVCStrategy(
            PVC_NAME,
            PVC_QUANTITY,
            PVC_ACCESS_MODE,
            PROJECT_FOLDER_PATH,
            pvcSubPathHelper,
            factory);
    final InternalMachineConfig machine = mock(InternalMachineConfig.class);
    when(machine.getServers())
        .thenReturn(singletonMap(SERVER_WS_AGENT_HTTP_REFERENCE, mock(ServerConfig.class)));
    when(env.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machine));
    doNothing().when(pvcSubPathHelper).execute(any(), any(), any());
    when(osEnv.getPersistentVolumeClaims()).thenReturn(new HashMap<>());
    when(pvcSubPathHelper.removeDirsAsync(anyString(), any(String.class)))
        .thenReturn(CompletableFuture.completedFuture(null));
    when(factory.create(WORKSPACE_ID)).thenReturn(osProject);
    when(osProject.persistentVolumeClaims()).thenReturn(osPVCs);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenMachineWithWsAgentNotFound() throws Exception {
    when(env.getMachines()).thenReturn(emptyMap());

    commonPVCStrategy.prepare(env, osEnv, WORKSPACE_ID);
  }

  @Test
  public void addPVCWithConfiguredNameToOsEnv() throws Exception {
    when(pod.getSpec()).thenReturn(podSpec);
    mockName(pod, POD_NAME);
    when(osEnv.getPersistentVolumeClaims()).thenReturn(new HashMap<>());
    when(osEnv.getPods()).thenReturn(singletonMap(POD_NAME, pod));
    when(podSpec.getContainers()).thenReturn(singletonList(container));
    when(podSpec.getVolumes()).thenReturn(new ArrayList<>());
    when(container.getName()).thenReturn(CONTAINER_NAME);
    when(container.getVolumeMounts()).thenReturn(new ArrayList<>());

    commonPVCStrategy.prepare(env, osEnv, WORKSPACE_ID);

    verify(container).getVolumeMounts();
    verify(podSpec).getVolumes();
    verify(pvcSubPathHelper).createDirs(WORKSPACE_ID, WORKSPACE_ID + PROJECT_FOLDER_PATH);
    assertFalse(podSpec.getVolumes().isEmpty());
    assertFalse(container.getVolumeMounts().isEmpty());
    assertFalse(osEnv.getPersistentVolumeClaims().isEmpty());
    assertTrue(osEnv.getPersistentVolumeClaims().containsKey(PVC_NAME));
  }

  @Test
  public void testReplacePVCWhenItsAlreadyInOsEnvironment() throws Exception {
    final Map<String, PersistentVolumeClaim> claims = new HashMap<>();
    final PersistentVolumeClaim provisioned = mock(PersistentVolumeClaim.class);
    claims.put(PVC_NAME, provisioned);
    when(osEnv.getPersistentVolumeClaims()).thenReturn(claims);

    commonPVCStrategy.prepare(env, osEnv, WORKSPACE_ID);

    verify(factory).create(WORKSPACE_ID);
    assertNotEquals(osEnv.getPersistentVolumeClaims().get(PVC_NAME), provisioned);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwInfrastructureExceptionWhenOsProjectCreationFailed() throws Exception {
    when(factory.create(any())).thenThrow(new InfrastructureException("Project creation failed"));

    commonPVCStrategy.prepare(env, osEnv, WORKSPACE_ID);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwInfrastructureExceptionWhenPVCCreationFailed() throws Exception {
    doThrow(InfrastructureException.class).when(osPVCs).createIfNotExist(any());

    commonPVCStrategy.prepare(env, osEnv, WORKSPACE_ID);
  }

  @Test
  public void testDoNothingWhenNoMachineWithWsAgentFoundInOsEnvironment() throws Exception {
    final Pod pod = mock(Pod.class);
    final PodSpec podSpec = mock(PodSpec.class);
    final Container container = mock(Container.class);
    mockName(pod, "testPod");
    when(pod.getSpec()).thenReturn(podSpec);
    when(osEnv.getPods()).thenReturn(singletonMap("testPod", pod));
    when(podSpec.getContainers()).thenReturn(singletonList(container));
    when(container.getName()).thenReturn("container");

    commonPVCStrategy.prepare(env, osEnv, WORKSPACE_ID);

    verify(container, never()).getVolumeMounts();
    verify(podSpec, never()).getVolumes();
  }

  @Test
  public void testCleanup() throws Exception {
    commonPVCStrategy.cleanup(WORKSPACE_ID);

    verify(pvcSubPathHelper).removeDirsAsync(WORKSPACE_ID, WORKSPACE_ID);
  }

  static void mockName(HasMetadata obj, String name) {
    final ObjectMeta objectMeta = mock(ObjectMeta.class);
    when(obj.getMetadata()).thenReturn(objectMeta);
    when(objectMeta.getName()).thenReturn(name);
  }
}
