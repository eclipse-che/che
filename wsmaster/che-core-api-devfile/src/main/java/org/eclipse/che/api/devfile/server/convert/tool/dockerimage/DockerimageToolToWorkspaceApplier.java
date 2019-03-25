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
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CONTAINER_ARGS_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CONTAINER_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_TOOL_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.devfile.model.Endpoint;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.Constants;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.tool.ToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPointParser;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSize;

/**
 * Applies changes on workspace config according to the specified dockerimage tool.
 *
 * @author Sergii Leshchenko
 */
public class DockerimageToolToWorkspaceApplier implements ToolToWorkspaceApplier {

  private final String projectFolderPath;
  private final EntryPointParser entryPointParser;

  @Inject
  public DockerimageToolToWorkspaceApplier(
      @Named("che.workspace.projects.storage") String projectFolderPath,
      EntryPointParser entryPointParser) {
    this.projectFolderPath = projectFolderPath;
    this.entryPointParser = entryPointParser;
  }

  /**
   * Applies changes on workspace config according to the specified dockerimage tool.
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
    if (workspaceConfig.getDefaultEnv() != null) {
      throw new DevfileException("Workspace already contains environment");
    }

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

    machineConfig
        .getAttributes()
        .put(
            MEMORY_LIMIT_ATTRIBUTE,
            Long.toString(KubernetesSize.toBytes(dockerimageTool.getMemoryLimit())));

    setEntryPointAttribute(
        machineConfig, CONTAINER_COMMAND_ATTRIBUTE, dockerimageTool.getCommand());
    setEntryPointAttribute(machineConfig, CONTAINER_ARGS_ATTRIBUTE, dockerimageTool.getArgs());

    RecipeImpl recipe =
        new RecipeImpl(DockerImageEnvironment.TYPE, null, dockerimageTool.getImage(), null);
    EnvironmentImpl environment =
        new EnvironmentImpl(recipe, ImmutableMap.of(machineName, machineConfig));
    workspaceConfig.getEnvironments().put(dockerimageTool.getName(), environment);
    workspaceConfig.setDefaultEnv(dockerimageTool.getName());

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

  private void setEntryPointAttribute(
      MachineConfigImpl machineConfig, String attributeName, List<String> attributeValue) {

    if (attributeValue == null) {
      return;
    }

    String val = entryPointParser.serializeEntry(attributeValue);

    machineConfig.getAttributes().put(attributeName, val);
  }

  private ServerConfigImpl toServerConfig(Endpoint endpoint) {
    HashMap<String, String> attributes = new HashMap<>(endpoint.getAttributes());

    String protocol = attributes.remove("protocol");
    if (isNullOrEmpty(protocol)) {
      protocol = "http";
    }

    String path = attributes.remove("path");

    String isPublic = attributes.remove("public");
    if ("false".equals(isPublic)) {
      attributes.put(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, "true");
    }

    return new ServerConfigImpl(Integer.toString(endpoint.getPort()), protocol, path, attributes);
  }
}
