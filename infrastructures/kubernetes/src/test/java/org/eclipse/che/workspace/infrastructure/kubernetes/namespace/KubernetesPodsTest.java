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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.POD_STATUS_PHASE_FAILED;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.POD_STATUS_PHASE_RUNNING;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.POD_STATUS_PHASE_SUCCEEDED;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.mockito.*;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class KubernetesPodsTest {

  @Mock KubernetesClientFactory clientFactory;

  @Mock PodResource<Pod, DoneablePod> podResource;

  @Mock KubernetesClient kubernetesClient;

  @Mock Pod pod;

  @Mock PodStatus status;

  @Mock ObjectMeta metadata;

  ArgumentCaptor<Watcher> watcherCaptor;

  Watcher watcher;

  KubernetesPods kubernetesPods;

  @BeforeMethod
  public void setUp() throws Exception {
    final Pod earlyPod = mock(Pod.class);
    final PodStatus earlyPodStatus = mock(PodStatus.class);
    when(earlyPod.getStatus()).thenReturn(earlyPodStatus);
    when(podResource.get()).thenReturn(earlyPod);

    when(clientFactory.create()).thenReturn(kubernetesClient);

    final MixedOperation mixedOperation = mock(MixedOperation.class);
    final NonNamespaceOperation namespaceOperation = mock(NonNamespaceOperation.class);
    doReturn(mixedOperation).when(kubernetesClient).pods();
    when(mixedOperation.withName(anyString())).thenReturn(podResource);
    when(mixedOperation.inNamespace(anyString())).thenReturn(namespaceOperation);
    when(namespaceOperation.withName(anyString())).thenReturn(podResource);

    when(pod.getStatus()).thenReturn(status);
    when(pod.getMetadata()).thenReturn(metadata);
    when(metadata.getName()).thenReturn("podName");
    when(podResource.getLog()).thenReturn("Pod fail log");
    watcherCaptor = ArgumentCaptor.forClass(Watcher.class);

    kubernetesPods = new KubernetesPods("namespace", "workspace123", clientFactory);
  }

  @Test
  public void shouldCompleteFutureForWaitingPidIfStatusIsRunning() {
    // given
    when(status.getPhase()).thenReturn(POD_STATUS_PHASE_RUNNING);
    CompletableFuture future = kubernetesPods.waitAsync("name");

    // when
    verify(podResource).watch(watcherCaptor.capture());
    watcher = watcherCaptor.getValue();
    watcher.eventReceived(Watcher.Action.MODIFIED, pod);

    // then
    assertTrue(future.isDone());
  }

  @Test
  public void shouldCompleteExceptionallyFutureForWaitingPodIfStatusIsSucceeded() throws Exception {
    // given
    when(status.getPhase()).thenReturn(POD_STATUS_PHASE_SUCCEEDED);
    CompletableFuture future = kubernetesPods.waitAsync("name");

    // when
    verify(podResource).watch(watcherCaptor.capture());
    watcher = watcherCaptor.getValue();
    watcher.eventReceived(Watcher.Action.MODIFIED, pod);

    // then
    assertTrue(future.isCompletedExceptionally());

    try {
      future.get();
    } catch (ExecutionException e) {
      assertEquals(
          e.getCause().getMessage(),
          "Pod container has been terminated. Container must be configured to use a non-terminating command.");
    }
  }

  @Test
  public void shouldCompleteExceptionallyFutureForWaitingPodIfStatusIsFailed() throws Exception {
    // given
    when(status.getPhase()).thenReturn(POD_STATUS_PHASE_FAILED);
    when(pod.getStatus().getReason()).thenReturn("Evicted");
    CompletableFuture future = kubernetesPods.waitAsync("name");

    // when
    verify(podResource).watch(watcherCaptor.capture());
    watcher = watcherCaptor.getValue();
    watcher.eventReceived(Watcher.Action.MODIFIED, pod);

    // then
    assertTrue(future.isCompletedExceptionally());

    try {
      future.get();
    } catch (ExecutionException e) {
      assertEquals(e.getCause().getMessage(), "Pod 'podName' failed to start. Reason: Evicted");
    }
  }

  @Test
  public void
      shouldCompleteExceptionallyFutureForWaitingPodIfStatusIsFailedAndReasonIsNotAvailable()
          throws Exception {
    // given
    when(status.getPhase()).thenReturn(POD_STATUS_PHASE_FAILED);
    CompletableFuture future = kubernetesPods.waitAsync("name");

    // when
    verify(podResource).watch(watcherCaptor.capture());
    watcher = watcherCaptor.getValue();
    watcher.eventReceived(Watcher.Action.MODIFIED, pod);

    // then
    assertTrue(future.isCompletedExceptionally());
    try {
      future.get();
    } catch (ExecutionException e) {
      assertEquals(
          e.getCause().getMessage(), "Pod 'podName' failed to start. Pod logs: Pod fail log");
    }
  }

  @Test
  public void
      shouldCompleteExceptionallyFutureForWaitingPodIfStatusIsFailedAndReasonNorLogsAreNotAvailable()
          throws Exception {
    // given
    when(status.getPhase()).thenReturn(POD_STATUS_PHASE_FAILED);
    CompletableFuture future = kubernetesPods.waitAsync("name");

    doThrow(new InfrastructureException("Failure while retrieving pod logs"))
        .when(clientFactory)
        .create();

    // when
    verify(podResource).watch(watcherCaptor.capture());
    watcher = watcherCaptor.getValue();
    watcher.eventReceived(Watcher.Action.MODIFIED, pod);

    // then
    assertTrue(future.isCompletedExceptionally());
    try {
      future.get();
    } catch (ExecutionException e) {
      assertEquals(
          e.getCause().getMessage(),
          "Pod 'podName' failed to start. Error occurred while fetching pod logs.");
    }
  }
}
