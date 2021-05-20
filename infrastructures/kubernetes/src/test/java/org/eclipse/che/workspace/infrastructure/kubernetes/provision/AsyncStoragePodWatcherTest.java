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

import static java.time.Instant.now;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner.ASYNC_STORAGE;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {MockitoTestNGListener.class})
public class AsyncStoragePodWatcherTest {

  private final String NAMESPACE = randomUUID().toString();
  private final String WORKSPACE_ID = randomUUID().toString();
  private final String USER_ID = randomUUID().toString();

  private Map<String, String> userPref;

  @Mock private KubernetesClientFactory kubernetesClientFactory;
  @Mock private UserManager userManager;
  @Mock private PreferenceManager preferenceManager;
  @Mock private WorkspaceRuntimes runtimes;
  @Mock private KubernetesClient kubernetesClient;
  @Mock private RollableScalableResource<Deployment> deploymentResource;
  @Mock private MixedOperation mixedOperation;
  @Mock private NonNamespaceOperation namespaceOperation;
  @Mock private PodResource<Pod> podResource;
  @Mock private MixedOperation mixedOperationPod;
  @Mock private NonNamespaceOperation namespacePodOperation;
  @Mock private UserImpl user;
  @Mock private AppsAPIGroupDSL apps;

  @BeforeMethod
  public void setUp() throws Exception {
    when(user.getId()).thenReturn(USER_ID);
    userPref = new HashMap<>(3);
    long epochSecond = now().getEpochSecond();
    long activityTime = epochSecond - 600; // stored time 10 minutes early
    userPref.put(Constants.LAST_ACTIVITY_TIME, Long.toString(activityTime));
    userPref.put(Constants.LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE, NAMESPACE);
    when(preferenceManager.find(USER_ID)).thenReturn(userPref);

    Page<UserImpl> userPage = new Page<>(Collections.singleton(user), 0, 1, 1);
    when(userManager.getAll(anyInt(), anyLong())).thenReturn(userPage);

    when(kubernetesClientFactory.create()).thenReturn(kubernetesClient);
    when(kubernetesClient.apps()).thenReturn(apps);
    when(apps.deployments()).thenReturn(mixedOperation);
    when(mixedOperation.inNamespace(NAMESPACE)).thenReturn(namespaceOperation);
    when(namespaceOperation.withName(ASYNC_STORAGE)).thenReturn(deploymentResource);

    when(kubernetesClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);
    when(podResource.get()).thenReturn(null);
  }

  @Test
  public void shouldDeleteAsyncStorageDeployment() throws Exception {
    AsyncStoragePodWatcher watcher =
        new AsyncStoragePodWatcher(
            kubernetesClientFactory,
            userManager,
            preferenceManager,
            runtimes,
            1,
            COMMON_STRATEGY,
            "<username>",
            1);

    when(runtimes.getInProgress(USER_ID)).thenReturn(emptySet());

    ObjectMeta meta = new ObjectMeta();
    meta.setName(ASYNC_STORAGE);
    Deployment deployment = new Deployment();
    deployment.setMetadata(meta);
    when(deploymentResource.get()).thenReturn(deployment);

    watcher.check();

    verify(preferenceManager).find(USER_ID);
    verify(deploymentResource).delete();
  }

  @Test
  public void shouldNotDeleteAsyncStoragePodIfTooEarly() throws Exception {
    AsyncStoragePodWatcher watcher =
        new AsyncStoragePodWatcher(
            kubernetesClientFactory,
            userManager,
            preferenceManager,
            runtimes,
            10,
            COMMON_STRATEGY,
            "<username>",
            1);
    long epochSecond = now().getEpochSecond();
    userPref.put(Constants.LAST_ACTIVITY_TIME, Long.toString(epochSecond));

    watcher.check();

    verify(preferenceManager).find(USER_ID);
    verifyNoMoreInteractions(kubernetesClientFactory);
    verifyNoMoreInteractions(deploymentResource);
  }

