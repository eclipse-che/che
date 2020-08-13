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
import io.opentracing.Tracer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
public abstract class PluginBrokerManager<E extends KubernetesEnvironment> {

  protected static final Logger LOG = LoggerFactory.getLogger(PluginBrokerManager.class);

  protected final int pluginBrokerWaitingTimeout;
  protected final KubernetesNamespaceFactory factory;
  protected final EventService eventService;
  protected final KubernetesPluginsToolingValidator pluginsValidator;
  protected final WorkspaceVolumesStrategy volumesStrategy;
  protected final BrokerEnvironmentFactory<E> brokerEnvironmentFactory;
  protected final KubernetesEnvironmentProvisioner<E> environmentProvisioner;
  protected final UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory;
  protected final RuntimeEventsPublisher runtimeEventsPublisher;
  protected final Tracer tracer;

  public PluginBrokerManager(
      KubernetesNamespaceFactory factory,
      EventService eventService,
      KubernetesPluginsToolingValidator pluginsValidator,
      KubernetesEnvironmentProvisioner<E> environmentProvisioner,
      WorkspaceVolumesStrategy volumesStrategy,
      BrokerEnvironmentFactory<E> brokerEnvironmentFactory,
      UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory,
      int pluginBrokerWaitingTimeout,
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
  public abstract List<ChePlugin> getTooling(
      RuntimeIdentity identity,
      StartSynchronizer startSynchronizer,
      Collection<PluginFQN> pluginFQNs,
      boolean isEphemeral,
      Map<String, String> startOptions)
      throws InfrastructureException;

  protected ListenBrokerEvents getListenEventPhase(
      String workspaceId, BrokersResult brokersResult) {
    return new ListenBrokerEvents(workspaceId, pluginsValidator, brokersResult, eventService);
  }

  protected PrepareStorage getPrepareStoragePhase(
      RuntimeIdentity identity,
      StartSynchronizer startSynchronizer,
      KubernetesEnvironment brokerEnvironment,
      Map<String, String> startOptions) {
    return new PrepareStorage(
        identity, brokerEnvironment, volumesStrategy, startSynchronizer, tracer, startOptions);
  }

  protected DeployBroker getDeployBrokerPhase(
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

  protected WaitBrokerResult getWaitBrokerPhase(String workspaceId, BrokersResult brokersResult) {
    return new WaitBrokerResult(workspaceId, brokersResult, pluginBrokerWaitingTimeout, tracer);
  }
}
