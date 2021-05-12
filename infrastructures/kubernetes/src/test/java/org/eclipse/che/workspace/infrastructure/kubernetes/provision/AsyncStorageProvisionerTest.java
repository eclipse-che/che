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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.UUID.randomUUID;
import static org.eclipse.che.api.workspace.shared.Constants.ASYNC_PERSIST_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner.ASYNC_STORAGE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner.ASYNC_STORAGE_CONFIG;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner.SSH_KEY_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class AsyncStorageProvisionerTest {

  private static final String WORKSPACE_ID = UUID.randomUUID().toString();
  private static final String NAMESPACE = UUID.randomUUID().toString();
  private static final String CONFIGMAP_NAME = NAMESPACE + ASYNC_STORAGE_CONFIG;
  private static final String VPC_NAME = UUID.randomUUID().toString();
  private static final String USER = "user";

  @Mock private KubernetesEnvironment kubernetesEnvironment;
  @Mock private RuntimeIdentity identity;
  @Mock private KubernetesClientFactory clientFactory;
  @Mock private KubernetesClient kubernetesClient;
  @Mock private SshManager sshManager;
  @Mock private Resource<PersistentVolumeClaim> pvcResource;
  @Mock private Resource<ConfigMap> mapResource;
  @Mock private PodResource<Pod> podResource;
  @Mock private RollableScalableResource<Deployment> deploymentResource;
  @Mock private ServiceResource<Service> serviceResource;
  @Mock private MixedOperation mixedOperationPvc;
  @Mock private MixedOperation mixedOperationConfigMap;
  @Mock private MixedOperation mixedOperationPod;
  @Mock private MixedOperation mixedOperationDeployment;
  @Mock private MixedOperation mixedOperationService;
  @Mock private NonNamespaceOperation namespacePvcOperation;
  @Mock private NonNamespaceOperation namespaceConfigMapOperation;
  @Mock private NonNamespaceOperation namespacePodOperation;
  @Mock private NonNamespaceOperation namespaceDeploymentOperation;
  @Mock private NonNamespaceOperation namespaceServiceOperation;
  @Mock private AppsAPIGroupDSL apps;
  @Captor private ArgumentCaptor<Watcher<Pod>> watcherCaptor;

  private Map<String, String> attributes;
  private AsyncStorageProvisioner asyncStorageProvisioner;
  private SshPairImpl sshPair;

  @BeforeMethod
  public void setUp() {
    asyncStorageProvisioner =
        new AsyncStorageProvisioner(
            "Always",
            "10Gi",
            "org/image:tag",
            "ReadWriteOnce",
            "common",
            VPC_NAME,
            "storage",
            sshManager,
            clientFactory);
    attributes = new HashMap<>(2);
    attributes.put(ASYNC_PERSIST_ATTRIBUTE, "true");
    attributes.put(PERSIST_VOLUMES_ATTRIBUTE, "false");
    sshPair = new SshPairImpl(USER, "internal", SSH_KEY_NAME, "", "");
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only for 'common' PVC strategy.*")
  public void shouldThrowExceptionIfNotCommonStrategy() throws Exception {
    AsyncStorageProvisioner asyncStorageProvisioner =
        new AsyncStorageProvisioner(
            "Always",
            "10Gi",
            "org/image:tag",
            "ReadWriteOnce",
            randomUUID().toString(),
            VPC_NAME,
            "storageClass",
            sshManager,
            clientFactory);
    when(kubernetesEnvironment.getAttributes()).thenReturn(attributes);
    asyncStorageProvisioner.provision(kubernetesEnvironment, identity);
    verifyNoMoreInteractions(sshManager);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only if 'persistVolumes' attribute set to false")
  public void shouldThrowExceptionIfAsyncStorageForNotEphemeralWorkspace() throws Exception {
    Map attributes = new HashMap<>(2);
    attributes.put(ASYNC_PERSIST_ATTRIBUTE, "true");
    attributes.put(PERSIST_VOLUMES_ATTRIBUTE, "true");
    when(kubernetesEnvironment.getAttributes()).thenReturn(attributes);
    asyncStorageProvisioner.provision(kubernetesEnvironment, identity);
    verifyNoMoreInteractions(sshManager);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldDoNothingIfNotSetAttribute() throws InfrastructureException {
    when(kubernetesEnvironment.getAttributes()).thenReturn(emptyMap());
    asyncStorageProvisioner.provision(kubernetesEnvironment, identity);
    verifyNoMoreInteractions(sshManager);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldDoNothingIfAttributesAsyncPersistOnly() throws InfrastructureException {
    when(kubernetesEnvironment.getAttributes())
        .thenReturn(singletonMap(PERSIST_VOLUMES_ATTRIBUTE, "false"));
    asyncStorageProvisioner.provision(kubernetesEnvironment, identity);
    verifyNoMoreInteractions(sshManager);
    verifyNoMoreInteractions(clientFactory);
    verifyNoMoreInteractions(identity);
  }

  @Test
  public void shouldCreateAll() throws InfrastructureException, ServerException, ConflictException {
    when(kubernetesEnvironment.getAttributes()).thenReturn(attributes);
    when(clientFactory.create(anyString())).thenReturn(kubernetesClient);
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);
    when(identity.getOwnerId()).thenReturn(USER);
    when(sshManager.getPairs(USER, "internal")).thenReturn(singletonList(sshPair));

    when(kubernetesClient.persistentVolumeClaims()).thenReturn(mixedOperationPvc);
    when(mixedOperationPvc.inNamespace(NAMESPACE)).thenReturn(namespacePvcOperation);
    when(namespacePvcOperation.withName(VPC_NAME)).thenReturn(pvcResource);
    when(pvcResource.get()).thenReturn(null);

    when(kubernetesClient.configMaps()).thenReturn(mixedOperationConfigMap);
    when(mixedOperationConfigMap.inNamespace(NAMESPACE)).thenReturn(namespaceConfigMapOperation);
    when(namespaceConfigMapOperation.withName(anyString())).thenReturn(mapResource);
    when(mapResource.get()).thenReturn(null);

    when(kubernetesClient.apps()).thenReturn(apps);
    when(apps.deployments()).thenReturn(mixedOperationDeployment);
    when(mixedOperationDeployment.inNamespace(NAMESPACE)).thenReturn(namespaceDeploymentOperation);
    when(namespaceDeploymentOperation.withName(ASYNC_STORAGE)).thenReturn(deploymentResource);
    when(deploymentResource.get()).thenReturn(null);

    when(kubernetesClient.services()).thenReturn(mixedOperationService);
    when(mixedOperationService.inNamespace(NAMESPACE)).thenReturn(namespaceServiceOperation);
    when(namespaceServiceOperation.withName(ASYNC_STORAGE)).thenReturn(serviceResource);
    when(serviceResource.get()).thenReturn(null);

    asyncStorageProvisioner.provision(kubernetesEnvironment, identity);

    verify(identity, times(1)).getInfrastructureNamespace();
    verify(identity, times(1)).getOwnerId();
    verify(sshManager, times(1)).getPairs(USER, "internal");
    verify(sshManager, never()).generatePair(USER, "internal", SSH_KEY_NAME);
    verify(kubernetesClient.services().inNamespace(NAMESPACE), times(1)).create(any(Service.class));
    verify(kubernetesClient.configMaps().inNamespace(NAMESPACE), times(1))
        .create(any(ConfigMap.class));
    verify(kubernetesClient.apps().deployments().inNamespace(NAMESPACE), times(1))
        .create(any(Deployment.class));
    verify(kubernetesClient.persistentVolumeClaims().inNamespace(NAMESPACE), times(1))
        .create(any(PersistentVolumeClaim.class));
  }

  @Test
  public void shouldNotCreateConfigMap()
      throws InfrastructureException, ServerException, ConflictException {
    when(kubernetesEnvironment.getAttributes()).thenReturn(attributes);
    when(clientFactory.create(anyString())).thenReturn(kubernetesClient);
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);
    when(identity.getOwnerId()).thenReturn(USER);

    when(kubernetesClient.persistentVolumeClaims()).thenReturn(mixedOperationPvc);
    when(mixedOperationPvc.inNamespace(NAMESPACE)).thenReturn(namespacePvcOperation);
    when(namespacePvcOperation.withName(VPC_NAME)).thenReturn(pvcResource);
    when(pvcResource.get()).thenReturn(null);

    when(kubernetesClient.configMaps()).thenReturn(mixedOperationConfigMap);
    when(mixedOperationConfigMap.inNamespace(NAMESPACE)).thenReturn(namespaceConfigMapOperation);
    when(namespaceConfigMapOperation.withName(CONFIGMAP_NAME)).thenReturn(mapResource);
    ObjectMeta meta = new ObjectMeta();
    meta.setName(CONFIGMAP_NAME);
    ConfigMap configMap = new ConfigMap();
    configMap.setMetadata(meta);
    when(mapResource.get()).thenReturn(configMap);

    when(kubernetesClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);
    when(podResource.get()).thenReturn(null);

    when(kubernetesClient.apps()).thenReturn(apps);
    when(apps.deployments()).thenReturn(mixedOperationDeployment);
    when(mixedOperationDeployment.inNamespace(NAMESPACE)).thenReturn(namespaceDeploymentOperation);
    when(namespaceDeploymentOperation.withName(ASYNC_STORAGE)).thenReturn(deploymentResource);
    when(deploymentResource.get()).thenReturn(null);

    when(kubernetesClient.services()).thenReturn(mixedOperationService);
    when(mixedOperationService.inNamespace(NAMESPACE)).thenReturn(namespaceServiceOperation);
    when(namespaceServiceOperation.withName(ASYNC_STORAGE)).thenReturn(serviceResource);
    when(serviceResource.get()).thenReturn(null);

    asyncStorageProvisioner.provision(kubernetesEnvironment, identity);
    verify(identity, times(1)).getInfrastructureNamespace();
    verify(identity, times(1)).getOwnerId();
    verify(identity, times(1)).getWorkspaceId();
    verify(sshManager, never()).getPairs(USER, "internal");
    verify(sshManager, never()).generatePair(USER, "internal", SSH_KEY_NAME);
    verify(kubernetesClient.services().inNamespace(NAMESPACE), times(1)).create(any(Service.class));
    verify(kubernetesClient.configMaps().inNamespace(NAMESPACE), never())
        .create(any(ConfigMap.class));
    verify(kubernetesClient.apps().deployments().inNamespace(NAMESPACE), times(1))
        .create(any(Deployment.class));
    verify(kubernetesClient.persistentVolumeClaims().inNamespace(NAMESPACE), times(1))
        .create(any(PersistentVolumeClaim.class));
  }

  @Test
  public void shouldNotCreatePod()
      throws InfrastructureException, ServerException, ConflictException {
    when(kubernetesEnvironment.getAttributes()).thenReturn(attributes);
    when(clientFactory.create(anyString())).thenReturn(kubernetesClient);
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);
    when(identity.getOwnerId()).thenReturn(USER);
    when(sshManager.getPairs(USER, "internal")).thenReturn(singletonList(sshPair));

    when(kubernetesClient.persistentVolumeClaims()).thenReturn(mixedOperationPvc);
    when(mixedOperationPvc.inNamespace(NAMESPACE)).thenReturn(namespacePvcOperation);
    when(namespacePvcOperation.withName(VPC_NAME)).thenReturn(pvcResource);
    when(pvcResource.get()).thenReturn(null);

    when(kubernetesClient.configMaps()).thenReturn(mixedOperationConfigMap);
    when(mixedOperationConfigMap.inNamespace(NAMESPACE)).thenReturn(namespaceConfigMapOperation);
    when(namespaceConfigMapOperation.withName(CONFIGMAP_NAME)).thenReturn(mapResource);
    when(mapResource.get()).thenReturn(null);

    when(kubernetesClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);

    when(kubernetesClient.apps()).thenReturn(apps);
    when(apps.deployments()).thenReturn(mixedOperationDeployment);
    when(mixedOperationDeployment.inNamespace(NAMESPACE)).thenReturn(namespaceDeploymentOperation);
    when(namespaceDeploymentOperation.withName(ASYNC_STORAGE)).thenReturn(deploymentResource);
    ObjectMeta meta = new ObjectMeta();
    meta.setName(ASYNC_STORAGE);
    Deployment deployment = new Deployment();
    deployment.setMetadata(meta);
    when(deploymentResource.get()).thenReturn(deployment);

    when(kubernetesClient.services()).thenReturn(mixedOperationService);
    when(mixedOperationService.inNamespace(NAMESPACE)).thenReturn(namespaceServiceOperation);
    when(namespaceServiceOperation.withName(ASYNC_STORAGE)).thenReturn(serviceResource);
    when(serviceResource.get()).thenReturn(null);

    asyncStorageProvisioner.provision(kubernetesEnvironment, identity);
    verify(identity, times(1)).getInfrastructureNamespace();
    verify(identity, times(1)).getOwnerId();
    verify(sshManager, times(1)).getPairs(USER, "internal");
    verify(sshManager, never()).generatePair(USER, "internal", SSH_KEY_NAME);
    verify(kubernetesClient.services().inNamespace(NAMESPACE), times(1)).create(any(Service.class));
    verify(kubernetesClient.configMaps().inNamespace(NAMESPACE), times(1))
        .create(any(ConfigMap.class));
    verify(kubernetesClient.pods().inNamespace(NAMESPACE), never()).create(any(Pod.class));
    verify(kubernetesClient.persistentVolumeClaims().inNamespace(NAMESPACE), times(1))
        .create(any(PersistentVolumeClaim.class));
  }

  @Test
  public void shouldNotCreateService()
      throws InfrastructureException, ServerException, ConflictException {
    when(kubernetesEnvironment.getAttributes()).thenReturn(attributes);
    when(clientFactory.create(anyString())).thenReturn(kubernetesClient);
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);
    when(identity.getOwnerId()).thenReturn(USER);
    when(sshManager.getPairs(USER, "internal")).thenReturn(singletonList(sshPair));

    when(kubernetesClient.persistentVolumeClaims()).thenReturn(mixedOperationPvc);
    when(mixedOperationPvc.inNamespace(NAMESPACE)).thenReturn(namespacePvcOperation);
    when(namespacePvcOperation.withName(VPC_NAME)).thenReturn(pvcResource);
    when(pvcResource.get()).thenReturn(null);

    when(kubernetesClient.configMaps()).thenReturn(mixedOperationConfigMap);
    when(mixedOperationConfigMap.inNamespace(NAMESPACE)).thenReturn(namespaceConfigMapOperation);
    when(namespaceConfigMapOperation.withName(CONFIGMAP_NAME)).thenReturn(mapResource);
    when(mapResource.get()).thenReturn(null);

    when(kubernetesClient.pods()).thenReturn(mixedOperationPod);
    when(mixedOperationPod.inNamespace(NAMESPACE)).thenReturn(namespacePodOperation);
    when(namespacePodOperation.withName(ASYNC_STORAGE)).thenReturn(podResource);
    when(podResource.get()).thenReturn(null);

    when(kubernetesClient.apps()).thenReturn(apps);
    when(apps.deployments()).thenReturn(mixedOperationDeployment);
    when(mixedOperationDeployment.inNamespace(NAMESPACE)).thenReturn(namespaceDeploymentOperation);
    when(namespaceDeploymentOperation.withName(ASYNC_STORAGE)).thenReturn(deploymentResource);
    when(deploymentResource.get()).thenReturn(null);

    when(kubernetesClient.services()).thenReturn(mixedOperationService);
    when(mixedOperationService.inNamespace(NAMESPACE)).thenReturn(namespaceServiceOperation);
    when(namespaceServiceOperation.withName(ASYNC_STORAGE)).thenReturn(serviceResource);
    ObjectMeta meta = new ObjectMeta();
    meta.setName(ASYNC_STORAGE);
    Service service = new Service();
    service.setMetadata(meta);
    when(serviceResource.get()).thenReturn(service);

    asyncStorageProvisioner.provision(kubernetesEnvironment, identity);
    verify(identity, times(1)).getInfrastructureNamespace();
    verify(identity, times(1)).getOwnerId();
    verify(sshManager, times(1)).getPairs(USER, "internal");
    verify(sshManager, never()).generatePair(USER, "internal", SSH_KEY_NAME);
    verify(kubernetesClient.services().inNamespace(NAMESPACE), never()).create(any(Service.class));
    verify(kubernetesClient.configMaps().inNamespace(NAMESPACE), times(1))
        .create(any(ConfigMap.class));
    verify(kubernetesClient.apps().deployments().inNamespace(NAMESPACE), times(1))
        .create(any(Deployment.class));
    verify(kubernetesClient.persistentVolumeClaims().inNamespace(NAMESPACE), times(1))
        .create(any(PersistentVolumeClaim.class));
  }
}
