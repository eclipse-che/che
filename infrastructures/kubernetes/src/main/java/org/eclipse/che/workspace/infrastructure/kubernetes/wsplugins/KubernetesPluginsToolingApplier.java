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

import static java.util.Collections.emptyList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider.SECURE_EXPOSER_IMPL_PROPERTY;

import com.google.common.annotations.Beta;
import com.google.common.collect.Sets;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
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
@Singleton
public class KubernetesPluginsToolingApplier implements ChePluginsApplier {

  private static final Set<String> validImagePullPolicies =
      Sets.newHashSet("Always", "Never", "IfNotPresent");
  private final String defaultSidecarMemoryLimitBytes;
  private final String sidecarImagePullPolicy;
  private final boolean isAuthEnabled;

  @Inject
  public KubernetesPluginsToolingApplier(
      @Named("che.workspace.sidecar.image_pull_policy") String sidecarImagePullPolicy,
      @Named("che.workspace.sidecar.default_memory_limit_mb") long defaultSidecarMemoryLimitMB,
      @Named("che.agents.auth_enabled") boolean isAuthEnabled) {
    this.defaultSidecarMemoryLimitBytes = String.valueOf(defaultSidecarMemoryLimitMB * 1024 * 1024);
    this.isAuthEnabled = isAuthEnabled;
    this.sidecarImagePullPolicy =
        validImagePullPolicies.contains(sidecarImagePullPolicy) ? sidecarImagePullPolicy : null;
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
    chePlugins.forEach(chePlugin -> populateWorkspaceEnvVars(chePlugin, kubernetesEnvironment));

    if (isAuthEnabled) {
      // enable per-workspace security with JWT proxy for sidecar based workspaces
      // because it is the only workspace security implementation supported for now
      kubernetesEnvironment.getAttributes().putIfAbsent(SECURE_EXPOSER_IMPL_PROPERTY, "jwtproxy");
    }
  }

  private void populateWorkspaceEnvVars(
      ChePlugin chePlugin, KubernetesEnvironment kubernetesEnvironment) {

    List<EnvVar> workspaceEnv = toK8sEnvVars(chePlugin.getWorkspaceEnv());
    kubernetesEnvironment
        .getPods()
        .values()
        .stream()
        .flatMap(pod -> pod.getSpec().getContainers().stream())
        .forEach(container -> container.getEnv().addAll(workspaceEnv));
  }

  private List<EnvVar> toK8sEnvVars(
      List<org.eclipse.che.api.workspace.server.wsplugins.model.EnvVar> workspaceEnv) {
    if (workspaceEnv == null) {
      return emptyList();
    }
    return workspaceEnv
        .stream()
        .map(e -> new EnvVar(e.getName(), e.getValue(), null))
        .collect(Collectors.toList());
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
            .setImagePullPolicy(sidecarImagePullPolicy)
            .setPluginName(chePlugin.getName())
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
