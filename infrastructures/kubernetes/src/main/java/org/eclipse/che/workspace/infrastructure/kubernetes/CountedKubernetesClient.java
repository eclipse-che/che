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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import io.fabric8.kubernetes.api.model.ComponentStatus;
import io.fabric8.kubernetes.api.model.ComponentStatusList;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.DoneableComponentStatus;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.api.model.DoneableEndpoints;
import io.fabric8.kubernetes.api.model.DoneableEvent;
import io.fabric8.kubernetes.api.model.DoneableLimitRange;
import io.fabric8.kubernetes.api.model.DoneableNamespace;
import io.fabric8.kubernetes.api.model.DoneableNode;
import io.fabric8.kubernetes.api.model.DoneablePersistentVolume;
import io.fabric8.kubernetes.api.model.DoneablePersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.DoneableReplicationController;
import io.fabric8.kubernetes.api.model.DoneableResourceQuota;
import io.fabric8.kubernetes.api.model.DoneableSecret;
import io.fabric8.kubernetes.api.model.DoneableService;
import io.fabric8.kubernetes.api.model.DoneableServiceAccount;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.LimitRange;
import io.fabric8.kubernetes.api.model.LimitRangeList;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.PersistentVolumeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerList;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ResourceQuotaList;
import io.fabric8.kubernetes.api.model.RootPaths;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountList;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apiextensions.DoneableCustomResourceDefinition;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.AutoscalingAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.BatchAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.ExtensionsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.KubernetesListMixedOperation;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable;
import io.fabric8.kubernetes.client.dsl.NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable;
import io.fabric8.kubernetes.client.dsl.NetworkAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.RbacAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import io.fabric8.kubernetes.client.dsl.StorageAPIGroupDSL;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

/** Wrapper over {@link KubernetesClient} that counts all client invocations. */
public class CountedKubernetesClient implements KubernetesClient {

  private final KubernetesClient client;
  protected final Runnable invoked;

  /** @param client to wrap */
  public CountedKubernetesClient(KubernetesClient client, Runnable invoked) {
    this.client = client;
    this.invoked = invoked;
  }

  @Override
  public NonNamespaceOperation<
          CustomResourceDefinition,
          CustomResourceDefinitionList,
          DoneableCustomResourceDefinition,
          Resource<CustomResourceDefinition, DoneableCustomResourceDefinition>>
      customResourceDefinitions() {
    invoked.run();
    return client.customResourceDefinitions();
  }

  @Override
  public <T extends HasMetadata, L extends KubernetesResourceList, D extends Doneable<T>>
      MixedOperation<T, L, D, Resource<T, D>> customResources(
          CustomResourceDefinition crd,
          Class<T> resourceType,
          Class<L> listClass,
          Class<D> doneClass) {
    invoked.run();
    return client.customResources(crd, resourceType, listClass, doneClass);
  }

  @Override
  public <T extends HasMetadata, L extends KubernetesResourceList, D extends Doneable<T>>
      MixedOperation<T, L, D, Resource<T, D>> customResource(
          CustomResourceDefinition crd,
          Class<T> resourceType,
          Class<L> listClass,
          Class<D> doneClass) {
    invoked.run();
    return client.customResource(crd, resourceType, listClass, doneClass);
  }

  @Override
  public ExtensionsAPIGroupDSL extensions() {
    invoked.run();
    return client.extensions();
  }

  @Override
  public VersionInfo getVersion() {
    invoked.run();
    return client.getVersion();
  }

  @Override
  public AppsAPIGroupDSL apps() {
    invoked.run();
    return client.apps();
  }

  @Override
  public AutoscalingAPIGroupDSL autoscaling() {
    invoked.run();
    return client.autoscaling();
  }

  @Override
  public NetworkAPIGroupDSL network() {
    invoked.run();
    return client.network();
  }

  @Override
  public StorageAPIGroupDSL storage() {
    invoked.run();
    return client.storage();
  }

  @Override
  public BatchAPIGroupDSL batch() {
    invoked.run();
    return client.batch();
  }

  @Override
  public RbacAPIGroupDSL rbac() {
    invoked.run();
    return client.rbac();
  }

  @Override
  public MixedOperation<
          ComponentStatus,
          ComponentStatusList,
          DoneableComponentStatus,
          Resource<ComponentStatus, DoneableComponentStatus>>
      componentstatuses() {
    invoked.run();
    return client.componentstatuses();
  }

  @Override
  public ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      load(InputStream is) {
    invoked.run();
    return client.load(is);
  }

  @Override
  public ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      resourceList(String s) {
    invoked.run();
    return client.resourceList(s);
  }

  @Override
  public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      resourceList(KubernetesResourceList list) {
    invoked.run();
    return client.resourceList(list);
  }

