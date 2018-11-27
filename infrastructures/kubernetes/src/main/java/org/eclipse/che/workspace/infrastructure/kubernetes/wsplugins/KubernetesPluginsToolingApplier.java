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

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider.SECURE_EXPOSER_IMPL_PROPERTY;

import com.google.common.annotations.Beta;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Sets;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.ChePluginsApplier;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.Command;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.Warnings;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Applies Che plugins tooling configuration to a kubernetes internal runtime object.
 *
 * @author Oleksander Garagatyi
 * @author Sergii Leshchenko
 */
@Beta
@Singleton
public class KubernetesPluginsToolingApplier implements ChePluginsApplier {

  private static final Set<String> validImagePullPolicies =
      Sets.newHashSet("Always", "Never", "IfNotPresent");
  private static final String CHE_WORKSPACE_POD = "che-workspace-pod";

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
    switch (pods.size()) {
      case 0:
        addToolingPod(kubernetesEnvironment);
        break;
      case 1:
        break;
      default:
        throw new InfrastructureException(
            "Che plugins tooling configuration can be applied to a workspace with one pod only");
    }
    Pod pod = pods.values().iterator().next();

    CommandsResolver commandsResolver = new CommandsResolver(kubernetesEnvironment);
    for (ChePlugin chePlugin : chePlugins) {
      Collection<CommandImpl> pluginRelatedCommands = commandsResolver.resolve(chePlugin);

      for (CheContainer container : chePlugin.getContainers()) {
        addSidecar(pod, container, chePlugin, kubernetesEnvironment, pluginRelatedCommands);
      }
    }

    chePlugins.forEach(chePlugin -> populateWorkspaceEnvVars(chePlugin, kubernetesEnvironment));

    if (isAuthEnabled) {
      // enable per-workspace security with JWT proxy for sidecar based workspaces
      // because it is the only workspace security implementation supported for now
      kubernetesEnvironment.getAttributes().putIfAbsent(SECURE_EXPOSER_IMPL_PROPERTY, "jwtproxy");
    }
  }

  private void addToolingPod(KubernetesEnvironment kubernetesEnvironment) {
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(CHE_WORKSPACE_POD)
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();

    kubernetesEnvironment.getPods().put(CHE_WORKSPACE_POD, pod);
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
   * <li>Fill in machine name attribute in related commands
   *
   * @throws InfrastructureException when any error occurs
   */
  private void addSidecar(
      Pod pod,
      CheContainer container,
      ChePlugin chePlugin,
      KubernetesEnvironment kubernetesEnvironment,
      Collection<CommandImpl> sidecarRelatedCommands)
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

    sidecarRelatedCommands.forEach(c -> c.getAttributes().put("machineName", machineName));

    container
        .getCommands()
        .stream()
        .map(c -> asCommand(machineName, c))
        .forEach(c -> kubernetesEnvironment.getCommands().add(c));

    SidecarServicesProvisioner sidecarServicesProvisioner =
        new SidecarServicesProvisioner(containerEndpoints, pod.getMetadata().getName());
    sidecarServicesProvisioner.provision(kubernetesEnvironment);
  }

  private CommandImpl asCommand(String machineName, Command command) {
    CommandImpl cmd =
        new CommandImpl(
            command.getName(),
            command.getCommand().stream().collect(Collectors.joining(" ")),
            "custom");
    cmd.getAttributes().put("workDir", command.getWorkingDir());
    cmd.getAttributes().put("machineName", machineName);
    return cmd;
  }

  private static class CommandsResolver {
    private final KubernetesEnvironment k8sEnvironment;
    private final ArrayListMultimap<String, CommandImpl> pluginRefToCommand;

    public CommandsResolver(KubernetesEnvironment k8sEnvironment) {
      this.k8sEnvironment = k8sEnvironment;

      pluginRefToCommand = ArrayListMultimap.create();
      k8sEnvironment
          .getCommands()
          .forEach(
              (c) -> {
                String pluginRef = c.getAttributes().get("plugin");
                if (pluginRef != null) {
                  pluginRefToCommand.put(pluginRef, c);
                }
              });
    }

    private Collection<CommandImpl> resolve(ChePlugin chePlugin) {
      List<CheContainer> containers = chePlugin.getContainers();

      String pluginRef = chePlugin.getId() + ":" + chePlugin.getVersion();
      Collection<CommandImpl> pluginsCommands = pluginRefToCommand.removeAll(pluginRef);

      if (pluginsCommands.isEmpty()) {
        // specified plugin doesn't have configured commands
        return emptyList();
      }

      if (containers.isEmpty()) {
        k8sEnvironment
            .getWarnings()
            .add(
                new WarningImpl(
                    Warnings.COMMAND_IS_CONFIGURED_IN_PLUGIN_WITHOUT_CONTAINERS_WARNING_CODE,
                    format(
                        Warnings
                            .COMMAND_IS_CONFIGURED_IN_PLUGIN_WITHOUT_CONTAINERS_WARNING_MESSAGE_FMT,
                        pluginRef)));
        return emptyList();
      }

      if (containers.size() > 1) {
        k8sEnvironment
            .getWarnings()
            .add(
                new WarningImpl(
                    Warnings.COMMAND_IS_CONFIGURED_IN_PLUGIN_WITH_MULTIPLY_CONTAINERS_WARNING_CODE,
                    format(
                        Warnings
                            .COMMAND_IS_CONFIGURED_IN_PLUGIN_WITH_MULTIPLY_CONTAINERS_WARNING_MESSAGE_FMT,
                        pluginRef)));
      }

      return pluginsCommands;
    }
  }
}
