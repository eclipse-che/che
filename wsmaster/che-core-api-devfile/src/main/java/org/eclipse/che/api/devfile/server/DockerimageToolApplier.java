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
package org.eclipse.che.api.devfile.server;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_TOOL_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Endpoint;
import org.eclipse.che.api.devfile.model.Env;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.model.Volume;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSize;

/**
 * Applies Kubernetes tool configuration on provided {@link Devfile} and {@link
 * WorkspaceConfigImpl}.
 *
 * @author Sergii Leshchenko
 */
public class DockerimageToolApplier {

  private static final String DOCKERIMAGE_RECIPE_TYPE = "dockerimage";

  private final String projectFolderPath;

  @Inject
  public DockerimageToolApplier(@Named("che.workspace.projects.storage") String projectFolderPath) {
    this.projectFolderPath = projectFolderPath;
  }

  /**
   * Applies Kubernetes tool configuration on provided {@link Devfile} and {@link
   * WorkspaceConfigImpl}.
   *
   * <p>It includes:
   *
   * <ul>
   *   <li>provisioning environment based on specified configuration in tool: dockerimage, env,
   *       volumes, etc.;
   *   <li>provisioning machine name attribute to commands that are configured to be run in the
   *       specified tool;
   * </ul>
   *
   * @param tool dockerimage tool that should be applied to devfile and workspace config
   * @param devfile devfile that should be changed according to the provided tool
   * @param workspaceConfig workspace config that should be changed according to the provided tool
   * @throws IllegalArgumentException when wrong type tool is passed
   */
  public void apply(Tool tool, Devfile devfile, WorkspaceConfigImpl workspaceConfig) {
    checkArgument(tool != null, "Tool must not be null");
    checkArgument(
        tool.getType().equals(DOCKERIMAGE_TOOL_TYPE), "The tool must have `dockerimage` type");

    String machineName = tool.getName();
    MachineConfigImpl machineConfig = new MachineConfigImpl();

    tool.getEnv().forEach(e -> machineConfig.getEnv().put(e.getName(), e.getValue()));

    for (Endpoint endpoint : tool.getEndpoints()) {
      HashMap<String, String> attributes = new HashMap<>(endpoint.getAttributes());

      String protocol = attributes.remove("protocol");
      if (isNullOrEmpty(protocol)) {
        protocol = "http";
      }

      String path = attributes.remove("path");

      machineConfig
          .getServers()
          .put(
              endpoint.getName(),
              new ServerConfigImpl(
                  Integer.toString(endpoint.getPort()), protocol, path, attributes));
    }

    tool.getVolumes()
        .forEach(
            v ->
                machineConfig
                    .getVolumes()
                    .put(v.getName(), new VolumeImpl().withPath(v.getContainerPath())));

    if (tool.getMountSources()) {
      machineConfig
          .getVolumes()
          .put(PROJECTS_VOLUME_NAME, new VolumeImpl().withPath(projectFolderPath));
    }

    machineConfig
        .getAttributes()
        .put(MEMORY_LIMIT_ATTRIBUTE, Long.toString(KubernetesSize.toBytes(tool.getMemoryLimit())));

    RecipeImpl recipe = new RecipeImpl(DOCKERIMAGE_RECIPE_TYPE, null, tool.getImage(), null);
    EnvironmentImpl environment =
        new EnvironmentImpl(recipe, ImmutableMap.of(machineName, machineConfig));
    workspaceConfig.getEnvironments().put(tool.getName(), environment);
    workspaceConfig.setDefaultEnv(tool.getName());

    devfile
        .getCommands()
        .stream()
        .filter(command -> command.getActions().get(0).getTool().equals(tool.getName()))
        .forEach(c -> c.getAttributes().put(MACHINE_NAME_ATTRIBUTE, machineName));
  }

  /**
   * Creates dockerimage tool from the specified environment.
   *
   * @param environmentName the name of environment. Will be used for tool name
   * @param environment environment that contains configuration which will be used for tool
   * @return created dockerimage tool
   * @throws IllegalArgumentException if the specified environment or name are null
   * @throws IllegalArgumentException if the specified environment does not have recipe
   * @throws IllegalArgumentException if the specified environment has non dockerimage recipe
   * @throws WorkspaceExportException when the specified environment can not be converted to
   *     dockerimage tool
   */
  public Tool from(String environmentName, EnvironmentImpl environment)
      throws WorkspaceExportException {
    checkArgument(environment != null, "The environment must not be null");
    checkArgument(environmentName != null, "The environment name must not be null");
    checkArgument(environment.getRecipe() != null, "The environment recipe must not be null");
    checkArgument(
        environment.getRecipe().getType().equals(DOCKERIMAGE_RECIPE_TYPE),
        "The environment recipe must have recipe with 'dockerimage' tool");

    RecipeImpl recipe = environment.getRecipe();
    Tool dockerimageTool = new Tool();
    dockerimageTool.setName(environmentName);

    dockerimageTool.setImage(recipe.getContent());
    dockerimageTool.setType(DOCKERIMAGE_TOOL_TYPE);

    if (environment.getMachines().isEmpty()) {
      // environment does not have additional configuration
      return dockerimageTool;
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

    machineConfig
        .getEnv()
        .entrySet()
        .stream()
        .map(e -> new Env().withName(e.getKey()).withValue(e.getValue()))
        .forEach(e -> dockerimageTool.getEnv().add(e));

    return dockerimageTool;
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
