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
import io.micrometer.core.instrument.Counter;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

/**
 * Wrapper over {@link KubernetesClient} that counts all client invocations to the {@link Counter}.
 */
public class CountedKubernetesClient implements KubernetesClient {

  private final KubernetesClient client;
  protected final Counter invocationCounter;

  /**
   * @param client to wrap
   * @param invocationCounter every method call will be recorder by this counter
   */
  public CountedKubernetesClient(KubernetesClient client, Counter invocationCounter) {
    this.client = client;
    this.invocationCounter = invocationCounter;
  }

  @Override
  public NonNamespaceOperation<
          CustomResourceDefinition,
          CustomResourceDefinitionList,
          DoneableCustomResourceDefinition,
          Resource<CustomResourceDefinition, DoneableCustomResourceDefinition>>
      customResourceDefinitions() {
    invocationCounter.increment();
    return client.customResourceDefinitions();
  }

  @Override
  public <T extends HasMetadata, L extends KubernetesResourceList, D extends Doneable<T>>
      MixedOperation<T, L, D, Resource<T, D>> customResources(
          CustomResourceDefinition crd,
          Class<T> resourceType,
          Class<L> listClass,
          Class<D> doneClass) {
    invocationCounter.increment();
    return client.customResources(crd, resourceType, listClass, doneClass);
  }

  @Override
  public <T extends HasMetadata, L extends KubernetesResourceList, D extends Doneable<T>>
      MixedOperation<T, L, D, Resource<T, D>> customResource(
          CustomResourceDefinition crd,
          Class<T> resourceType,
          Class<L> listClass,
          Class<D> doneClass) {
    invocationCounter.increment();
    return client.customResource(crd, resourceType, listClass, doneClass);
  }

  @Override
  public ExtensionsAPIGroupDSL extensions() {
    invocationCounter.increment();
    return client.extensions();
  }

  @Override
  public VersionInfo getVersion() {
    invocationCounter.increment();
    return client.getVersion();
  }

  @Override
  public AppsAPIGroupDSL apps() {
    invocationCounter.increment();
    return client.apps();
  }

  @Override
  public AutoscalingAPIGroupDSL autoscaling() {
    invocationCounter.increment();
    return client.autoscaling();
  }

  @Override
  public NetworkAPIGroupDSL network() {
    invocationCounter.increment();
    return client.network();
  }

  @Override
  public StorageAPIGroupDSL storage() {
    invocationCounter.increment();
    return client.storage();
  }

  @Override
  public BatchAPIGroupDSL batch() {
    invocationCounter.increment();
    return client.batch();
  }

  @Override
  public RbacAPIGroupDSL rbac() {
    invocationCounter.increment();
    return client.rbac();
  }

  @Override
  public MixedOperation<
          ComponentStatus,
          ComponentStatusList,
          DoneableComponentStatus,
          Resource<ComponentStatus, DoneableComponentStatus>>
      componentstatuses() {
    invocationCounter.increment();
    return client.componentstatuses();
  }

  @Override
  public ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      load(InputStream is) {
    invocationCounter.increment();
    return client.load(is);
  }

  @Override
  public ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      resourceList(String s) {
    invocationCounter.increment();
    return client.resourceList(s);
  }

  @Override
  public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      resourceList(KubernetesResourceList list) {
    invocationCounter.increment();
    return client.resourceList(list);
  }

  @Override
  public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      resourceList(HasMetadata... items) {
    invocationCounter.increment();
    return client.resourceList(items);
  }

  @Override
  public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      resourceList(Collection<HasMetadata> items) {
    invocationCounter.increment();
    return client.resourceList(items);
  }

  @Override
  public <T extends HasMetadata>
      NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable<T, Boolean> resource(T is) {
    invocationCounter.increment();
    return client.resource(is);
  }

  @Override
  public NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      resource(String s) {
    invocationCounter.increment();
    return client.resource(s);
  }

  @Override
  public MixedOperation<
          Endpoints, EndpointsList, DoneableEndpoints, Resource<Endpoints, DoneableEndpoints>>
      endpoints() {
    invocationCounter.increment();
    return client.endpoints();
  }

