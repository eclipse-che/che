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
package org.eclipse.che.api.devfile.server.convert.tool.dockerimage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.PUBLIC_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.MACHINE_NAME_ANNOTATION_FMT;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.devfile.model.Endpoint;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.Constants;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.tool.ToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.tool.kubernetes.KubernetesEnvironmentProvisioner;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;

/**
 * Applies changes on workspace config according to the specified dockerimage tool.
 *
 * @author Sergii Leshchenko
 */
public class DockerimageToolToWorkspaceApplier implements ToolToWorkspaceApplier {

  private final String projectFolderPath;
  private final KubernetesEnvironmentProvisioner k8sEnvProvisioner;

  @Inject
  public DockerimageToolToWorkspaceApplier(
      @Named("che.workspace.projects.storage") String projectFolderPath,
      KubernetesEnvironmentProvisioner k8sEnvProvisioner) {
    this.projectFolderPath = projectFolderPath;
    this.k8sEnvProvisioner = k8sEnvProvisioner;
  }

  /**
   * Applies changes on workspace config according to the specified dockerimage tool.
   *
   * <p>Dockerimage tool is provisioned as Deployment in Kubernetes recipe.<br>
   * Generated deployment contains container with environment variables, memory limit, docker image,
   * arguments and commands specified in tool.<br>
   * Also, environment is provisioned with machine config with volumes and servers specified, then
   * Kubernetes infra will created needed PVC, Services, Ingresses, Routes according to specified
   * configuration.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param dockerimageTool dockerimage tool that should be applied
   * @param contentProvider optional content provider that may be used for external tool resource
   *     fetching
   * @throws DevfileException if specified workspace config already has default environment where
   *     dockerimage tool should be stored
   * @throws IllegalArgumentException if specified workspace config or plugin tool is null
   * @throws IllegalArgumentException if specified tool has type different from dockerimage
   */
  @Override
  public void apply(
      WorkspaceConfigImpl workspaceConfig,
      Tool dockerimageTool,
      FileContentProvider contentProvider)
      throws DevfileException {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(dockerimageTool != null, "Tool must not be null");
    checkArgument(
        DOCKERIMAGE_TOOL_TYPE.equals(dockerimageTool.getType()),
        format("Plugin must have `%s` type", DOCKERIMAGE_TOOL_TYPE));

    String machineName = dockerimageTool.getName();

    MachineConfigImpl machineConfig = new MachineConfigImpl();
    dockerimageTool
        .getEndpoints()
        .forEach(e -> machineConfig.getServers().put(e.getName(), toServerConfig(e)));

    dockerimageTool
        .getVolumes()
        .forEach(
            v ->
                machineConfig
                    .getVolumes()
                    .put(v.getName(), new VolumeImpl().withPath(v.getContainerPath())));

    if (dockerimageTool.getMountSources()) {
      machineConfig
          .getVolumes()
          .put(PROJECTS_VOLUME_NAME, new VolumeImpl().withPath(projectFolderPath));
    }

    Deployment deployment =
        buildDeployment(
            machineName,
            dockerimageTool.getImage(),
            dockerimageTool.getMemoryLimit(),
            dockerimageTool
                .getEnv()
                .stream()
                .map(e -> new EnvVar(e.getName(), e.getValue(), null))
                .collect(Collectors.toCollection(ArrayList::new)),
            dockerimageTool.getCommand(),
            dockerimageTool.getArgs());

    k8sEnvProvisioner.provision(
        workspaceConfig,
        KubernetesEnvironment.TYPE,
        singletonList(deployment),
        ImmutableMap.of(machineName, machineConfig));

    workspaceConfig
        .getCommands()
        .stream()
        .filter(
            c ->
                dockerimageTool
                    .getName()
                    .equals(c.getAttributes().get(Constants.TOOL_NAME_COMMAND_ATTRIBUTE)))
        .forEach(c -> c.getAttributes().put(MACHINE_NAME_ATTRIBUTE, machineName));
  }

  private Deployment buildDeployment(
      String name,
      String image,
      String memoryLimit,
      List<EnvVar> env,
      List<String> command,
      List<String> args) {
    Container container =
        new ContainerBuilder()
            .withImage(image)
            .withName(name)
            .withEnv(env)
            .withCommand(command)
            .withArgs(args)
            .build();

    Containers.addRamLimit(container, memoryLimit);
    return new DeploymentBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withNewTemplate()
        .withNewMetadata()
        .withName(name)
        .addToLabels(CHE_ORIGINAL_NAME_LABEL, name)
        .addToAnnotations(String.format(MACHINE_NAME_ANNOTATION_FMT, name), name)
        .endMetadata()
        .withNewSpec()
        .withContainers(container)
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();
  }

  private ServerConfigImpl toServerConfig(Endpoint endpoint) {
    HashMap<String, String> attributes = new HashMap<>(endpoint.getAttributes());

    String protocol = attributes.remove("protocol");
    if (isNullOrEmpty(protocol)) {
      protocol = "http";
    }

    String path = attributes.remove("path");

    String isPublic = attributes.remove(PUBLIC_ENDPOINT_ATTRIBUTE);
    if ("false".equals(isPublic)) {
      attributes.put(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, "true");
    }

    return new ServerConfigImpl(Integer.toString(endpoint.getPort()), protocol, path, attributes);
  }
}
