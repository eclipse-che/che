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

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PVCSubPathHelper.JOB_MOUNT_PATH;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PVCSubPathHelper.MKDIR_COMMAND_BASE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PVCSubPathHelper.POD_PHASE_FAILED;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PVCSubPathHelper.POD_PHASE_SUCCEEDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPods;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link PVCSubPathHelper}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class PVCSubPathHelperTest {

  private static final String WORKSPACE_ID = "workspace132";
  private static final String PVC_NAME = "che-workspace-claim";
  private static final String jobMemoryLimit = "250Mi";
  private static final String jobImage = "centos:centos7";
  private static final String PROJECTS_PATH = "/projects";
  private static final String M2_PATH = "/.m2";

  @Mock private KubernetesNamespaceFactory k8sNamespaceFactory;
  @Mock private KubernetesNamespace k8sNamespace;
  @Mock private KubernetesPods osPods;
  @Mock private Pod pod;
  @Mock private PodStatus podStatus;

  @Captor private ArgumentCaptor<Pod> podCaptor;

  private PVCSubPathHelper pvcSubPathHelper;

  @BeforeMethod
  public void setup() throws Exception {
    pvcSubPathHelper =
        new PVCSubPathHelper(PVC_NAME, jobMemoryLimit, jobImage, k8sNamespaceFactory);
    when(k8sNamespaceFactory.create(anyString())).thenReturn(k8sNamespace);
    when(k8sNamespace.pods()).thenReturn(osPods);
    when(pod.getStatus()).thenReturn(podStatus);
    when(osPods.create(any(Pod.class))).thenReturn(pod);
    when(osPods.wait(anyString(), anyInt(), any())).thenReturn(pod);
    doNothing().when(osPods).delete(anyString());
  }

  @Test
  public void testBuildsCommandByGivenBaseAndPaths() throws Exception {
    final String[] paths = {WORKSPACE_ID + PROJECTS_PATH, WORKSPACE_ID + M2_PATH};

    final String[] actual = pvcSubPathHelper.buildCommand(MKDIR_COMMAND_BASE, paths);

    final String[] expected = new String[MKDIR_COMMAND_BASE.length + 2];
    System.arraycopy(MKDIR_COMMAND_BASE, 0, expected, 0, MKDIR_COMMAND_BASE.length);
    expected[expected.length - 1] = JOB_MOUNT_PATH + '/' + WORKSPACE_ID + M2_PATH;
    expected[expected.length - 2] = JOB_MOUNT_PATH + '/' + WORKSPACE_ID + PROJECTS_PATH;
    assertEquals(actual, expected);
  }

  @Test
  public void testSuccessfullyCreatesWorkspaceDirs() throws Exception {
    when(podStatus.getPhase()).thenReturn(POD_PHASE_SUCCEEDED);

    pvcSubPathHelper.createDirs(WORKSPACE_ID, WORKSPACE_ID + PROJECTS_PATH);

    verify(osPods).create(podCaptor.capture());
    final List<String> actual = podCaptor.getValue().getSpec().getContainers().get(0).getCommand();
    final List<String> expected =
        Stream.concat(
                Arrays.stream(MKDIR_COMMAND_BASE),
                Stream.of(JOB_MOUNT_PATH + '/' + WORKSPACE_ID + PROJECTS_PATH))
            .collect(toList());
    assertEquals(actual, expected);
    verify(osPods).wait(anyString(), anyInt(), any());
    verify(podStatus).getPhase();
    verify(osPods).delete(anyString());
  }

  @Test
  public void testLogErrorWhenJobExecutionFailed() throws Exception {
    when(podStatus.getPhase()).thenReturn(POD_PHASE_FAILED);

    pvcSubPathHelper.execute(WORKSPACE_ID, MKDIR_COMMAND_BASE, WORKSPACE_ID + PROJECTS_PATH);

    verify(osPods).create(any());
    verify(osPods).wait(anyString(), anyInt(), any());
    verify(podStatus).getPhase();
    verify(osPods).delete(anyString());
  }

  @Test
  public void testLogErrorWhenKubernetesProjectCreationFailed() throws Exception {
    when(k8sNamespaceFactory.create(WORKSPACE_ID))
        .thenThrow(new InfrastructureException("Kubernetes namespace creation failed"));

    pvcSubPathHelper.execute(WORKSPACE_ID, MKDIR_COMMAND_BASE, WORKSPACE_ID + PROJECTS_PATH);

    verify(k8sNamespaceFactory).create(WORKSPACE_ID);
    verify(k8sNamespace, never()).pods();
  }

  @Test
  public void testLogErrorWhenKubernetesPodCreationFailed() throws Exception {
    when(osPods.create(any()))
        .thenThrow(new InfrastructureException("Kubernetes pod creation failed"));

    pvcSubPathHelper.execute(WORKSPACE_ID, MKDIR_COMMAND_BASE, WORKSPACE_ID + PROJECTS_PATH);

    verify(k8sNamespaceFactory).create(WORKSPACE_ID);
    verify(k8sNamespace).pods();
    verify(osPods).create(any());
    verify(osPods, never()).wait(anyString(), anyInt(), any());
  }

  @Test
  public void testIgnoreExceptionWhenPodJobRemovalFailed() throws Exception {
    when(podStatus.getPhase()).thenReturn(POD_PHASE_SUCCEEDED);
    doThrow(InfrastructureException.class).when(osPods).delete(anyString());

    pvcSubPathHelper.execute(WORKSPACE_ID, MKDIR_COMMAND_BASE, WORKSPACE_ID + PROJECTS_PATH);

    verify(osPods).create(any());
    verify(osPods).wait(anyString(), anyInt(), any());
    verify(podStatus).getPhase();
    verify(osPods).delete(anyString());
  }
}
