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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static com.google.common.collect.ImmutableMap.of;
import static io.fabric8.kubernetes.api.model.DeletionPropagation.BACKGROUND;
import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static org.eclipse.che.api.workspace.shared.Constants.ASYNC_PERSIST_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner.ASYNC_STORAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.EditReplacePatchDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import java.util.UUID;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class AsyncStoragePodInterceptorTest {

  private static final String WORKSPACE_ID = UUID.randomUUID().toString();
  private static final String NAMESPACE = UUID.randomUUID().toString();

  @Mock private KubernetesEnvironment kubernetesEnvironment;
  @Mock private RuntimeIdentity identity;
  @Mock private KubernetesClientFactory clientFactory;
  @Mock private KubernetesClient kubernetesClient;
  @Mock private RollableScalableResource<Deployment> deploymentResource;
  @Mock private PodResource<Pod> podResource;
  @Mock private MixedOperation mixedOperation;
  @Mock private MixedOperation mixedOperationPod;
  @Mock private NonNamespaceOperation namespaceOperation;
  @Mock private NonNamespaceOperation namespacePodOperation;
  @Mock private EditReplacePatchDeletable<Deployment> deletable;
  @Mock private AppsAPIGroupDSL apps;

  private AsyncStoragePodInterceptor asyncStoragePodInterceptor;

  @BeforeMethod
  public void setUp() {
    asyncStoragePodInterceptor = new AsyncStoragePodInterceptor("common", clientFactory);
  }

  @Test
  public void shouldDoNothingIfNotCommonStrategy() throws Exception {
    when(kubernetesClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);
    when(podResource.get()).thenReturn(null);

    AsyncStoragePodInterceptor asyncStoragePodInterceptor =
        new AsyncStoragePodInterceptor(randomUUID().toString(), clientFactory);
    asyncStoragePodInterceptor.intercept(kubernetesEnvironment, identity);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldDoNothingIfEphemeralWorkspace() throws Exception {
    when(kubernetesEnvironment.getAttributes()).thenReturn(of(PERSIST_VOLUMES_ATTRIBUTE, "false"));
    asyncStoragePodInterceptor.intercept(kubernetesEnvironment, identity);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldDoNothingIfWorkspaceConfiguredWithAsyncStorage() throws Exception {
    when(kubernetesEnvironment.getAttributes())
        .thenReturn(of(PERSIST_VOLUMES_ATTRIBUTE, "false", ASYNC_PERSIST_ATTRIBUTE, "true"));
    asyncStoragePodInterceptor.intercept(kubernetesEnvironment, identity);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldDoNothingIfPodDoesNotExist() throws InfrastructureException {
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);

    when(clientFactory.create(WORKSPACE_ID)).thenReturn(kubernetesClient);
    when(kubernetesEnvironment.getAttributes()).thenReturn(emptyMap());

    when(kubernetesClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);
    when(podResource.get()).thenReturn(null);

    when(kubernetesClient.apps()).thenReturn(apps);
    when(apps.deployments()).thenReturn(mixedOperation);
    when(mixedOperation.inNamespace(NAMESPACE)).thenReturn(namespaceOperation);
    when(namespaceOperation.withName(ASYNC_STORAGE)).thenReturn(deploymentResource);
    when(deploymentResource.get()).thenReturn(null);

    asyncStoragePodInterceptor.intercept(kubernetesEnvironment, identity);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
    verifyNoMoreInteractions(kubernetesClient);
  }

  @Test
  public void shouldDoDeletePodIfWorkspaceWithEmptyAttributes() throws InfrastructureException {
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);

    when(clientFactory.create(WORKSPACE_ID)).thenReturn(kubernetesClient);
    when(kubernetesEnvironment.getAttributes()).thenReturn(emptyMap());

    when(kubernetesClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);
    when(podResource.get()).thenReturn(null);

    when(kubernetesClient.apps()).thenReturn(apps);
    when(apps.deployments()).thenReturn(mixedOperation);
    when(mixedOperation.inNamespace(NAMESPACE)).thenReturn(namespaceOperation);
    when(namespaceOperation.withName(ASYNC_STORAGE)).thenReturn(deploymentResource);

    ObjectMeta meta = new ObjectMeta();
    meta.setName(ASYNC_STORAGE);
    Deployment deployment = new Deployment();
    deployment.setMetadata(meta);

    when(deploymentResource.get()).thenReturn(deployment);
    when(deploymentResource.withPropagationPolicy(BACKGROUND)).thenReturn(deletable);

    Watch watch = mock(Watch.class);
    when(deploymentResource.watch(any())).thenReturn(watch);

    asyncStoragePodInterceptor.intercept(kubernetesEnvironment, identity);
    verify(deletable).delete();
    verify(watch).close();
  }

  @Test
  public void shouldDoDeletePodIfWorkspaceConfigureToPersistentStorage()
      throws InfrastructureException {
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);

    when(clientFactory.create(WORKSPACE_ID)).thenReturn(kubernetesClient);
    when(kubernetesEnvironment.getAttributes())
        .thenReturn(ImmutableMap.of(PERSIST_VOLUMES_ATTRIBUTE, "true"));

    when(kubernetesClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);
    when(podResource.get()).thenReturn(null);

    when(kubernetesClient.apps()).thenReturn(apps);
    when(apps.deployments()).thenReturn(mixedOperation);
    when(mixedOperation.inNamespace(NAMESPACE)).thenReturn(namespaceOperation);
    when(namespaceOperation.withName(ASYNC_STORAGE)).thenReturn(deploymentResource);

    ObjectMeta meta = new ObjectMeta();
    meta.setName(ASYNC_STORAGE);
    Deployment deployment = new Deployment();
    deployment.setMetadata(meta);

    when(deploymentResource.get()).thenReturn(deployment);
    when(deploymentResource.withPropagationPolicy(BACKGROUND)).thenReturn(deletable);

    Watch watch = mock(Watch.class);
    when(deploymentResource.watch(any())).thenReturn(watch);

    asyncStoragePodInterceptor.intercept(kubernetesEnvironment, identity);
    verify(deletable).delete();
    verify(watch).close();
  }
}
