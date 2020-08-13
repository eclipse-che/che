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
package org.eclipse.che.workspace.infrastructure.openshift.wsplugins;

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
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.BrokersResult;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesPluginsToolingValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.PluginBrokerManager;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.DeployBroker;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.ListenBrokerEvents;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.PrepareStorage;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.WaitBrokerResult;
import org.eclipse.che.workspace.infrastructure.openshift.provision.Openshift4TrustedCAProvisioner;

public class OpenshiftPluginBrokerManager<E extends KubernetesEnvironment>
    extends PluginBrokerManager<E> {

  private final Openshift4TrustedCAProvisioner trustedCAProvisioner;

  @Inject
  public OpenshiftPluginBrokerManager(
      KubernetesNamespaceFactory factory,
      EventService eventService,
      KubernetesPluginsToolingValidator pluginsValidator,
      KubernetesEnvironmentProvisioner<E> environmentProvisioner,
      WorkspaceVolumesStrategy volumesStrategy,
      BrokerEnvironmentFactory<E> brokerEnvironmentFactory,
      UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory,
      @Named("che.workspace.plugin_broker.wait_timeout_min") int pluginBrokerWaitingTimeout,
      RuntimeEventsPublisher runtimeEventsPublisher,
      Tracer tracer,
      Openshift4TrustedCAProvisioner trustedCAProvisioner) {
    super(
        factory,
        eventService,
        pluginsValidator,
        environmentProvisioner,
        volumesStrategy,
        brokerEnvironmentFactory,
        unrecoverablePodEventListenerFactory,
        pluginBrokerWaitingTimeout,
        runtimeEventsPublisher,
        tracer);
    this.trustedCAProvisioner = trustedCAProvisioner;
  }

  @Beta
  @Traced
  public List<ChePlugin> getTooling(
      RuntimeIdentity identity,
      StartSynchronizer startSynchronizer,
      Collection<PluginFQN> pluginFQNs,
      boolean isEphemeral,
      Map<String, String> startOptions)
      throws InfrastructureException {

    String workspaceId = identity.getWorkspaceId();
    KubernetesNamespace kubernetesNamespace = factory.getOrCreate(identity);
    BrokersResult brokersResult = new BrokersResult();

    E brokerEnvironment = brokerEnvironmentFactory.createForMetadataBroker(pluginFQNs, identity);
    if (isEphemeral) {
      EphemeralWorkspaceUtility.makeEphemeral(brokerEnvironment.getAttributes());
    }

    environmentProvisioner.provision(brokerEnvironment, identity);
    trustedCAProvisioner.provision(brokerEnvironment, kubernetesNamespace);

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
}
