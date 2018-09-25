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
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.ChePluginsApplier;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Applies Che plugins tooling configuration to a kubernetes internal runtime object.
 *
 * @author Oleksander Garagatyi
 */
@Beta
public class KubernetesPluginsToolingApplier implements ChePluginsApplier {

  private final String defaultSidecarMemoryLimitBytes;

  @Inject
  public KubernetesPluginsToolingApplier(
      @Named("che.workspace.sidecar.default_memory_limit_mb") long defaultSidecarMemoryLimitMB) {
    this.defaultSidecarMemoryLimitBytes = String.valueOf(defaultSidecarMemoryLimitMB * 1024 * 1024);
  }

  @Override
  public void apply(InternalEnvironment internalEnvironment, Collection<ChePlugin> chePlugins)
      throws InfrastructureException {
    if (chePlugins.isEmpty()) {
      return;
    }

    KubernetesEnvironment kubernetesEnvironment = (KubernetesEnvironment) internalEnvironment;

    Map<String, Pod> pods = kubernetesEnvironment.getPods();
    if (pods.size() != 1) {
      throw new InfrastructureException(
          "Che plugins tooling configuration can be applied to a workspace with one pod only");
    }
    Pod pod = pods.values().iterator().next();

    for (ChePlugin chePlugin : chePlugins) {
      for (CheContainer container : chePlugin.getContainers()) {
        addSidecar(pod, container, chePlugin, kubernetesEnvironment);
      }
    }
  }

  /**
   * Adds k8s and Che specific configuration of a sidecar into the environment. For example:
   * <li>k8s container configuration {@link Container}
   * <li>k8s service configuration {@link Service}
   * <li>Che machine config {@link InternalMachineConfig}
   *
   * @throws InfrastructureException when any error occurs
   */
  private void addSidecar(
      Pod pod,
      CheContainer container,
      ChePlugin chePlugin,
      KubernetesEnvironment kubernetesEnvironment)
      throws InfrastructureException {

    K8sContainerResolver k8sContainerResolver =
        new K8sContainerResolverBuilder()
            .setContainer(container)
            .setPluginEndpoints(chePlugin.getEndpoints())
            .build();
    List<ChePluginEndpoint> containerEndpoints = k8sContainerResolver.getEndpoints();

    Container k8sContainer = k8sContainerResolver.resolve();

    String machineName = Names.machineName(pod, k8sContainer);
    pod.getSpec().getContainers().add(k8sContainer);

    MachineResolver machineResolver =
        new MachineResolverBuilder()
            .setCheContainer(container)
            .setContainer(k8sContainer)
            .setContainerEndpoints(containerEndpoints)
            .setDefaultSidecarMemorySizeAttribute(defaultSidecarMemoryLimitBytes)
            .setAttributes(kubernetesEnvironment.getAttributes())
            .build();

    InternalMachineConfig machineConfig = machineResolver.resolve();
    kubernetesEnvironment.getMachines().put(machineName, machineConfig);

    SidecarServicesProvisioner sidecarServicesProvisioner =
        new SidecarServicesProvisioner(containerEndpoints, pod.getMetadata().getName());
    sidecarServicesProvisioner.provision(kubernetesEnvironment);
  }
}
