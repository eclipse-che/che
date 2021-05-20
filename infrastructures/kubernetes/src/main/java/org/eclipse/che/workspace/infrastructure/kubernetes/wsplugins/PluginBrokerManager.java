/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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
import io.opentracing.Tracer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizer;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.EphemeralWorkspaceUtility;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListenerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.DeployBroker;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.ListenBrokerEvents;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.PrepareStorage;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.WaitBrokerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger LOG = LoggerFactory.getLogger(PluginBrokerManager.class);

  private final int pluginBrokerWaitingTimeout;
  private final KubernetesNamespaceFactory factory;
  private final EventService eventService;
  private final KubernetesPluginsToolingValidator pluginsValidator;
  private final WorkspaceVolumesStrategy volumesStrategy;
  private final BrokerEnvironmentFactory<E> brokerEnvironmentFactory;
  private final KubernetesEnvironmentProvisioner<E> environmentProvisioner;
  private final UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory;
  private final RuntimeEventsPublisher runtimeEventsPublisher;
  private final Tracer tracer;

  @Inject
  public PluginBrokerManager(
      KubernetesNamespaceFactory factory,
      EventService eventService,
      KubernetesPluginsToolingValidator pluginsValidator,
      KubernetesEnvironmentProvisioner<E> environmentProvisioner,
      WorkspaceVolumesStrategy volumesStrategy,
      BrokerEnvironmentFactory<E> brokerEnvironmentFactory,
      UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory,
      @Named("che.workspace.plugin_broker.wait_timeout_min") int pluginBrokerWaitingTimeout,
      RuntimeEventsPublisher runtimeEventsPublisher,
      Tracer tracer) {
    this.factory = factory;
    this.eventService = eventService;
    this.pluginsValidator = pluginsValidator;
    this.volumesStrategy = volumesStrategy;
    this.brokerEnvironmentFactory = brokerEnvironmentFactory;
    this.environmentProvisioner = environmentProvisioner;
    this.pluginBrokerWaitingTimeout = pluginBrokerWaitingTimeout;
    this.unrecoverablePodEventListenerFactory = unrecoverablePodEventListenerFactory;
    this.runtimeEventsPublisher = runtimeEventsPublisher;
    this.tracer = tracer;
  }

  /**
   * Deploys Che plugin brokers in a workspace, receives result of theirs execution and returns
   * resolved workspace tooling or error of plugins brokering execution.
   *
   * <p>This API is in <b>Beta</b> and is subject to changes or removal.
   */
  @Beta
  @Traced
  public List<ChePlugin> getTooling(
      RuntimeIdentity identity,
      StartSynchronizer startSynchronizer,
      Collection<PluginFQN> pluginFQNs,
      boolean isEphemeral,
      boolean mergePlugins,
      Map<String, String> startOptions)
      throws InfrastructureException {

    String workspaceId = identity.getWorkspaceId();
    KubernetesNamespace kubernetesNamespace = factory.getOrCreate(identity);
    BrokersResult brokersResult = new BrokersResult();

    E brokerEnvironment =
        brokerEnvironmentFactory.createForMetadataBroker(pluginFQNs, identity, mergePlugins);
    if (isEphemeral) {
      EphemeralWorkspaceUtility.makeEphemeral(brokerEnvironment.getAttributes());
    }
    environmentProvisioner.provision(brokerEnvironment, identity);

    ListenBrokerEvents listenBrokerEvents = getListenEventPhase(workspaceId, brokersResult);
    PrepareStorage prepareStorage =
        getPrepareStoragePhase(identity, startSynchronizer, brokerEnvironment, startOptions);
    WaitBrokerResult waitBrokerResult = getWaitBrokerPhase(workspaceId, brokersResult);
    DeployBroker deployBroker =
        getDeployBrokerPhase(
            identity, kubernetesNamespace, brokerEnvironment, brokersResult, startOptions);
    LOG.debug("Entering plugin brokers deployment chain workspace '{}'", workspaceId);
    listenBrokerEvents.then(prepareStorage).then(deployBroker).then(waitBrokerResult);
    return listenBrokerEvents.execute();
  }

  private ListenBrokerEvents getListenEventPhase(String workspaceId, BrokersResult brokersResult) {
    return new ListenBrokerEvents(workspaceId, pluginsValidator, brokersResult, eventService);
  }

  private PrepareStorage getPrepareStoragePhase(
      RuntimeIdentity identity,
      StartSynchronizer startSynchronizer,
      KubernetesEnvironment brokerEnvironment,
      Map<String, String> startOptions) {
    return new PrepareStorage(
        identity, brokerEnvironment, volumesStrategy, startSynchronizer, tracer, startOptions);
  }

  private DeployBroker getDeployBrokerPhase(
      RuntimeIdentity runtimeId,
      KubernetesNamespace kubernetesNamespace,
      KubernetesEnvironment brokerEnvironment,
      BrokersResult brokersResult,
      Map<String, String> startOptions) {
    return new DeployBroker(
        runtimeId,
        kubernetesNamespace,
        brokerEnvironment,
        brokersResult,
        unrecoverablePodEventListenerFactory,
        runtimeEventsPublisher,
        tracer,
        startOptions);
  }

  private WaitBrokerResult getWaitBrokerPhase(String workspaceId, BrokersResult brokersResult) {
    return new WaitBrokerResult(workspaceId, brokersResult, pluginBrokerWaitingTimeout, tracer);
  }
}
