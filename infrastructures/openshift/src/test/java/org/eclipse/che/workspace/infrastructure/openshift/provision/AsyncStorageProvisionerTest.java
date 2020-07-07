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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.UUID.randomUUID;
import static org.eclipse.che.api.workspace.shared.Constants.ASYNC_PERSIST_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.AsyncStorageProvisioner.ASYNC_STORAGE;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.AsyncStorageProvisioner.ASYNC_STORAGE_CONFIG;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.AsyncStorageProvisioner.SSH_KEY_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class AsyncStorageProvisionerTest {

  @Mock private OpenShiftEnvironment openShiftEnvironment;
  @Mock private RuntimeIdentity identity;
  @Mock private OpenShiftClientFactory clientFactory;
  @Mock private OpenShiftClient osClient;
  @Mock private SshManager sshManager;
  @Mock private PersistentVolumeClaimList pvcs;
  @Mock private ConfigMapList configMaps;
  @Mock private PodList pods;
  @Mock private ServiceList services;
  @Mock private MixedOperation mixedOperationPvc;
  @Mock private MixedOperation mixedOperationConfigMap;
  @Mock private NonNamespaceOperation namespacePvcOperation;
  @Mock private NonNamespaceOperation namespaceConfigMapOperation;
  @Mock private MixedOperation mixedOperationPod;
  @Mock private MixedOperation mixedOperationService;
  @Mock private NonNamespaceOperation namespacePodOperation;
  @Mock private NonNamespaceOperation namespaceServiceOperation;

  private Map<String, String> attributes;
  private AsyncStorageProvisioner asyncStorageProvisioner;
  private SshPairImpl sshPair;

  @BeforeMethod
  public void setUp() {
    asyncStorageProvisioner =
        new AsyncStorageProvisioner(
            "10Gi",
            "org/image:tag",
            "ReadWriteOnce",
            "common",
            "name",
            "storage",
            sshManager,
            clientFactory);
    attributes = new HashMap<>(2);
    attributes.put(ASYNC_PERSIST_ATTRIBUTE, "true");
    attributes.put(PERSIST_VOLUMES_ATTRIBUTE, "false");
    sshPair = new SshPairImpl("user", "internal", SSH_KEY_NAME, "", "");
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldThrowExceptionIfNotCommonStrategy() throws Exception {
    AsyncStorageProvisioner asyncStorageProvisioner =
        new AsyncStorageProvisioner(
            "10Gi",
            "org/image:tag",
            "ReadWriteOnce",
            randomUUID().toString(),
            "pvcName",
            "storageClass",
            sshManager,
            clientFactory);
    when(openShiftEnvironment.getAttributes()).thenReturn(attributes);
    asyncStorageProvisioner.provision(openShiftEnvironment, identity);
    verifyNoMoreInteractions(sshManager);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldThrowExceptionIfAsyncStorageForNotEphemeralWorkspace() throws Exception {
    Map attributes = new HashMap<>(2);
    attributes.put(ASYNC_PERSIST_ATTRIBUTE, "true");
    attributes.put(PERSIST_VOLUMES_ATTRIBUTE, "true");
    when(openShiftEnvironment.getAttributes()).thenReturn(attributes);
    asyncStorageProvisioner.provision(openShiftEnvironment, identity);
    verifyNoMoreInteractions(sshManager);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldDoNothingIfNotSetAttribute() throws InfrastructureException {
    when(openShiftEnvironment.getAttributes()).thenReturn(emptyMap());
    asyncStorageProvisioner.provision(openShiftEnvironment, identity);
    verifyNoMoreInteractions(sshManager);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldDoNothingIfAttributesAsyncPersistOnly() throws InfrastructureException {
    when(openShiftEnvironment.getAttributes())
        .thenReturn(singletonMap(PERSIST_VOLUMES_ATTRIBUTE, "false"));
    asyncStorageProvisioner.provision(openShiftEnvironment, identity);
    verifyNoMoreInteractions(sshManager);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldCreateAll() throws InfrastructureException, ServerException, ConflictException {
    when(openShiftEnvironment.getAttributes()).thenReturn(attributes);
    when(clientFactory.create(anyString())).thenReturn(osClient);
    when(identity.getWorkspaceId()).thenReturn("wsid");
    when(identity.getInfrastructureNamespace()).thenReturn("wsid");
    when(identity.getOwnerId()).thenReturn("user");
    when(sshManager.getPairs("user", "internal")).thenReturn(singletonList(sshPair));

    when(osClient.persistentVolumeClaims()).thenReturn(mixedOperationPvc);
    when(mixedOperationPvc.inNamespace(anyString())).thenReturn(namespacePvcOperation);
    when(namespacePvcOperation.list()).thenReturn(pvcs);
    when(pvcs.getItems()).thenReturn(emptyList());

    when(osClient.configMaps()).thenReturn(mixedOperationConfigMap);
    when(mixedOperationConfigMap.inNamespace(anyString())).thenReturn(namespaceConfigMapOperation);
    when(namespaceConfigMapOperation.list()).thenReturn(configMaps);
    when(configMaps.getItems()).thenReturn(emptyList());

    when(osClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(anyString())).thenReturn(namespacePodOperation);
    when(namespacePodOperation.list()).thenReturn(pods);
    when(pods.getItems()).thenReturn(emptyList());

    when(osClient.services()).thenReturn(mixedOperationService);
    when(mixedOperationService.inNamespace(anyString())).thenReturn(namespaceServiceOperation);
    when(namespaceServiceOperation.list()).thenReturn(services);
    when(services.getItems()).thenReturn(emptyList());

    asyncStorageProvisioner.provision(openShiftEnvironment, identity);
    verify(identity, times(1)).getInfrastructureNamespace();
    verify(identity, times(1)).getOwnerId();
    verify(sshManager, times(1)).getPairs("user", "internal");
    verify(sshManager, never()).generatePair("user", "internal", SSH_KEY_NAME);
    verify(osClient.services().inNamespace(anyString()), times(1)).create(any(Service.class));
    verify(osClient.configMaps().inNamespace(anyString()), times(1)).create(any(ConfigMap.class));
    verify(osClient.pods().inNamespace(anyString()), times(1)).create(any(Pod.class));
    verify(osClient.persistentVolumeClaims().inNamespace(anyString()), times(1))
        .create(any(PersistentVolumeClaim.class));
  }

  @Test
  public void shouldNotCreateConfigMap()
      throws InfrastructureException, ServerException, ConflictException {
    when(openShiftEnvironment.getAttributes()).thenReturn(attributes);
    when(clientFactory.create(anyString())).thenReturn(osClient);
    when(identity.getWorkspaceId()).thenReturn("wsid");
    when(identity.getInfrastructureNamespace()).thenReturn("wsid");
    when(osClient.persistentVolumeClaims()).thenReturn(mixedOperationPvc);
    when(mixedOperationPvc.inNamespace(anyString())).thenReturn(namespacePvcOperation);
    when(namespacePvcOperation.list()).thenReturn(pvcs);
    when(pvcs.getItems()).thenReturn(emptyList());

    when(osClient.configMaps()).thenReturn(mixedOperationConfigMap);
    when(mixedOperationConfigMap.inNamespace(anyString())).thenReturn(namespaceConfigMapOperation);
    when(namespaceConfigMapOperation.list()).thenReturn(configMaps);
    ObjectMeta meta = new ObjectMeta();
    meta.setName("wsid" + ASYNC_STORAGE_CONFIG);
    ConfigMap configMap = new ConfigMap();
    configMap.setMetadata(meta);
    when(configMaps.getItems()).thenReturn(singletonList(configMap));

    when(osClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(anyString())).thenReturn(namespacePodOperation);
    when(namespacePodOperation.list()).thenReturn(pods);
    when(pods.getItems()).thenReturn(emptyList());

    when(osClient.services()).thenReturn(mixedOperationService);
    when(mixedOperationService.inNamespace(anyString())).thenReturn(namespaceServiceOperation);
    when(namespaceServiceOperation.list()).thenReturn(services);
    when(services.getItems()).thenReturn(emptyList());

    asyncStorageProvisioner.provision(openShiftEnvironment, identity);
    verify(identity, times(1)).getInfrastructureNamespace();
    verify(identity, never()).getOwnerId();
    verify(identity, times(1)).getWorkspaceId();
    verify(sshManager, never()).getPairs("user", "internal");
    verify(sshManager, never()).generatePair("user", "internal", SSH_KEY_NAME);
    verify(osClient.services().inNamespace(anyString()), times(1)).create(any(Service.class));
    verify(osClient.configMaps().inNamespace(anyString()), never()).create(any(ConfigMap.class));
    verify(osClient.pods().inNamespace(anyString()), times(1)).create(any(Pod.class));
    verify(osClient.persistentVolumeClaims().inNamespace(anyString()), times(1))
        .create(any(PersistentVolumeClaim.class));
  }

  @Test
  public void shouldNotCreatePod()
      throws InfrastructureException, ServerException, ConflictException {
    when(openShiftEnvironment.getAttributes()).thenReturn(attributes);
    when(clientFactory.create(anyString())).thenReturn(osClient);
    when(identity.getWorkspaceId()).thenReturn("wsid");
    when(identity.getInfrastructureNamespace()).thenReturn("wsid");
    when(identity.getOwnerId()).thenReturn("user");
    when(sshManager.getPairs("user", "internal")).thenReturn(singletonList(sshPair));

    when(osClient.persistentVolumeClaims()).thenReturn(mixedOperationPvc);
    when(mixedOperationPvc.inNamespace(anyString())).thenReturn(namespacePvcOperation);
    when(namespacePvcOperation.list()).thenReturn(pvcs);
    when(pvcs.getItems()).thenReturn(emptyList());

    when(osClient.configMaps()).thenReturn(mixedOperationConfigMap);
    when(mixedOperationConfigMap.inNamespace(anyString())).thenReturn(namespaceConfigMapOperation);
    when(namespaceConfigMapOperation.list()).thenReturn(configMaps);
    when(configMaps.getItems()).thenReturn(emptyList());

    when(osClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(anyString())).thenReturn(namespacePodOperation);
    when(namespacePodOperation.list()).thenReturn(pods);
    ObjectMeta meta = new ObjectMeta();
    meta.setName(ASYNC_STORAGE);
    Pod pod = new Pod();
    pod.setMetadata(meta);
    when(pods.getItems()).thenReturn(singletonList(pod));

    when(osClient.services()).thenReturn(mixedOperationService);
    when(mixedOperationService.inNamespace(anyString())).thenReturn(namespaceServiceOperation);
    when(namespaceServiceOperation.list()).thenReturn(services);
    when(services.getItems()).thenReturn(emptyList());

    asyncStorageProvisioner.provision(openShiftEnvironment, identity);
    verify(identity, times(1)).getInfrastructureNamespace();
    verify(identity, times(1)).getOwnerId();
    verify(sshManager, times(1)).getPairs("user", "internal");
    verify(sshManager, never()).generatePair("user", "internal", SSH_KEY_NAME);
    verify(osClient.services().inNamespace(anyString()), times(1)).create(any(Service.class));
    verify(osClient.configMaps().inNamespace(anyString()), times(1)).create(any(ConfigMap.class));
    verify(osClient.pods().inNamespace(anyString()), never()).create(any(Pod.class));
    verify(osClient.persistentVolumeClaims().inNamespace(anyString()), times(1))
        .create(any(PersistentVolumeClaim.class));
  }

  @Test
  public void shouldNotCreateService()
      throws InfrastructureException, ServerException, ConflictException {
    when(openShiftEnvironment.getAttributes()).thenReturn(attributes);
    when(clientFactory.create(anyString())).thenReturn(osClient);
    when(identity.getWorkspaceId()).thenReturn("wsid");
    when(identity.getInfrastructureNamespace()).thenReturn("wsid");
    when(identity.getOwnerId()).thenReturn("user");
    when(sshManager.getPairs("user", "internal")).thenReturn(singletonList(sshPair));

    when(osClient.persistentVolumeClaims()).thenReturn(mixedOperationPvc);
    when(mixedOperationPvc.inNamespace(anyString())).thenReturn(namespacePvcOperation);
    when(namespacePvcOperation.list()).thenReturn(pvcs);
    when(pvcs.getItems()).thenReturn(emptyList());

    when(osClient.configMaps()).thenReturn(mixedOperationConfigMap);
    when(mixedOperationConfigMap.inNamespace(anyString())).thenReturn(namespaceConfigMapOperation);
    when(namespaceConfigMapOperation.list()).thenReturn(configMaps);
    when(configMaps.getItems()).thenReturn(emptyList());

    when(osClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(anyString())).thenReturn(namespacePodOperation);
    when(namespacePodOperation.list()).thenReturn(pods);
    when(pods.getItems()).thenReturn(emptyList());

    when(osClient.services()).thenReturn(mixedOperationService);
    when(mixedOperationService.inNamespace(anyString())).thenReturn(namespaceServiceOperation);
    when(namespaceServiceOperation.list()).thenReturn(services);
    ObjectMeta meta = new ObjectMeta();
    meta.setName(ASYNC_STORAGE);
    Service service = new Service();
    service.setMetadata(meta);
    when(services.getItems()).thenReturn(singletonList(service));

    asyncStorageProvisioner.provision(openShiftEnvironment, identity);
    verify(identity, times(1)).getInfrastructureNamespace();
    verify(identity, times(1)).getOwnerId();
    verify(sshManager, times(1)).getPairs("user", "internal");
    verify(sshManager, never()).generatePair("user", "internal", SSH_KEY_NAME);
    verify(osClient.services().inNamespace(anyString()), never()).create(any(Service.class));
    verify(osClient.configMaps().inNamespace(anyString()), times(1)).create(any(ConfigMap.class));
    verify(osClient.pods().inNamespace(anyString()), times(1)).create(any(Pod.class));
    verify(osClient.persistentVolumeClaims().inNamespace(anyString()), times(1))
        .create(any(PersistentVolumeClaim.class));
  }
}
