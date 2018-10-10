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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import io.fabric8.kubernetes.api.model.DoneableNamespace;
import io.fabric8.kubernetes.api.model.DoneableServiceAccount;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceFluent.MetadataNested;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.Watcher.Action;
import io.fabric8.kubernetes.client.dsl.ExtensionsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesNamespace}
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesNamespaceTest {

  public static final String NAMESPACE = "testNamespace";
  public static final String WORKSPACE_ID = "workspace123";

  @Mock private KubernetesDeployments deployments;
  @Mock private KubernetesServices services;
  @Mock private KubernetesIngresses ingresses;
  @Mock private KubernetesPersistentVolumeClaims pvcs;
  @Mock private KubernetesSecrets secrets;
  @Mock private KubernetesConfigsMaps configMaps;
  @Mock private KubernetesClientFactory clientFactory;
  @Mock private KubernetesClient kubernetesClient;
  @Mock private NonNamespaceOperation namespaceOperation;
  @Mock private Resource<ServiceAccount, DoneableServiceAccount> serviceAccountResource;

  // Deployments Mocks
  @Mock private ExtensionsAPIGroupDSL extensions;
  @Mock private MixedOperation deploymentsMixedOperation;
  @Mock private NonNamespaceOperation deploymentsNamespaceOperation;
  @Mock private ScalableResource deploymentResource;
  @Mock private Deployment deployment;
  @Mock private ObjectMeta deploymentMetadata;

  // Pod Mocks
  @Mock private MixedOperation podsMixedOperation;
  @Mock private NonNamespaceOperation podsNamespaceOperation;
  @Mock private PodResource podResource;
  @Mock private Pod pod;
  @Mock private ObjectMeta podMetadata;

  private KubernetesNamespace k8sNamespace;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(clientFactory.create()).thenReturn(kubernetesClient);
    lenient().when(clientFactory.create(anyString())).thenReturn(kubernetesClient);

    lenient().doReturn(namespaceOperation).when(kubernetesClient).namespaces();

    final MixedOperation mixedOperation = mock(MixedOperation.class);
    final NonNamespaceOperation namespaceOperation = mock(NonNamespaceOperation.class);
    lenient().doReturn(mixedOperation).when(kubernetesClient).serviceAccounts();
    lenient().when(mixedOperation.inNamespace(anyString())).thenReturn(namespaceOperation);
    lenient().when(namespaceOperation.withName(anyString())).thenReturn(serviceAccountResource);
    lenient().when(serviceAccountResource.get()).thenReturn(mock(ServiceAccount.class));

    // Model DSL: client.pods().inNamespace(...).withName(...).get().getMetadata().getName();
    doReturn(podsMixedOperation).when(kubernetesClient).pods();
    doReturn(podsNamespaceOperation).when(podsMixedOperation).inNamespace(anyString());
    doReturn(podResource).when(podsNamespaceOperation).withName(anyString());
    doReturn(pod).when(podResource).get();
    lenient().doReturn(podMetadata).when(pod).getMetadata();

    doReturn(extensions).when(kubernetesClient).extensions();
    doReturn(deploymentsMixedOperation).when(extensions).deployments();
    doReturn(deploymentsNamespaceOperation)
        .when(deploymentsMixedOperation)
        .inNamespace(anyString());
    doReturn(deploymentResource).when(deploymentsNamespaceOperation).withName(anyString());
    doReturn(deployment).when(deploymentResource).get();
    lenient().doReturn(deploymentMetadata).when(deployment).getMetadata();

    k8sNamespace =
        new KubernetesNamespace(
            clientFactory,
            WORKSPACE_ID,
            NAMESPACE,
            deployments,
            services,
            pvcs,
            ingresses,
            secrets,
            configMaps);
  }

  @Test
  public void testKubernetesNamespacePreparingWhenNamespaceExists() throws Exception {
    // given
    prepareNamespace(NAMESPACE);
    KubernetesNamespace namespace = new KubernetesNamespace(clientFactory, NAMESPACE, WORKSPACE_ID);

    // when
    namespace.prepare();
  }

  @Test
  public void testKubernetesNamespacePreparingCreationWhenNamespaceDoesNotExist() throws Exception {
    // given
    MetadataNested namespaceMeta = prepareCreateNamespaceRequest();

    Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    KubernetesNamespace namespace = new KubernetesNamespace(clientFactory, NAMESPACE, WORKSPACE_ID);

    // when
    namespace.prepare();

    // then
    verify(namespaceMeta).withName(NAMESPACE);
  }

  @Test
  public void testKubernetesNamespaceCleaningUp() throws Exception {
    // when
    k8sNamespace.cleanUp();

    verify(ingresses).delete();
    verify(services).delete();
    verify(deployments).delete();
    verify(secrets).delete();
    verify(configMaps).delete();
  }

  @Test
  public void testKubernetesNamespaceCleaningUpIfExceptionsOccurs() throws Exception {
    doThrow(new InfrastructureException("err1.")).when(services).delete();
    doThrow(new InfrastructureException("err2.")).when(deployments).delete();

    InfrastructureException error = null;
    // when
    try {
      k8sNamespace.cleanUp();

    } catch (InfrastructureException e) {
      error = e;
    }

    // then
    assertNotNull(error);
    String message = error.getMessage();
    assertEquals(message, "Error(s) occurs while cleaning up the namespace. err1. err2.");
    verify(ingresses).delete();
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testThrowsInfrastructureExceptionWhenFailedToGetNamespaceServiceAccounts()
      throws Exception {
    prepareCreateNamespaceRequest();
    final Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    doThrow(KubernetesClientException.class).when(kubernetesClient).serviceAccounts();

    new KubernetesNamespace(clientFactory, NAMESPACE, WORKSPACE_ID).prepare();
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testThrowsInfrastructureExceptionWhenServiceAccountEventNotPublished()
      throws Exception {
    prepareCreateNamespaceRequest();
    final Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    when(serviceAccountResource.get()).thenReturn(null);

    new KubernetesNamespace(clientFactory, NAMESPACE, WORKSPACE_ID).prepare();
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testThrowsInfrastructureExceptionWhenWatcherClosed() throws Exception {
    prepareCreateNamespaceRequest();
    final Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    when(serviceAccountResource.get()).thenReturn(null);
    doAnswer(
            (Answer<Watch>)
                invocation -> {
                  final Watcher<ServiceAccount> watcher = invocation.getArgument(0);
                  watcher.onClose(mock(KubernetesClientException.class));
                  return mock(Watch.class);
                })
        .when(serviceAccountResource)
        .watch(any());

    new KubernetesNamespace(clientFactory, NAMESPACE, WORKSPACE_ID).prepare();
  }

  @Test
  public void testStopsWaitingServiceAccountEventJustAfterEventReceived() throws Exception {
    prepareCreateNamespaceRequest();
    final Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    when(serviceAccountResource.get()).thenReturn(null);
    doAnswer(
            invocation -> {
              final Watcher<ServiceAccount> watcher = invocation.getArgument(0);
              watcher.eventReceived(Action.ADDED, mock(ServiceAccount.class));
              return mock(Watch.class);
            })
        .when(serviceAccountResource)
        .watch(any());

    new KubernetesNamespace(clientFactory, NAMESPACE, WORKSPACE_ID).prepare();

    verify(serviceAccountResource).get();
    verify(serviceAccountResource).watch(any());
  }

  @Test
  public void testDeleteNonExistingPodBeforeWatch() throws Exception {
    final String POD_NAME = "nonExistingPod";
    doReturn(POD_NAME).when(podMetadata).getName();

    doReturn(Boolean.FALSE).when(podResource).delete();
    Watch watch = mock(Watch.class);
    doReturn(watch).when(podResource).watch(any());

    new KubernetesDeployments("", "", clientFactory).doDeletePod(POD_NAME).get(5, TimeUnit.SECONDS);

    verify(watch).close();
  }

  @Test
  public void testDeletePodThrowingKubernetesClientExceptionShouldCloseWatch() throws Exception {
    final String POD_NAME = "nonExistingPod";
    doReturn(POD_NAME).when(podMetadata).getName();

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
    doReturn(POD_NAME).when(podMetadata).getName();

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

  private MetadataNested prepareCreateNamespaceRequest() {
    DoneableNamespace namespace = mock(DoneableNamespace.class);
    MetadataNested metadataNested = mock(MetadataNested.class);

    doReturn(namespace).when(namespaceOperation).createNew();
    doReturn(metadataNested).when(namespace).withNewMetadata();
    doReturn(metadataNested).when(metadataNested).withName(anyString());
    doReturn(namespace).when(metadataNested).endMetadata();
    return metadataNested;
  }

  private Resource prepareNamespaceResource(String namespaceName) {
    Resource namespaceResource = mock(Resource.class);
    doReturn(namespaceResource).when(namespaceOperation).withName(namespaceName);
    kubernetesClient.namespaces().withName(namespaceName).get();
    return namespaceResource;
  }

  private void prepareNamespace(String namespaceName) {
    Namespace namespace = mock(Namespace.class);
    Resource namespaceResource = prepareNamespaceResource(namespaceName);
    doReturn(namespace).when(namespaceResource).get();
  }
}
