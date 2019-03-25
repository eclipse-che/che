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
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.PUBLIC_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Endpoint;
import org.eclipse.che.api.devfile.model.Env;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.model.Volume;
import org.eclipse.che.api.devfile.server.convert.tool.ToolProvisioner;
import org.eclipse.che.api.devfile.server.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPoint;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPointParser;

/**
 * Provision dockerimage tool in {@link Devfile} according to the value of environment with
 * dockerimage recipe if the specified {@link WorkspaceConfigImpl} has such.
 *
 * @author Sergii Leshchenko
 */
public class DockerimageToolProvisioner implements ToolProvisioner {

  private final EntryPointParser entryPointParser;

  @Inject
  public DockerimageToolProvisioner(EntryPointParser entryPointParser) {
    this.entryPointParser = entryPointParser;
  }

  /**
   * Provision dockerimage tool in {@link Devfile} according to the value of environment with
   * dockerimage recipe if the specified {@link WorkspaceConfigImpl} has such.
   *
   * @param devfile devfile to which created dockerimage tool should be injected
   * @param workspaceConfig workspace config that may contain environment with dockerimage recipe to
   *     convert
   * @throws IllegalArgumentException if the specified workspace config or devfile is null
   * @throws WorkspaceExportException if workspace config has more than one dockerimage environments
   */
  @Override
  public void provision(Devfile devfile, WorkspaceConfigImpl workspaceConfig)
      throws WorkspaceExportException {
    checkArgument(devfile != null, "The environment must not be null");
    checkArgument(workspaceConfig != null, "The workspace config must not be null");

    List<Entry<String, EnvironmentImpl>> dockerimageEnvironments =
        workspaceConfig
            .getEnvironments()
            .entrySet()
            .stream()
            .filter(e -> DockerImageEnvironment.TYPE.equals(e.getValue().getRecipe().getType()))
            .collect(Collectors.toList());

    if (dockerimageEnvironments.isEmpty()) {
      return;
    }

    if (dockerimageEnvironments.size() > 1) {
      throw new WorkspaceExportException(
          "Workspace with multiple `dockerimage` environments can not be converted to devfile");
    }

    Entry<String, EnvironmentImpl> dockerimageEnvEntry = dockerimageEnvironments.get(0);
    String environmentName = dockerimageEnvEntry.getKey();
    EnvironmentImpl environment = dockerimageEnvEntry.getValue();

    RecipeImpl recipe = environment.getRecipe();
    Tool dockerimageTool = new Tool();
    dockerimageTool.setName(environmentName);

    dockerimageTool.setImage(recipe.getContent());
    dockerimageTool.setType(DOCKERIMAGE_TOOL_TYPE);

    if (environment.getMachines().isEmpty()) {
      // environment does not have additional configuration
      devfile.getTools().add(dockerimageTool);
      return;
    }

    if (environment.getMachines().size() > 1) {
      throw new WorkspaceExportException(
          "Environment with 'dockerimage' recipe must contain only one machine configuration");
    }

    MachineConfigImpl machineConfig = environment.getMachines().values().iterator().next();

    for (Entry<String, ServerConfigImpl> serverEntry : machineConfig.getServers().entrySet()) {
      dockerimageTool.getEndpoints().add(toEndpoint(serverEntry.getKey(), serverEntry.getValue()));
    }

    for (Entry<String, VolumeImpl> volumeEntry : machineConfig.getVolumes().entrySet()) {
      if (volumeEntry.getKey().equals(PROJECTS_VOLUME_NAME)) {
        dockerimageTool.setMountSources(true);
        continue;
      }

      dockerimageTool
          .getVolumes()
          .add(toDevfileVolume(volumeEntry.getKey(), volumeEntry.getValue()));
    }

    dockerimageTool.setMemoryLimit(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE));

    EntryPoint ep = toEntryPoint(machineConfig);
    dockerimageTool.setCommand(ep.getCommand());
    dockerimageTool.setArgs(ep.getArguments());

    machineConfig
        .getEnv()
        .entrySet()
        .stream()
        .map(e -> new Env().withName(e.getKey()).withValue(e.getValue()))
        .forEach(e -> dockerimageTool.getEnv().add(e));

    devfile.getTools().add(dockerimageTool);
  }

  private EntryPoint toEntryPoint(MachineConfig machineConfig) throws WorkspaceExportException {
    try {
      return entryPointParser.parse(machineConfig.getAttributes());
    } catch (InfrastructureException e) {
      throw new WorkspaceExportException(e.getMessage());
    }
  }

  private Volume toDevfileVolume(String name, VolumeImpl volume) {
    return new Volume().withName(name).withContainerPath(volume.getPath());
  }

  private Endpoint toEndpoint(String name, ServerConfigImpl config) {
    String stringPort =
        config.getPort().split("/")[0]; // cut protocol from string port like 8080/TCP

    Map<String, String> attributes = new HashMap<>(config.getAttributes());
    putIfNotNull(attributes, "protocol", config.getProtocol());
    putIfNotNull(attributes, "path", config.getPath());

    String isInternal = config.getAttributes().remove(ServerConfig.INTERNAL_SERVER_ATTRIBUTE);
    if ("true".equals(isInternal)) {
      attributes.put(PUBLIC_ENDPOINT_ATTRIBUTE, "false");
    }

    return new Endpoint()
        .withName(name)
        .withPort(Integer.parseInt(stringPort))
        .withAttributes(attributes);
  }

  private void putIfNotNull(Map<String, String> attributes, String key, String value) {
    if (value != null) {
      attributes.put(key, value);
    }
  }
}
