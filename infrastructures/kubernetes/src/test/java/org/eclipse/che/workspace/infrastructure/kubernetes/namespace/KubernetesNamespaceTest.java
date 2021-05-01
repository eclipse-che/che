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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.Watcher.Action;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
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
  @Mock private CheServerKubernetesClientFactory cheClientFactory;
  @Mock private Executor executor;
  @Mock private KubernetesClient kubernetesClient;
  @Mock private NonNamespaceOperation namespaceOperation;
  @Mock private Resource<ServiceAccount> serviceAccountResource;

  private KubernetesNamespace k8sNamespace;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(clientFactory.create(anyString())).thenReturn(kubernetesClient);

    lenient().doReturn(namespaceOperation).when(kubernetesClient).namespaces();

    final MixedOperation mixedOperation = mock(MixedOperation.class);
    final NonNamespaceOperation namespaceOperation = mock(NonNamespaceOperation.class);
    lenient().doReturn(mixedOperation).when(kubernetesClient).serviceAccounts();
    lenient().when(mixedOperation.inNamespace(anyString())).thenReturn(namespaceOperation);
    lenient().when(namespaceOperation.withName(anyString())).thenReturn(serviceAccountResource);
    lenient().when(serviceAccountResource.get()).thenReturn(mock(ServiceAccount.class));

    k8sNamespace =
        new KubernetesNamespace(
            clientFactory,
            cheClientFactory,
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
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);

    // when
    namespace.prepare(true, Map.of());

    // then
    verify(namespaceOperation, never()).create(any(Namespace.class));
  }

  @Test
  public void testKubernetesNamespacePreparingCreationWhenNamespaceDoesNotExist() throws Exception {
    // given

    Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);

    // when
    namespace.prepare(true, Map.of());

    // then
    ArgumentCaptor<Namespace> captor = ArgumentCaptor.forClass(Namespace.class);
    verify(namespaceOperation).create(captor.capture());
    Assert.assertEquals(captor.getValue().getMetadata().getName(), NAMESPACE);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsExceptionIfNamespaceDoesntExistAndNotAllowedToCreateIt() throws Exception {
    // given
    Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);

    // when
    namespace.prepare(false, Map.of());

    // then
    // exception is thrown
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
    // prepareCreateNamespaceRequest();
    final Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    doThrow(KubernetesClientException.class).when(kubernetesClient).serviceAccounts();

    new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID)
        .prepare(false, Map.of());
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testThrowsInfrastructureExceptionWhenServiceAccountEventNotPublished()
      throws Exception {
    // prepareCreateNamespaceRequest();
    final Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    when(serviceAccountResource.get()).thenReturn(null);

    new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID)
        .prepare(false, Map.of());
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testThrowsInfrastructureExceptionWhenWatcherClosed() throws Exception {
    // prepareCreateNamespaceRequest();
    final Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    when(serviceAccountResource.get()).thenReturn(null);
    doAnswer(
            (Answer<Watch>)
                invocation -> {
                  final Watcher<ServiceAccount> watcher = invocation.getArgument(0);
                  watcher.onClose(mock(WatcherException.class));
                  return mock(Watch.class);
                })
        .when(serviceAccountResource)
        .watch(any());

    new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID)
        .prepare(false, Map.of());
  }

  @Test
  public void testStopsWaitingServiceAccountEventJustAfterEventReceived() throws Exception {

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

    new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID)
        .prepare(true, Map.of());

    verify(serviceAccountResource).get();
    verify(serviceAccountResource).watch(any());
  }

  @Test
  public void testDeletesExistingNamespace() throws Exception {
    // given
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);
    Resource resource = prepareNamespaceResource(NAMESPACE);

    // when
    namespace.delete();

    // then
    verify(resource).delete();
  }

  @Test
  public void testDoesntFailIfDeletedNamespaceDoesntExist() throws Exception {
    // given
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);
    Resource resource = prepareNamespaceResource(NAMESPACE);
    when(resource.get()).thenThrow(new KubernetesClientException("err", 404, null));
    when(resource.delete()).thenThrow(new KubernetesClientException("err", 404, null));

    // when
    namespace.delete();

    // then
    verify(resource).delete();
    // and no exception is thrown
  }

  @Test
  public void testDoesntFailIfDeletedNamespaceIsBeingDeleted() throws Exception {
    // given
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);
    Resource resource = prepareNamespaceResource(NAMESPACE);
    when(resource.delete()).thenThrow(new KubernetesClientException("err", 409, null));

    // when
    namespace.delete();

    // then
    verify(resource).delete();
    // and no exception is thrown
  }

  @Test
  public void testLabelNamespace() throws InfrastructureException {
    // given
    prepareNamespace(NAMESPACE);
    KubernetesNamespace kubernetesNamespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);

    KubernetesClient cheKubeClient = mock(KubernetesClient.class);
    doReturn(cheKubeClient).when(cheClientFactory).create();

    NonNamespaceOperation nonNamespaceOperation = mock(NonNamespaceOperation.class);
    doReturn(nonNamespaceOperation).when(cheKubeClient).namespaces();

    ArgumentCaptor<Namespace> namespaceArgumentCaptor = ArgumentCaptor.forClass(Namespace.class);
    doAnswer(a -> a.getArgument(0))
        .when(nonNamespaceOperation)
        .createOrReplace(namespaceArgumentCaptor.capture());

    Map<String, String> labels = Map.of("label.with.this", "yes", "and.this", "of courese");

    // when
    kubernetesNamespace.prepare(true, labels);

    // then
    Namespace updatedNamespace = namespaceArgumentCaptor.getValue();
    assertTrue(
        updatedNamespace.getMetadata().getLabels().entrySet().containsAll(labels.entrySet()));
    assertEquals(updatedNamespace.getMetadata().getName(), NAMESPACE);
  }

  @Test
  public void testDontTryToLabelNamespaceIfAlreadyLabeled() throws InfrastructureException {
    // given
    Map<String, String> labels = Map.of("label.with.this", "yes", "and.this", "of courese");

    Namespace namespace = prepareNamespace(NAMESPACE);
    namespace.getMetadata().setLabels(labels);
    KubernetesNamespace kubernetesNamespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);

    KubernetesClient cheKubeClient = mock(KubernetesClient.class);
    lenient().doReturn(cheKubeClient).when(cheClientFactory).create();

    NonNamespaceOperation nonNamespaceOperation = mock(NonNamespaceOperation.class);
    lenient().doReturn(nonNamespaceOperation).when(cheKubeClient).namespaces();

    lenient()
        .doAnswer(a -> a.getArgument(0))
        .when(nonNamespaceOperation)
        .createOrReplace(any(Namespace.class));

    // when
    kubernetesNamespace.prepare(true, labels);

    // then
    assertTrue(namespace.getMetadata().getLabels().entrySet().containsAll(labels.entrySet()));
    verify(nonNamespaceOperation, never()).createOrReplace(any());
  }

  @Test
  public void testDontTryToLabelNamespaceIfNoNamespacesProvided() throws InfrastructureException {
    // given
    Map<String, String> existingLabels = Map.of("some", "labels");
    Namespace namespace = prepareNamespace(NAMESPACE);
    namespace.getMetadata().setLabels(existingLabels);
    KubernetesNamespace kubernetesNamespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);

    KubernetesClient cheKubeClient = mock(KubernetesClient.class);
    lenient().doReturn(cheKubeClient).when(cheClientFactory).create();

    NonNamespaceOperation nonNamespaceOperation = mock(NonNamespaceOperation.class);
    lenient().doReturn(nonNamespaceOperation).when(cheKubeClient).namespaces();

    lenient()
        .doAnswer(a -> a.getArgument(0))
        .when(nonNamespaceOperation)
        .createOrReplace(any(Namespace.class));

    // when
    kubernetesNamespace.prepare(true, Collections.emptyMap());

    // then
    assertTrue(
        namespace.getMetadata().getLabels().entrySet().containsAll(existingLabels.entrySet()));
    verify(nonNamespaceOperation, never()).createOrReplace(any());
  }

  @Test
  public void testDoNotFailWhenNoPermissionsToUpdateNamespace() throws InfrastructureException {
    // given
    Map<String, String> labels = Map.of("label.with.this", "yes", "and.this", "of courese");

    prepareNamespace(NAMESPACE);
    KubernetesNamespace kubernetesNamespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);

    KubernetesClient cheKubeClient = mock(KubernetesClient.class);
    lenient().doReturn(cheKubeClient).when(cheClientFactory).create();

    NonNamespaceOperation nonNamespaceOperation = mock(NonNamespaceOperation.class);
    lenient().doReturn(nonNamespaceOperation).when(cheKubeClient).namespaces();

    ArgumentCaptor<Namespace> namespaceArgumentCaptor = ArgumentCaptor.forClass(Namespace.class);
    lenient()
        .doThrow(new KubernetesClientException("No permissions.", 403, new Status()))
        .when(nonNamespaceOperation)
        .createOrReplace(namespaceArgumentCaptor.capture());

    // when
    kubernetesNamespace.prepare(true, labels);

    // then
    Namespace updatedNamespace = namespaceArgumentCaptor.getValue();
    assertTrue(
        updatedNamespace.getMetadata().getLabels().entrySet().containsAll(labels.entrySet()));
    assertEquals(updatedNamespace.getMetadata().getName(), NAMESPACE);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testFailWhenFailToUpdateNamespace() throws InfrastructureException {
    // given
    Map<String, String> labels = Map.of("label.with.this", "yes", "and.this", "of courese");

    Namespace namespace = prepareNamespace(NAMESPACE);
    KubernetesNamespace kubernetesNamespace =
        new KubernetesNamespace(clientFactory, cheClientFactory, executor, NAMESPACE, WORKSPACE_ID);

    KubernetesClient cheKubeClient = mock(KubernetesClient.class);
    lenient().doReturn(cheKubeClient).when(cheClientFactory).create();

    NonNamespaceOperation nonNamespaceOperation = mock(NonNamespaceOperation.class);
    lenient().doReturn(nonNamespaceOperation).when(cheKubeClient).namespaces();

    lenient()
        .doThrow(new KubernetesClientException("Some other error", 500, new Status()))
        .when(nonNamespaceOperation)
        .createOrReplace(any(Namespace.class));

    // when
    kubernetesNamespace.prepare(true, labels);

    // then
    verify(nonNamespaceOperation).createOrReplace(namespace);
  }

  private Resource prepareNamespaceResource(String namespaceName) {
    Resource namespaceResource = mock(Resource.class);
    doReturn(namespaceResource).when(namespaceOperation).withName(namespaceName);
    lenient()
        .doReturn(namespaceResource)
        .when(namespaceResource)
        .withPropagationPolicy(eq(DeletionPropagation.BACKGROUND));
    when(namespaceResource.get())
        .thenReturn(
            new NamespaceBuilder().withNewMetadata().withName(namespaceName).endMetadata().build());
    kubernetesClient.namespaces().withName(namespaceName).get();
    return namespaceResource;
  }

  private Namespace prepareNamespace(String namespaceName) {
    Namespace namespace =
        new NamespaceBuilder().withNewMetadata().withName(namespaceName).endMetadata().build();
    Resource namespaceResource = prepareNamespaceResource(namespaceName);
    doReturn(namespace).when(namespaceResource).get();
    return namespace;
  }
}
