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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.DeployBroker;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.ListenBrokerEvents;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.PrepareStorage;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.WaitBrokerResult;

/**
 * Deploys Che plugin broker in a workspace, receives result of its execution and return resolved
 * workspace tooling or error of plugin broker execution.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class PluginBrokerManager<E extends KubernetesEnvironment> {

  private final int pluginBrokerWaitingTimeout;
  private final KubernetesNamespaceFactory factory;
  private final EventService eventService;
  private final WorkspaceVolumesStrategy volumesStrategy;
  private final BrokerEnvironmentFactory<E> brokerEnvironmentConfig;
  private final KubernetesEnvironmentProvisioner<E> environmentProvisioner;

  @Inject
  public PluginBrokerManager(
      KubernetesNamespaceFactory factory,
      EventService eventService,
      KubernetesEnvironmentProvisioner<E> environmentProvisioner,
      WorkspaceVolumesStrategy volumesStrategy,
      BrokerEnvironmentFactory<E> brokerEnvironmentConfig,
      @Named("che.workspace.plugin_broker.wait_timeout_min") int pluginBrokerWaitingTimeout) {
    this.factory = factory;
    this.eventService = eventService;
    this.volumesStrategy = volumesStrategy;
    this.brokerEnvironmentConfig = brokerEnvironmentConfig;
    this.environmentProvisioner = environmentProvisioner;
    this.pluginBrokerWaitingTimeout = pluginBrokerWaitingTimeout;
  }

  /**
   * Deploys Che plugin broker in a workspace, receives result of its execution and return resolved
   * workspace tooling or error of plugin broker execution.
   *
   * <p>This API is in <b>Beta</b> and is subject to changes or removal.
   */
  @Beta
  public List<ChePlugin> getTooling(RuntimeIdentity runtimeID, Collection<PluginMeta> pluginsMeta)
      throws InfrastructureException {

    String workspaceId = runtimeID.getWorkspaceId();
    CompletableFuture<List<ChePlugin>> toolingFuture = new CompletableFuture<>();
    KubernetesNamespace kubernetesNamespace = factory.create(workspaceId);
    E brokerEnvironment = brokerEnvironmentConfig.create(pluginsMeta, runtimeID);

    environmentProvisioner.provision(brokerEnvironment, runtimeID);

    ListenBrokerEvents listenBrokerEvents = getListenEventPhase(workspaceId, toolingFuture);
    PrepareStorage prepareStorage = getPrepareStoragePhase(workspaceId, brokerEnvironment);
    WaitBrokerResult waitBrokerResult = getWaitBrokerPhase(toolingFuture);
    DeployBroker deployBroker = getDeployBrokerPhase(kubernetesNamespace, brokerEnvironment);

    listenBrokerEvents.then(prepareStorage).then(deployBroker).then(waitBrokerResult);
    return listenBrokerEvents.execute();
  }

  private ListenBrokerEvents getListenEventPhase(
      String workspaceId, CompletableFuture<List<ChePlugin>> toolingFuture) {
    return new ListenBrokerEvents(workspaceId, toolingFuture, eventService);
  }

  private PrepareStorage getPrepareStoragePhase(
      String workspaceId, KubernetesEnvironment brokerEnvironment) {
    return new PrepareStorage(workspaceId, brokerEnvironment, volumesStrategy);
  }

  private DeployBroker getDeployBrokerPhase(
      KubernetesNamespace kubernetesNamespace, KubernetesEnvironment brokerEnvironment) {
    return new DeployBroker(kubernetesNamespace, brokerEnvironment);
  }

  private WaitBrokerResult getWaitBrokerPhase(CompletableFuture<List<ChePlugin>> toolingFuture) {
    return new WaitBrokerResult(toolingFuture, pluginBrokerWaitingTimeout);
  }
}