  @Test
  public void shouldNotDeleteAsyncStoragePodIfHasActiveRuntime() throws Exception {
    AsyncStoragePodWatcher watcher =
        new AsyncStoragePodWatcher(
            kubernetesClientFactory,
            userManager,
            preferenceManager,
            runtimes,
            1,
            COMMON_STRATEGY,
            "<username>",
            1);

    // has active runtime
    InternalRuntime runtime = mock(InternalRuntime.class);
    when(runtime.getOwner()).thenReturn(USER_ID);
    when(runtimes.getInProgress(USER_ID)).thenReturn(singleton(WORKSPACE_ID));
    when(runtimes.getInternalRuntime(WORKSPACE_ID)).thenReturn(runtime);

    Page<UserImpl> userPage = new Page<>(Collections.singleton(user), 0, 1, 1);
    when(userManager.getAll(anyInt(), anyLong())).thenReturn(userPage);

    watcher.check();

    verify(preferenceManager).find(USER_ID);
    verifyNoMoreInteractions(kubernetesClientFactory);
    verifyNoMoreInteractions(deploymentResource);
  }

  @Test
  public void shouldNotDeleteAsyncStoragePodIfNoRecord() throws Exception {
    AsyncStoragePodWatcher watcher =
        new AsyncStoragePodWatcher(
            kubernetesClientFactory,
            userManager,
            preferenceManager,
            runtimes,
            1,
            COMMON_STRATEGY,
            "<username>",
            1);
    when(preferenceManager.find(USER_ID)).thenReturn(emptyMap()); // no records in user preferences

    watcher.check();

    verify(preferenceManager).find(USER_ID);
    verifyNoMoreInteractions(kubernetesClientFactory);
    verifyNoMoreInteractions(deploymentResource);
  }

  @Test
  public void shouldDoNothingIfNotCommonPvcStrategy() throws Exception {
    AsyncStoragePodWatcher watcher =
        new AsyncStoragePodWatcher(
            kubernetesClientFactory,
            userManager,
            preferenceManager,
            runtimes,
            1,
            "my-own-strategy",
            "<username>",
            1);
    when(preferenceManager.find(USER_ID)).thenReturn(emptyMap()); // no records in user preferences

    watcher.check();

    verifyNoMoreInteractions(preferenceManager);
    verifyNoMoreInteractions(kubernetesClientFactory);
    verifyNoMoreInteractions(deploymentResource);
  }

  @Test
  public void shouldDoNothingIfAllowedUserDefinedNamespaces() throws Exception {
    AsyncStoragePodWatcher watcher =
        new AsyncStoragePodWatcher(
            kubernetesClientFactory,
            userManager,
            preferenceManager,
            runtimes,
            1,
            "my-own-strategy",
            "<username>",
            1);
    when(preferenceManager.find(USER_ID)).thenReturn(emptyMap()); // no records in user preferences

    watcher.check();

    verifyNoMoreInteractions(preferenceManager);
    verifyNoMoreInteractions(kubernetesClientFactory);
    verifyNoMoreInteractions(deploymentResource);
  }

  @Test
  public void shouldDoNothingIfDefaultNamespaceNotCorrect() throws Exception {
    AsyncStoragePodWatcher watcher =
        new AsyncStoragePodWatcher(
            kubernetesClientFactory,
            userManager,
            preferenceManager,
            runtimes,
            1,
            "my-own-strategy",
            "<foo-bar>",
            1);
    when(preferenceManager.find(USER_ID)).thenReturn(emptyMap()); // no records in user preferences

    watcher.check();

    verifyNoMoreInteractions(preferenceManager);
    verifyNoMoreInteractions(kubernetesClientFactory);
    verifyNoMoreInteractions(deploymentResource);
  }

  @Test
  public void shouldDoNothingIfAllowMoreThanOneRuntime() throws Exception {
    AsyncStoragePodWatcher watcher =
        new AsyncStoragePodWatcher(
            kubernetesClientFactory,
            userManager,
            preferenceManager,
            runtimes,
            1,
            "my-own-strategy",
            "<foo-bar>",
            2);
    when(preferenceManager.find(USER_ID)).thenReturn(emptyMap()); // no records in user preferences

    watcher.check();

    verifyNoMoreInteractions(preferenceManager);
    verifyNoMoreInteractions(kubernetesClientFactory);
    verifyNoMoreInteractions(deploymentResource);
  }

  @Test
  public void shouldDoNothingIfShutdownTimeSetToZero() throws Exception {
    AsyncStoragePodWatcher watcher =
        new AsyncStoragePodWatcher(
            kubernetesClientFactory,
            userManager,
            preferenceManager,
            runtimes,
            0,
            COMMON_STRATEGY,
            "<username>",
            1);
    when(preferenceManager.find(USER_ID)).thenReturn(emptyMap()); // no records in user preferences

    watcher.check();

    verifyNoMoreInteractions(preferenceManager);
    verifyNoMoreInteractions(kubernetesClientFactory);
    verifyNoMoreInteractions(deploymentResource);
  }
}
