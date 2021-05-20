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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PVCSubPathHelper.JOB_MOUNT_PATH;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PVCSubPathHelper.MKDIR_COMMAND_BASE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PVCSubPathHelper.POD_PHASE_FAILED;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PVCSubPathHelper.POD_PHASE_SUCCEEDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.observability.NoopExecutorServiceWrapper;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecurityContextProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
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
  private static final String NAMESPACE = "namespace";
  private static final String PVC_NAME = "che-workspace-claim";
  private static final String jobMemoryLimit = "250Mi";
  private static final String jobImage = "centos:centos7";
  private static final String PROJECTS_PATH = "/projects";
  private static final String M2_PATH = "/.m2";

  @Mock private SecurityContextProvisioner securityContextProvisioner;
  @Mock private KubernetesNamespaceFactory k8sNamespaceFactory;
  @Mock private KubernetesNamespace k8sNamespace;
  @Mock private KubernetesDeployments osDeployments;
  @Mock private Pod pod;
  @Mock private PodStatus podStatus;
  @Mock private RuntimeEventsPublisher eventsPublisher;
  @Mock private RuntimeIdentity identity;

  @Captor private ArgumentCaptor<Pod> podCaptor;

  private PVCSubPathHelper pvcSubPathHelper;

  @BeforeMethod
  public void setup() throws Exception {
    pvcSubPathHelper =
        new PVCSubPathHelper(
            jobMemoryLimit,
            jobImage,
            "IfNotPresent",
            k8sNamespaceFactory,
            securityContextProvisioner,
            new NoopExecutorServiceWrapper(),
            eventsPublisher);
    lenient().when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);
    lenient().when(k8sNamespaceFactory.access(WORKSPACE_ID, NAMESPACE)).thenReturn(k8sNamespace);
    lenient().when(k8sNamespace.deployments()).thenReturn(osDeployments);
    lenient().when(pod.getStatus()).thenReturn(podStatus);
    lenient().when(osDeployments.deploy(nullable(Pod.class))).thenReturn(pod);
    lenient().when(osDeployments.wait(anyString(), anyInt(), any())).thenReturn(pod);
    lenient().doNothing().when(osDeployments).delete(anyString());
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

    pvcSubPathHelper.createDirs(
        identity, WORKSPACE_ID, PVC_NAME, emptyMap(), WORKSPACE_ID + PROJECTS_PATH);

    verify(osDeployments).create(podCaptor.capture());
    final List<String> actual = podCaptor.getValue().getSpec().getContainers().get(0).getCommand();

    for (Container container : podCaptor.getValue().getSpec().getContainers()) {
      assertEquals(container.getImagePullPolicy(), "IfNotPresent");
    }
    final List<String> expected =
        Stream.concat(
                Arrays.stream(MKDIR_COMMAND_BASE),
                Stream.of(JOB_MOUNT_PATH + '/' + WORKSPACE_ID + PROJECTS_PATH))
            .collect(toList());
    assertEquals(actual, expected);
    verify(osDeployments).wait(anyString(), anyInt(), any());
    verify(podStatus).getPhase();
    verify(osDeployments).delete(anyString());
    verify(securityContextProvisioner).provision(any());
  }

  @Test
  public void testWatchLogsWhenCreatingWorkspaceDirs() throws InfrastructureException {
    when(podStatus.getPhase()).thenReturn(POD_PHASE_SUCCEEDED);

    pvcSubPathHelper.createDirs(
        identity,
        WORKSPACE_ID,
        PVC_NAME,
        ImmutableMap.of(
            DEBUG_WORKSPACE_START, TRUE.toString(), DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES, "123"),
        WORKSPACE_ID + PROJECTS_PATH);

    verify(osDeployments).watchLogs(any(), any(), any(), any(), eq(123L));
  }

  @Test
  public void testSetMemoryLimitAndRequest() throws Exception {
    when(podStatus.getPhase()).thenReturn(POD_PHASE_SUCCEEDED);

    pvcSubPathHelper.createDirs(
        identity, WORKSPACE_ID, PVC_NAME, emptyMap(), WORKSPACE_ID + PROJECTS_PATH);

    verify(osDeployments).create(podCaptor.capture());
    ResourceRequirements actual =
        podCaptor.getValue().getSpec().getContainers().get(0).getResources();
    ResourceRequirements expected =
        new ResourceRequirementsBuilder()
            .addToLimits(of("memory", new Quantity(jobMemoryLimit)))
            .addToRequests(of("memory", new Quantity(jobMemoryLimit)))
            .build();
    assertEquals(actual, expected);
    verify(osDeployments).wait(anyString(), anyInt(), any());
    verify(podStatus).getPhase();
    verify(osDeployments).delete(anyString());
    verify(securityContextProvisioner).provision(any());
  }

  @Test
  public void testLogErrorWhenJobExecutionFailed() throws Exception {
    when(podStatus.getPhase()).thenReturn(POD_PHASE_FAILED);

    pvcSubPathHelper.execute(
        WORKSPACE_ID, NAMESPACE, PVC_NAME, MKDIR_COMMAND_BASE, WORKSPACE_ID + PROJECTS_PATH);

    verify(osDeployments).create(any());
    verify(osDeployments).wait(anyString(), anyInt(), any());
    verify(podStatus).getPhase();
    verify(osDeployments).getPodLogs(any());
    verify(osDeployments).delete(anyString());
  }

  @Test
  public void testLogErrorWhenKubernetesProjectCreationFailed() throws Exception {
    when(osDeployments.create(any()))
        .thenThrow(new InfrastructureException("Kubernetes namespace creation failed"));

    pvcSubPathHelper.execute(
        WORKSPACE_ID, NAMESPACE, PVC_NAME, MKDIR_COMMAND_BASE, WORKSPACE_ID + PROJECTS_PATH);

    verify(k8sNamespaceFactory).access(WORKSPACE_ID, NAMESPACE);
    verify(osDeployments).create(any());
    verify(osDeployments, never()).wait(anyString(), anyInt(), any());
  }

  @Test
  public void testLogErrorWhenKubernetesPodCreationFailed() throws Exception {
    when(osDeployments.create(any()))
        .thenThrow(new InfrastructureException("Kubernetes pod creation failed"));

    pvcSubPathHelper.execute(
        WORKSPACE_ID, NAMESPACE, PVC_NAME, MKDIR_COMMAND_BASE, WORKSPACE_ID + PROJECTS_PATH);

    verify(k8sNamespaceFactory).access(WORKSPACE_ID, NAMESPACE);
    verify(k8sNamespace).deployments();
    verify(osDeployments).create(any());
    verify(osDeployments, never()).wait(anyString(), anyInt(), any());
  }

  @Test
  public void testIgnoreExceptionWhenPodJobRemovalFailed() throws Exception {
    when(podStatus.getPhase()).thenReturn(POD_PHASE_SUCCEEDED);
    doThrow(InfrastructureException.class).when(osDeployments).delete(anyString());

    pvcSubPathHelper.execute(
        WORKSPACE_ID, NAMESPACE, PVC_NAME, MKDIR_COMMAND_BASE, WORKSPACE_ID + PROJECTS_PATH);

    verify(osDeployments).create(any());
    verify(osDeployments).wait(anyString(), anyInt(), any());
    verify(podStatus).getPhase();
    verify(osDeployments).delete(anyString());
  }

  @Test
  public void shouldBeAbleToConfigureImagePullPolicy() throws InfrastructureException {
    // given
    pvcSubPathHelper =
        new PVCSubPathHelper(
            jobMemoryLimit,
            jobImage,
            "ToBeOrNotIfPresent",
            k8sNamespaceFactory,
            securityContextProvisioner,
            new NoopExecutorServiceWrapper(),
            eventsPublisher);
    // when
    pvcSubPathHelper.execute(
        WORKSPACE_ID, NAMESPACE, PVC_NAME, MKDIR_COMMAND_BASE, WORKSPACE_ID + PROJECTS_PATH);

    // then
    verify(osDeployments).create(podCaptor.capture());
    for (Container container : podCaptor.getValue().getSpec().getContainers()) {
      assertEquals(container.getImagePullPolicy(), "ToBeOrNotIfPresent");
    }
  }
}
