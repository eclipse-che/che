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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import io.fabric8.kubernetes.api.model.DoneableNamespace;
import io.fabric8.kubernetes.api.model.DoneableServiceAccount;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.NamespaceFluent.MetadataNested;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.Watcher.Action;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Map;
import java.util.concurrent.Executor;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
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
  @Mock private Executor executor;
  @Mock private KubernetesClient kubernetesClient;
  @Mock private NonNamespaceOperation namespaceOperation;
  @Mock private Resource<ServiceAccount, DoneableServiceAccount> serviceAccountResource;

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
    MetadataNested namespaceMeta = prepareCreateNamespaceRequest();

    prepareNamespace(NAMESPACE);
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID);

    // when
    namespace.prepare(false, true);

    // then
    verify(namespaceMeta, never()).withName(NAMESPACE);
  }

  @Test
  public void testKubernetesNamespacePreparingCreationWhenNamespaceDoesNotExist() throws Exception {
    // given
    MetadataNested namespaceMeta = prepareCreateNamespaceRequest();

    Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID);

    // when
    namespace.prepare(false, true);

    // then
    verify(namespaceMeta).withName(NAMESPACE);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsExceptionIfNamespaceDoesntExistAndNotAllowedToCreateIt() throws Exception {
    // given
    Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID);

    // when
    namespace.prepare(false, false);

    // then
    // exception is thrown
  }

  @Test
  public void testMarksNamespaceManaged() throws Exception {
    // given
    MetadataNested namespaceMeta = prepareCreateNamespaceRequest();

    Namespace nsResource = prepareNamespace(NAMESPACE);
    ObjectMeta metadata = mock(ObjectMeta.class);
    @SuppressWarnings("unchecked")
    Map<String, String> labels = mock(Map.class);
    when(nsResource.getMetadata()).thenReturn(metadata);
    when(metadata.getLabels()).thenReturn(labels);

    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID);

    // when
    namespace.prepare(true, false);

    // then
    verify(namespaceMeta, never()).withName(NAMESPACE);
    verify(labels).put("che-managed", "true");
  }

  @Test
  public void testDoesntMarkNamespaceManaged() throws Exception {
    // given
    MetadataNested namespaceMeta = prepareCreateNamespaceRequest();

    Namespace nsResource = prepareNamespace(NAMESPACE);
    ObjectMeta metadata = mock(ObjectMeta.class);
    @SuppressWarnings("unchecked")
    Map<String, String> labels = mock(Map.class);
    when(nsResource.getMetadata()).thenReturn(metadata);
    when(metadata.getLabels()).thenReturn(labels);

    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID);

    // when
    namespace.prepare(false, false);

    // then
    verify(namespaceMeta, never()).withName(NAMESPACE);
    verify(labels, never()).put(anyString(), anyString());
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

    new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID).prepare(false, false);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testThrowsInfrastructureExceptionWhenServiceAccountEventNotPublished()
      throws Exception {
    prepareCreateNamespaceRequest();
    final Resource resource = prepareNamespaceResource(NAMESPACE);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    when(serviceAccountResource.get()).thenReturn(null);

    new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID).prepare(false, false);
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

    new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID).prepare(false, false);
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

    new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID).prepare(false, true);

    verify(serviceAccountResource).get();
    verify(serviceAccountResource).watch(any());
  }

  @Test
  public void testDeletesExistingManagedNamespace() throws Exception {
    // given
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID);
    Resource resource = prepareManagedNamespaceResource(NAMESPACE);

    // when
    namespace.deleteIfManaged();

    // then
    verify(resource).delete();
  }

  @Test
  public void testDoesntDeleteExistingNonManagedNamespace() throws Exception {
    // given
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID);
    Resource resource = prepareNamespaceResource(NAMESPACE);

    // when
    namespace.deleteIfManaged();

    // then
    verify(resource, never()).delete();
  }

  @Test
  public void testDoesntFailIfDeletedNamespaceDoesntExist() throws Exception {
    // given
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID);
    Resource resource = prepareNamespaceResource(NAMESPACE);
    when(resource.get()).thenThrow(new KubernetesClientException("err", 404, null));
    when(resource.delete()).thenThrow(new KubernetesClientException("err", 404, null));

    // when
    namespace.deleteIfManaged();

    // then
    verify(resource).delete();
    // and no exception is thrown
  }

  @Test
  public void testDoesntFailIfDeletedNamespaceIsBeingDeleted() throws Exception {
    // given
    KubernetesNamespace namespace =
        new KubernetesNamespace(clientFactory, executor, NAMESPACE, WORKSPACE_ID);
    Resource resource = prepareManagedNamespaceResource(NAMESPACE);
    when(resource.delete()).thenThrow(new KubernetesClientException("err", 409, null));

    // when
    namespace.deleteIfManaged();

    // then
    verify(resource).delete();
    // and no exception is thrown
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
    when(namespaceResource.get())
        .thenReturn(
            new NamespaceBuilder().withNewMetadata().withName(namespaceName).endMetadata().build());
    kubernetesClient.namespaces().withName(namespaceName).get();
    return namespaceResource;
  }

  private Resource prepareManagedNamespaceResource(String namespaceName) {
    Resource namespaceResource = mock(Resource.class);
    doReturn(namespaceResource).when(namespaceOperation).withName(namespaceName);
    when(namespaceResource.get())
        .thenReturn(
            new NamespaceBuilder()
                .withNewMetadata()
                .withName(namespaceName)
                .addToLabels("che-managed", "true")
                .endMetadata()
                .build());
    kubernetesClient.namespaces().withName(namespaceName).get();
    return namespaceResource;
  }

  private Namespace prepareNamespace(String namespaceName) {
    Namespace namespace = mock(Namespace.class);
    Resource namespaceResource = prepareNamespaceResource(namespaceName);
    doReturn(namespace).when(namespaceResource).get();
    return namespace;
  }
}
