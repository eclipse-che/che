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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.POD_STATUS_PHASE_FAILED;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.POD_STATUS_PHASE_RUNNING;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.POD_STATUS_PHASE_SUCCEEDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class KubernetesDeploymentsTest {
  private static final String POD_NAME = "podName";

  @Mock private KubernetesClientFactory clientFactory;
  @Mock private KubernetesClient kubernetesClient;

  // Deployments Mocks
  @Mock private AppsAPIGroupDSL apps;
  @Mock private MixedOperation deploymentsMixedOperation;
  @Mock private NonNamespaceOperation deploymentsNamespaceOperation;
  @Mock private ScalableResource deploymentResource;
  @Mock private Deployment deployment;
  @Mock private ObjectMeta deploymentMetadata;

  // Pod Mocks
  @Mock private Pod pod;
  @Mock private PodStatus status;
  @Mock private PodResource<Pod, DoneablePod> podResource;
  @Mock private ObjectMeta metadata;
  @Mock private MixedOperation podsMixedOperation;
  @Mock private NonNamespaceOperation podsNamespaceOperation;

  @Captor private ArgumentCaptor<Watcher<Pod>> watcherCaptor;

  private KubernetesDeployments kubernetesDeployments;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(clientFactory.create(anyString())).thenReturn(kubernetesClient);

    when(pod.getStatus()).thenReturn(status);
    when(pod.getMetadata()).thenReturn(metadata);
    when(metadata.getName()).thenReturn(POD_NAME);

    // Model DSL: client.pods().inNamespace(...).withName(...).get().getMetadata().getName();
    lenient().doReturn(podsMixedOperation).when(kubernetesClient).pods();
    lenient().doReturn(podsNamespaceOperation).when(podsMixedOperation).inNamespace(anyString());
    lenient().doReturn(podResource).when(podsNamespaceOperation).withName(anyString());
    lenient().doReturn(pod).when(podResource).get();

    // Model DSL:
    // client.apps().deployments(...).inNamespace(...).withName(...).get().getMetadata().getName();
    lenient().doReturn(apps).when(kubernetesClient).apps();
    lenient().doReturn(deploymentsMixedOperation).when(apps).deployments();
    lenient()
        .doReturn(deploymentsNamespaceOperation)
        .when(deploymentsMixedOperation)
        .inNamespace(anyString());
    lenient()
        .doReturn(deploymentResource)
        .when(deploymentsNamespaceOperation)
        .withName(anyString());
    lenient().doReturn(deployment).when(deploymentResource).get();
    lenient().doReturn(deploymentMetadata).when(deployment).getMetadata();

    kubernetesDeployments = new KubernetesDeployments("namespace", "workspace123", clientFactory);
  }

  @Test
  public void shouldCompleteFutureForWaitingPidIfStatusIsRunning() {
    // given
    when(status.getPhase()).thenReturn(POD_STATUS_PHASE_RUNNING);
    CompletableFuture future = kubernetesDeployments.waitRunningAsync(POD_NAME);

    // when
    verify(podResource).watch(watcherCaptor.capture());
    Watcher<Pod> watcher = watcherCaptor.getValue();
    watcher.eventReceived(Watcher.Action.MODIFIED, pod);

    // then
    assertTrue(future.isDone());
  }

  @Test
  public void shouldCompleteExceptionallyFutureForWaitingPodIfStatusIsSucceeded() throws Exception {
    // given
    when(status.getPhase()).thenReturn(POD_STATUS_PHASE_SUCCEEDED);
    CompletableFuture future = kubernetesDeployments.waitRunningAsync(POD_NAME);

    // when
    verify(podResource).watch(watcherCaptor.capture());
    Watcher<Pod> watcher = watcherCaptor.getValue();
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
    CompletableFuture future = kubernetesDeployments.waitRunningAsync(POD_NAME);

    // when
    verify(podResource).watch(watcherCaptor.capture());
    Watcher<Pod> watcher = watcherCaptor.getValue();
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
    when(podResource.getLog()).thenReturn("Pod fail log");
    CompletableFuture future = kubernetesDeployments.waitRunningAsync(POD_NAME);

    // when
    verify(podResource).watch(watcherCaptor.capture());
    Watcher<Pod> watcher = watcherCaptor.getValue();
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
    CompletableFuture future = kubernetesDeployments.waitRunningAsync(POD_NAME);

    when(status.getPhase()).thenReturn(POD_STATUS_PHASE_FAILED);
    doThrow(new InfrastructureException("Unable to create client"))
        .when(clientFactory)
        .create(anyString());

    // when
    verify(podResource).watch(watcherCaptor.capture());
    Watcher<Pod> watcher = watcherCaptor.getValue();
    watcher.eventReceived(Watcher.Action.MODIFIED, pod);

    // then
    assertTrue(future.isCompletedExceptionally());
    try {
      future.get();
    } catch (ExecutionException e) {
      assertEquals(
          e.getCause().getMessage(),
          "Pod 'podName' failed to start. Error occurred while fetching pod logs: Unable to create client");
    }
  }

  @Test
  public void testDeleteNonExistingPodBeforeWatch() throws Exception {
    final String POD_NAME = "nonExistingPod";
    doReturn(POD_NAME).when(metadata).getName();

    doReturn(Boolean.FALSE).when(podResource).delete();
    Watch watch = mock(Watch.class);
    doReturn(watch).when(podResource).watch(any());

    new KubernetesDeployments("", "", clientFactory).doDeletePod(POD_NAME).get(5, TimeUnit.SECONDS);

    verify(watch).close();
  }

  @Test
  public void testDeletePodThrowingKubernetesClientExceptionShouldCloseWatch() throws Exception {
    final String POD_NAME = "nonExistingPod";
    doReturn(POD_NAME).when(metadata).getName();

    doThrow(KubernetesClientException.class).when(podResource).delete();
    Watch watch = mock(Watch.class);
    doReturn(watch).when(podResource).watch(any());

    try {
      new KubernetesDeployments("", "", clientFactory)
          .doDeletePod(POD_NAME)
          .get(5, TimeUnit.SECONDS);
    } catch (KubernetesInfrastructureException e) {
      assertTrue(e.getCause() instanceof KubernetesClientException);
      verify(watch).close();
      return;
    }
    fail("The exception should have been rethrown");
  }

  @Test
  public void testDeleteNonExistingDeploymentBeforeWatch() throws Exception {
    final String DEPLOYMENT_NAME = "nonExistingPod";
    doReturn(DEPLOYMENT_NAME).when(deploymentMetadata).getName();

    doReturn(Boolean.FALSE).when(deploymentResource).delete();
    Watch watch = mock(Watch.class);
    doReturn(watch).when(podResource).watch(any());

    new KubernetesDeployments("", "", clientFactory)
        .doDeleteDeployment(DEPLOYMENT_NAME)
        .get(5, TimeUnit.SECONDS);

    verify(watch).close();
  }

  @Test
  public void testDeleteDeploymentThrowingKubernetesClientExceptionShouldCloseWatch()
      throws Exception {
    final String DEPLOYMENT_NAME = "nonExistingPod";
    doReturn(DEPLOYMENT_NAME).when(deploymentMetadata).getName();

    doThrow(KubernetesClientException.class).when(deploymentResource).delete();
    Watch watch = mock(Watch.class);
    doReturn(watch).when(podResource).watch(any());

    try {
      new KubernetesDeployments("", "", clientFactory)
          .doDeleteDeployment(DEPLOYMENT_NAME)
          .get(5, TimeUnit.SECONDS);
    } catch (KubernetesInfrastructureException e) {
      assertTrue(e.getCause() instanceof KubernetesClientException);
      verify(watch).close();
      return;
    }
    fail("The exception should have been rethrown");
  }

  @Test
  public void testDeletePodThrowingAnyExceptionShouldCloseWatch() throws Exception {
    final String POD_NAME = "nonExistingPod";
    doReturn(POD_NAME).when(metadata).getName();

    doThrow(RuntimeException.class).when(podResource).delete();
    Watch watch = mock(Watch.class);
    doReturn(watch).when(podResource).watch(any());

    try {
      new KubernetesDeployments("", "", clientFactory)
          .doDeletePod(POD_NAME)
          .get(5, TimeUnit.SECONDS);
    } catch (RuntimeException e) {
      verify(watch).close();
      return;
    }
    fail("The exception should have been rethrown");
  }

  @Test
  public void testDeleteDeploymentThrowingAnyExceptionShouldCloseWatch() throws Exception {
    final String DEPLOYMENT_NAME = "nonExistingPod";

    doThrow(RuntimeException.class).when(deploymentResource).delete();
    Watch watch = mock(Watch.class);
    doReturn(watch).when(podResource).watch(any());

    try {
      new KubernetesDeployments("", "", clientFactory)
          .doDeleteDeployment(DEPLOYMENT_NAME)
          .get(5, TimeUnit.SECONDS);
    } catch (RuntimeException e) {
      verify(watch).close();
      return;
    }
    fail("The exception should have been rethrown");
  }
}
