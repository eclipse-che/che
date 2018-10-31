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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListener;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListenerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.BrokersResult;
import org.slf4j.Logger;

/**
 * Deploys Che plugin broker in a workspace, calls next {@link BrokerPhase} and removes deployment
 * after next phase completes.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class DeployBroker extends BrokerPhase {

  private static final Logger LOG = getLogger(DeployBroker.class);

  private final KubernetesNamespace namespace;
  private final KubernetesEnvironment brokerEnvironment;
  private final BrokersResult brokersResult;
  private final UnrecoverablePodEventListenerFactory factory;
  private final String workspaceId;

  public DeployBroker(
      String workspaceId,
      KubernetesNamespace namespace,
      KubernetesEnvironment brokerEnvironment,
      BrokersResult brokersResult,
      UnrecoverablePodEventListenerFactory factory) {
    this.workspaceId = workspaceId;
    this.namespace = namespace;
    this.brokerEnvironment = brokerEnvironment;
    this.brokersResult = brokersResult;
    this.factory = factory;
  }

  @Override
  public List<ChePlugin> execute() throws InfrastructureException {
    KubernetesDeployments deployments = namespace.deployments();
    try {
      // Creates config map that can inject Che tooling plugins meta files into a Che plugin
      // broker in a workspace.
      for (ConfigMap configMap : brokerEnvironment.getConfigMaps().values()) {
        namespace.configMaps().create(configMap);
      }

      Pod pluginBrokerPod = getPluginBrokerPod(brokerEnvironment.getPods());

      if (factory.isConfigured()) {
        UnrecoverablePodEventListener unrecoverableEventListener =
            factory.create(
                ImmutableSet.of(pluginBrokerPod.getMetadata().getName()),
                this::handleUnrecoverableEvent);
        namespace.deployments().watchEvents(unrecoverableEventListener);
      }

      Pod barePod = deployments.create(pluginBrokerPod);

      deployments.waitRunningAsync(barePod.getMetadata().getName());

      return nextPhase.execute();
    } finally {
      namespace.deployments().stopWatch();
      try {
        deployments.delete();
      } catch (InfrastructureException e) {
        LOG.error("Broker deployment removal failed. Error: " + e.getLocalizedMessage(), e);
      }
      try {
        namespace.configMaps().delete();
      } catch (InfrastructureException ex) {
        LOG.error(
            "Broker deployment config map removal failed. Error: " + ex.getLocalizedMessage(), ex);
      }
    }
  }

  private void handleUnrecoverableEvent(PodEvent podEvent) {
    String reason = podEvent.getReason();
    String message = podEvent.getMessage();
    LOG.error(
        "Unrecoverable event occurred during plugin broking for workspace '{}' startup: {}, {}, {}",
        workspaceId,
        reason,
        message,
        podEvent.getPodName());
    brokersResult.error(
        new InfrastructureException(
            format(
                "Unrecoverable event occurred: '%s', '%s', '%s'",
                reason, message, podEvent.getPodName())));
  }

  private Pod getPluginBrokerPod(Map<String, Pod> pods) throws InfrastructureException {
    if (pods.size() != 1) {
      throw new InternalInfrastructureException(
          format(
              "Plugin broker environment must have only "
                  + "one pod. Workspace `%s` contains `%s` pods.",
              workspaceId, pods.size()));
    }

    return pods.values().iterator().next();
  }
}