  @Override
  public MixedOperation<Event, EventList, DoneableEvent, Resource<Event, DoneableEvent>> events() {
    invocationCounter.increment();
    return client.events();
  }

  @Override
  public NonNamespaceOperation<
          Namespace, NamespaceList, DoneableNamespace, Resource<Namespace, DoneableNamespace>>
      namespaces() {
    invocationCounter.increment();
    return client.namespaces();
  }

  @Override
  public NonNamespaceOperation<Node, NodeList, DoneableNode, Resource<Node, DoneableNode>> nodes() {
    invocationCounter.increment();
    return client.nodes();
  }

  @Override
  public NonNamespaceOperation<
          PersistentVolume,
          PersistentVolumeList,
          DoneablePersistentVolume,
          Resource<PersistentVolume, DoneablePersistentVolume>>
      persistentVolumes() {
    invocationCounter.increment();
    return client.persistentVolumes();
  }

  @Override
  public MixedOperation<
          PersistentVolumeClaim,
          PersistentVolumeClaimList,
          DoneablePersistentVolumeClaim,
          Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>>
      persistentVolumeClaims() {
    invocationCounter.increment();
    return client.persistentVolumeClaims();
  }

  @Override
  public MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> pods() {
    invocationCounter.increment();
    return client.pods();
  }

  @Override
  public MixedOperation<
          ReplicationController,
          ReplicationControllerList,
          DoneableReplicationController,
          RollableScalableResource<ReplicationController, DoneableReplicationController>>
      replicationControllers() {
    invocationCounter.increment();
    return client.replicationControllers();
  }

  @Override
  public MixedOperation<
          ResourceQuota,
          ResourceQuotaList,
          DoneableResourceQuota,
          Resource<ResourceQuota, DoneableResourceQuota>>
      resourceQuotas() {
    invocationCounter.increment();
    return client.resourceQuotas();
  }

  @Override
  public MixedOperation<Secret, SecretList, DoneableSecret, Resource<Secret, DoneableSecret>>
      secrets() {
    invocationCounter.increment();
    return client.secrets();
  }

  @Override
  public MixedOperation<
          Service, ServiceList, DoneableService, ServiceResource<Service, DoneableService>>
      services() {
    invocationCounter.increment();
    return client.services();
  }

  @Override
  public MixedOperation<
          ServiceAccount,
          ServiceAccountList,
          DoneableServiceAccount,
          Resource<ServiceAccount, DoneableServiceAccount>>
      serviceAccounts() {
    invocationCounter.increment();
    return client.serviceAccounts();
  }

  @Override
  public KubernetesListMixedOperation lists() {
    invocationCounter.increment();
    return client.lists();
  }

  @Override
  public MixedOperation<
          ConfigMap, ConfigMapList, DoneableConfigMap, Resource<ConfigMap, DoneableConfigMap>>
      configMaps() {
    invocationCounter.increment();
    return client.configMaps();
  }

  @Override
  public MixedOperation<
          LimitRange, LimitRangeList, DoneableLimitRange, Resource<LimitRange, DoneableLimitRange>>
      limitRanges() {
    invocationCounter.increment();
    return client.limitRanges();
  }

  @Override
  public <C> Boolean isAdaptable(Class<C> type) {
    invocationCounter.increment();
    return client.isAdaptable(type);
  }

  @Override
  public <C> C adapt(Class<C> type) {
    invocationCounter.increment();
    return client.adapt(type);
  }

  @Override
  public URL getMasterUrl() {
    invocationCounter.increment();
    return client.getMasterUrl();
  }

  @Override
  public String getApiVersion() {
    invocationCounter.increment();
    return client.getApiVersion();
  }

  @Override
  public String getNamespace() {
    invocationCounter.increment();
    return client.getNamespace();
  }

  @Override
  public RootPaths rootPaths() {
    invocationCounter.increment();
    return client.rootPaths();
  }

  @Override
  public boolean supportsApiPath(String path) {
    invocationCounter.increment();
    return client.supportsApiPath(path);
  }

  @Override
  public void close() {
    client.close();
  }

  @Override
  public Config getConfiguration() {
    invocationCounter.increment();
    return client.getConfiguration();
  }
}
