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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static org.eclipse.che.api.workspace.shared.Constants.ASYNC_PERSIST_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.AsyncStorageProvisioner.ASYNC_STORAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.UUID;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class AsyncStoragePodInterceptorTest {

  private static final String WORKSPACE_ID = UUID.randomUUID().toString();
  private static final String NAMESPACE = UUID.randomUUID().toString();

  @Mock private OpenShiftEnvironment openShiftEnvironment;
  @Mock private RuntimeIdentity identity;
  @Mock private OpenShiftClientFactory clientFactory;
  @Mock private OpenShiftClient osClient;
  @Mock private PodResource<Pod, DoneablePod> podResource;
  @Mock private MixedOperation mixedOperationPod;
  @Mock private NonNamespaceOperation namespacePodOperation;
  @Mock private FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> deletable;

  private AsyncStoragePodInterceptor asyncStoragePodInterceptor;

  @BeforeMethod
  public void setUp() {
    asyncStoragePodInterceptor = new AsyncStoragePodInterceptor("common", clientFactory);
  }

  @Test
  public void shouldDoNothingIfNotCommonStrategy() throws Exception {
    AsyncStoragePodInterceptor asyncStoragePodInterceptor =
        new AsyncStoragePodInterceptor(randomUUID().toString(), clientFactory);
    asyncStoragePodInterceptor.intercept(openShiftEnvironment, identity);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldDoNothingIfEphemeralWorkspace() throws Exception {
    when(openShiftEnvironment.getAttributes()).thenReturn(of(PERSIST_VOLUMES_ATTRIBUTE, "false"));
    asyncStoragePodInterceptor.intercept(openShiftEnvironment, identity);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldDoNothingIfWorkspaceConfiguredWithAsyncStorage() throws Exception {
    when(openShiftEnvironment.getAttributes())
        .thenReturn(of(PERSIST_VOLUMES_ATTRIBUTE, "false", ASYNC_PERSIST_ATTRIBUTE, "true"));
    asyncStoragePodInterceptor.intercept(openShiftEnvironment, identity);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldDoNothingIfPodDoesNotExist() throws InfrastructureException {
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);

    when(clientFactory.create(WORKSPACE_ID)).thenReturn(osClient);
    when(openShiftEnvironment.getAttributes()).thenReturn(emptyMap());

    when(osClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);
    when(podResource.get()).thenReturn(null);

    asyncStoragePodInterceptor.intercept(openShiftEnvironment, identity);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
    verifyNoMoreInteractions(osClient);
  }

  @Test
  public void shouldDoDeletePodIfWorkspaceWithEmptyAttributes() throws InfrastructureException {
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);

    when(clientFactory.create(WORKSPACE_ID)).thenReturn(osClient);
    when(openShiftEnvironment.getAttributes()).thenReturn(emptyMap());

    when(osClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);

    ObjectMeta meta = new ObjectMeta();
    meta.setName(ASYNC_STORAGE);
    Pod pod = new Pod();
    pod.setMetadata(meta);

    when(podResource.get()).thenReturn(pod);
    when(podResource.withPropagationPolicy("Background")).thenReturn(deletable);

    Watch watch = mock(Watch.class);
    when(podResource.watch(any())).thenReturn(watch);

    asyncStoragePodInterceptor.intercept(openShiftEnvironment, identity);
    verify(deletable).delete();
    verify(watch).close();
  }

  @Test
  public void shouldDoDeletePodIfWorkspaceConfigureToPersistentStorage()
      throws InfrastructureException {
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);

    when(clientFactory.create(WORKSPACE_ID)).thenReturn(osClient);
    when(openShiftEnvironment.getAttributes())
        .thenReturn(ImmutableMap.of(PERSIST_VOLUMES_ATTRIBUTE, "true"));

    when(osClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);

    ObjectMeta meta = new ObjectMeta();
    meta.setName(ASYNC_STORAGE);
    Pod pod = new Pod();
    pod.setMetadata(meta);

    when(podResource.get()).thenReturn(pod);
    when(podResource.withPropagationPolicy("Background")).thenReturn(deletable);

    Watch watch = mock(Watch.class);
    when(podResource.watch(any())).thenReturn(watch);

    asyncStoragePodInterceptor.intercept(openShiftEnvironment, identity);
    verify(deletable).delete();
    verify(watch).close();
  }
}
