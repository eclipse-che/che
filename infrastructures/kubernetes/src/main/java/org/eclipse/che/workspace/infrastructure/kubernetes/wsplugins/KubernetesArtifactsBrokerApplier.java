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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import io.fabric8.kubernetes.api.model.Container;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory;

/**
 * Given a {@link InternalEnvironment} representing a workspace, adds the artifacts plugin broker as
 * an init container to the workspace Pod in order to ensure that any extensions are downloaded.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Angel Misevski
 */
@Beta
public class KubernetesArtifactsBrokerApplier<E extends KubernetesEnvironment> {

  private final BrokerEnvironmentFactory<E> brokerEnvironmentFactory;

  @Inject
  public KubernetesArtifactsBrokerApplier(BrokerEnvironmentFactory<E> brokerEnvironmentFactory) {
    this.brokerEnvironmentFactory = brokerEnvironmentFactory;
  }

  /**
   * Apply plugin broker as init container to workspace environment. Workspace environment will have
   * broker's configmap, machines, and volumes added in addition to the init container
   */
  public void apply(
      E workspaceEnvironment,
      RuntimeIdentity runtimeID,
      Collection<PluginFQN> pluginFQNs,
      boolean mergePlugins)
      throws InfrastructureException {

    E brokerEnvironment =
        brokerEnvironmentFactory.createForArtifactsBroker(pluginFQNs, runtimeID, mergePlugins);

    Map<String, PodData> workspacePods = workspaceEnvironment.getPodsData();
    if (workspacePods.size() != 1) {
      throw new InfrastructureException(
          "Che plugins tooling configuration can be applied to a workspace with one pod only.");
    }
    PodData workspacePod = workspacePods.values().iterator().next();

    Map<String, PodData> brokerPods = brokerEnvironment.getPodsData();
    if (brokerPods.size() != 1) {
      throw new InfrastructureException("Broker environment must have only one Pod.");
    }
    PodData brokerPod = brokerPods.values().iterator().next();

    // Add broker machines to workspace environment so that the init containers can be provisioned.
    List<Container> brokerContainers = brokerPod.getSpec().getContainers();
    for (Container container : brokerContainers) {
      InternalMachineConfig brokerMachine =
          brokerEnvironment.getMachines().get(Names.machineName(brokerPod, container));
      if (brokerMachine == null) {
        throw new InfrastructureException(
            String.format(
                "Could not retrieve the specification of the plugin broker container %s",
                container.getName()));
      }
      workspaceEnvironment
          .getMachines()
          .put(Names.machineName(workspacePod, container), brokerMachine);
    }

    workspaceEnvironment.getConfigMaps().putAll(brokerEnvironment.getConfigMaps());
    workspacePod.getSpec().getInitContainers().addAll(brokerPod.getSpec().getContainers());
    workspacePod.getSpec().getVolumes().addAll(brokerPod.getSpec().getVolumes());
  }
}