  @Override
  public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      resourceList(HasMetadata... items) {
    invoked.run();
    return client.resourceList(items);
  }

  @Override
  public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      resourceList(Collection<HasMetadata> items) {
    invoked.run();
    return client.resourceList(items);
  }

  @Override
  public <T extends HasMetadata>
      NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable<T, Boolean> resource(T is) {
    invoked.run();
    return client.resource(is);
  }

  @Override
  public NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      resource(String s) {
    invoked.run();
    return client.resource(s);
  }

  @Override
  public MixedOperation<
          Endpoints, EndpointsList, DoneableEndpoints, Resource<Endpoints, DoneableEndpoints>>
      endpoints() {
    invoked.run();
    return client.endpoints();
  }

  @Override
  public MixedOperation<Event, EventList, DoneableEvent, Resource<Event, DoneableEvent>> events() {
    invoked.run();
    return client.events();
  }

  @Override
  public NonNamespaceOperation<
          Namespace, NamespaceList, DoneableNamespace, Resource<Namespace, DoneableNamespace>>
      namespaces() {
    invoked.run();
    return client.namespaces();
  }

  @Override
  public NonNamespaceOperation<Node, NodeList, DoneableNode, Resource<Node, DoneableNode>> nodes() {
    invoked.run();
    return client.nodes();
  }

  @Override
  public NonNamespaceOperation<
          PersistentVolume,
          PersistentVolumeList,
          DoneablePersistentVolume,
          Resource<PersistentVolume, DoneablePersistentVolume>>
      persistentVolumes() {
    invoked.run();
    return client.persistentVolumes();
  }

  @Override
  public MixedOperation<
          PersistentVolumeClaim,
          PersistentVolumeClaimList,
          DoneablePersistentVolumeClaim,
          Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>>
      persistentVolumeClaims() {
    invoked.run();
    return client.persistentVolumeClaims();
  }

  @Override
  public MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> pods() {
    invoked.run();
    return client.pods();
  }

  @Override
  public MixedOperation<
          ReplicationController,
          ReplicationControllerList,
          DoneableReplicationController,
          RollableScalableResource<ReplicationController, DoneableReplicationController>>
      replicationControllers() {
    invoked.run();
    return client.replicationControllers();
  }

  @Override
  public MixedOperation<
          ResourceQuota,
          ResourceQuotaList,
          DoneableResourceQuota,
          Resource<ResourceQuota, DoneableResourceQuota>>
      resourceQuotas() {
    invoked.run();
    return client.resourceQuotas();
  }

  @Override
  public MixedOperation<Secret, SecretList, DoneableSecret, Resource<Secret, DoneableSecret>>
      secrets() {
    invoked.run();
    return client.secrets();
  }

  @Override
  public MixedOperation<
          Service, ServiceList, DoneableService, ServiceResource<Service, DoneableService>>
      services() {
    invoked.run();
    return client.services();
  }

  @Override
  public MixedOperation<
          ServiceAccount,
          ServiceAccountList,
          DoneableServiceAccount,
          Resource<ServiceAccount, DoneableServiceAccount>>
      serviceAccounts() {
    invoked.run();
    return client.serviceAccounts();
  }

  @Override
  public KubernetesListMixedOperation lists() {
    invoked.run();
    return client.lists();
  }

  @Override
  public MixedOperation<
          ConfigMap, ConfigMapList, DoneableConfigMap, Resource<ConfigMap, DoneableConfigMap>>
      configMaps() {
    invoked.run();
    return client.configMaps();
  }

  @Override
  public MixedOperation<
          LimitRange, LimitRangeList, DoneableLimitRange, Resource<LimitRange, DoneableLimitRange>>
      limitRanges() {
    invoked.run();
    return client.limitRanges();
  }

  @Override
  public <C> Boolean isAdaptable(Class<C> type) {
    invoked.run();
    return client.isAdaptable(type);
  }

  @Override
  public <C> C adapt(Class<C> type) {
    invoked.run();
    return client.adapt(type);
  }

  @Override
  public URL getMasterUrl() {
    invoked.run();
    return client.getMasterUrl();
  }

  @Override
  public String getApiVersion() {
    invoked.run();
    return client.getApiVersion();
  }

  @Override
  public String getNamespace() {
    invoked.run();
    return client.getNamespace();
  }

  @Override
  public RootPaths rootPaths() {
    invoked.run();
    return client.rootPaths();
  }

  @Override
  public boolean supportsApiPath(String path) {
    invoked.run();
    return client.supportsApiPath(path);
  }

  @Override
  public void close() {
    client.close();
  }

  @Override
  public Config getConfiguration() {
    invoked.run();
    return client.getConfiguration();
  }
}
