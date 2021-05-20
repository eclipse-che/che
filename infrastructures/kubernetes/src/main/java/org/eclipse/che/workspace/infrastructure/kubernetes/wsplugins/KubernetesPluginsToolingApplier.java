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

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.Command.WORKING_DIRECTORY_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.CONTAINER_SOURCE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PLUGIN_MACHINE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.TOOL_CONTAINER_SOURCE;

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
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.provision.env.ProjectsRootEnvVariableProvider;
import org.eclipse.che.api.workspace.server.wsplugins.ChePluginsApplier;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.Command;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.Warnings;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.EnvVars;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSize;

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
  private final String defaultSidecarMemoryRequestBytes;
  private final String sidecarImagePullPolicy;
  private final String defaultSidecarCpuLimitCores;
  private final String defaultSidecarCpuRequestCores;
  private final boolean isAuthEnabled;
  private final ProjectsRootEnvVariableProvider projectsRootEnvVariableProvider;
  private final ChePluginsVolumeApplier chePluginsVolumeApplier;
  private final EnvVars envVars;

  @Inject
  public KubernetesPluginsToolingApplier(
      @Named("che.workspace.sidecar.image_pull_policy") String sidecarImagePullPolicy,
      @Named("che.workspace.sidecar.default_memory_limit_mb") long defaultSidecarMemoryLimitMB,
      @Named("che.workspace.sidecar.default_memory_request_mb") long defaultSidecarMemoryRequestMB,
      @Named("che.workspace.sidecar.default_cpu_limit_cores") String defaultSidecarCpuLimitCores,
      @Named("che.workspace.sidecar.default_cpu_request_cores")
          String defaultSidecarCpuRequestCores,
      @Named("che.agents.auth_enabled") boolean isAuthEnabled,
      ProjectsRootEnvVariableProvider projectsRootEnvVariableProvider,
      ChePluginsVolumeApplier chePluginsVolumeApplier,
      EnvVars envVars) {
    this.defaultSidecarMemoryLimitBytes = toBytesString(defaultSidecarMemoryLimitMB);
    this.defaultSidecarMemoryRequestBytes = toBytesString(defaultSidecarMemoryRequestMB);
    this.defaultSidecarCpuLimitCores =
        Float.toString(KubernetesSize.toCores(defaultSidecarCpuLimitCores));
    this.defaultSidecarCpuRequestCores =
        Float.toString(KubernetesSize.toCores(defaultSidecarCpuRequestCores));
    this.isAuthEnabled = isAuthEnabled;
    this.sidecarImagePullPolicy =
        validImagePullPolicies.contains(sidecarImagePullPolicy) ? sidecarImagePullPolicy : null;
    this.projectsRootEnvVariableProvider = projectsRootEnvVariableProvider;
    this.chePluginsVolumeApplier = chePluginsVolumeApplier;
    this.envVars = envVars;
  }

  @Override
  public void apply(
      RuntimeIdentity runtimeIdentity,
      InternalEnvironment internalEnvironment,
      Collection<ChePlugin> chePlugins)
      throws InfrastructureException {
    if (chePlugins.isEmpty()) {
      return;
    }

    KubernetesEnvironment k8sEnv = (KubernetesEnvironment) internalEnvironment;

    Map<String, PodData> pods = k8sEnv.getPodsData();
    switch (pods.size()) {
      case 0:
        addToolingPod(k8sEnv);
        pods = k8sEnv.getPodsData();
        break;
      case 1:
        break;
      default:
        throw new InfrastructureException(
            "Che plugins tooling configuration can be applied to a workspace with one pod only");
    }
    PodData pod = pods.values().iterator().next();

    CommandsResolver commandsResolver = new CommandsResolver(k8sEnv);
    for (ChePlugin chePlugin : chePlugins) {
      Map<String, ComponentImpl> devfilePlugins =
          k8sEnv
              .getDevfile()
              .getComponents()
              .stream()
              .filter(c -> c.getType().equals("cheEditor") || c.getType().equals("chePlugin"))
              .collect(Collectors.toMap(ComponentImpl::getId, Function.identity()));
      if (!devfilePlugins.containsKey(chePlugin.getId())) {
        throw new InfrastructureException(
            String.format(
                "The downloaded plugin '%s' configuration does not have the "
                    + "corresponding component in devfile. Devfile contains the following cheEditor/chePlugins: %s",
                chePlugin.getId(), devfilePlugins.keySet()));
      }
      ComponentImpl pluginRelatedComponent = devfilePlugins.get(chePlugin.getId());

      for (CheContainer container : chePlugin.getInitContainers()) {
        Container k8sInitContainer = toK8sContainer(container);
        envVars.apply(k8sInitContainer, pluginRelatedComponent.getEnv());
        chePluginsVolumeApplier.applyVolumes(pod, k8sInitContainer, container.getVolumes(), k8sEnv);
        pod.getSpec().getInitContainers().add(k8sInitContainer);
      }

      Collection<CommandImpl> pluginRelatedCommands = commandsResolver.resolve(chePlugin);

      for (CheContainer container : chePlugin.getContainers()) {
        addSidecar(
            pod,
            container,
            chePlugin,
            k8sEnv,
            pluginRelatedCommands,
            pluginRelatedComponent,
            runtimeIdentity);
      }
    }

    chePlugins.forEach(chePlugin -> populateWorkspaceEnvVars(chePlugin, k8sEnv));
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

    kubernetesEnvironment.addPod(pod);
  }

  private void populateWorkspaceEnvVars(
      ChePlugin chePlugin, KubernetesEnvironment kubernetesEnvironment) {

    List<EnvVar> workspaceEnv = toK8sEnvVars(chePlugin.getWorkspaceEnv());
    kubernetesEnvironment
        .getPodsData()
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

  private Container toK8sContainer(CheContainer container) throws InfrastructureException {
    return toK8sContainerResolver(container, emptyList()).resolve();
  }

  private K8sContainerResolver toK8sContainerResolver(
      CheContainer container, List<ChePluginEndpoint> endpoints) {
    return new K8sContainerResolverBuilder()
        .setContainer(container)
        .setImagePullPolicy(sidecarImagePullPolicy)
        .setPluginEndpoints(endpoints)
        .build();
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
      PodData pod,
      CheContainer container,
      ChePlugin chePlugin,
      KubernetesEnvironment k8sEnv,
      Collection<CommandImpl> sidecarRelatedCommands,
      Component pluginRelatedComponent,
      RuntimeIdentity runtimeIdentity)
      throws InfrastructureException {

    K8sContainerResolver k8sContainerResolver =
        toK8sContainerResolver(container, chePlugin.getEndpoints());
    List<ChePluginEndpoint> containerEndpoints = k8sContainerResolver.getEndpoints();

    Container k8sContainer = k8sContainerResolver.resolve();
    envVars.apply(k8sContainer, pluginRelatedComponent.getEnv());
    chePluginsVolumeApplier.applyVolumes(pod, k8sContainer, container.getVolumes(), k8sEnv);

    String machineName = k8sContainer.getName();
    Names.putMachineName(pod.getMetadata(), k8sContainer.getName(), machineName);
    pod.getSpec().getContainers().add(k8sContainer);

    MachineResolver machineResolver =
        new MachineResolverBuilder()
            .setCheContainer(container)
            .setContainer(k8sContainer)
            .setContainerEndpoints(containerEndpoints)
            .setDefaultSidecarMemoryLimitAttribute(defaultSidecarMemoryLimitBytes)
            .setDefaultSidecarMemoryRequestAttribute(defaultSidecarMemoryRequestBytes)
            .setDefaultSidecarCpuLimitAttribute(defaultSidecarCpuLimitCores)
            .setDefaultSidecarCpuRequestAttribute(defaultSidecarCpuRequestCores)
            .setProjectsRootPathEnvVar(projectsRootEnvVariableProvider.get(runtimeIdentity))
            .setComponent(pluginRelatedComponent)
            .build();

    InternalMachineConfig machineConfig = machineResolver.resolve();
    machineConfig.getAttributes().put(CONTAINER_SOURCE_ATTRIBUTE, TOOL_CONTAINER_SOURCE);
    machineConfig.getAttributes().put(PLUGIN_MACHINE_ATTRIBUTE, chePlugin.getId());
    k8sEnv.getMachines().put(machineName, machineConfig);

    sidecarRelatedCommands.forEach(
        c ->
            c.getAttributes()
                .put(
                    org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE,
                    machineName));

    container
        .getCommands()
        .stream()
        .map(c -> asCommand(machineName, c))
        .forEach(c -> k8sEnv.getCommands().add(c));

    SidecarServicesProvisioner sidecarServicesProvisioner =
        new SidecarServicesProvisioner(containerEndpoints, pod.getMetadata().getName());
    sidecarServicesProvisioner.provision(k8sEnv);
  }

  private CommandImpl asCommand(String machineName, Command command) {
    CommandImpl cmd =
        new CommandImpl(command.getName(), String.join(" ", command.getCommand()), "custom");
    cmd.getAttributes().put(WORKING_DIRECTORY_ATTRIBUTE, command.getWorkingDir());
    cmd.getAttributes().put(MACHINE_NAME_ATTRIBUTE, machineName);
    return cmd;
  }

  private String toBytesString(long memoryLimitMB) {
    return String.valueOf(memoryLimitMB * 1024L * 1024L);
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
                String pluginRef =
                    c.getAttributes()
                        .get(
                            org.eclipse.che.api.core.model.workspace.config.Command
                                .PLUGIN_ATTRIBUTE);
                if (pluginRef != null) {
                  pluginRefToCommand.put(pluginRef, c);
                }
              });
    }

    private Collection<CommandImpl> resolve(ChePlugin chePlugin) {
      List<CheContainer> containers = chePlugin.getContainers();

      String pluginRef = chePlugin.getId();
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
