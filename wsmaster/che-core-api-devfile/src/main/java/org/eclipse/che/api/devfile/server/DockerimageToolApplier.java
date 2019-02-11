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
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Endpoint;
import org.eclipse.che.api.devfile.model.Tool;
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
}
