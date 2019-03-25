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
package org.eclipse.che.api.devfile.server.convert.component.dockerimage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CONTAINER_ARGS_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CONTAINER_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.devfile.model.Component;
import org.eclipse.che.api.devfile.model.Endpoint;
import org.eclipse.che.api.devfile.server.Constants;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.component.ComponentToWorkspaceApplier;
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
 * Applies changes on workspace config according to the specified dockerimage component.
 *
 * @author Sergii Leshchenko
 */
public class DockerimageComponentToWorkspaceApplier implements ComponentToWorkspaceApplier {

  private final String projectFolderPath;
  private final EntryPointParser entryPointParser;

  @Inject
  public DockerimageComponentToWorkspaceApplier(
      @Named("che.workspace.projects.storage") String projectFolderPath,
      EntryPointParser entryPointParser) {
    this.projectFolderPath = projectFolderPath;
    this.entryPointParser = entryPointParser;
  }

  /**
   * Applies changes on workspace config according to the specified dockerimage component.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param dockerimageComponent dockerimage component that should be applied
   * @param contentProvider optional content provider that may be used for external component
   *     resource fetching
   * @throws DevfileException if specified workspace config already has default environment where
   *     dockerimage component should be stored
   * @throws IllegalArgumentException if specified workspace config or plugin component is null
   * @throws IllegalArgumentException if specified component has type different from dockerimage
   */
  @Override
  public void apply(
      WorkspaceConfigImpl workspaceConfig,
      Component dockerimageComponent,
      FileContentProvider contentProvider)
      throws DevfileException {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(dockerimageComponent != null, "Component must not be null");
    checkArgument(
        DOCKERIMAGE_COMPONENT_TYPE.equals(dockerimageComponent.getType()),
        format("Plugin must have `%s` type", DOCKERIMAGE_COMPONENT_TYPE));
    if (workspaceConfig.getDefaultEnv() != null) {
      throw new DevfileException("Workspace already contains environment");
    }

    String machineName = dockerimageComponent.getName();
    MachineConfigImpl machineConfig = new MachineConfigImpl();

    dockerimageComponent.getEnv().forEach(e -> machineConfig.getEnv().put(e.getName(), e.getValue()));

    for (Endpoint endpoint : dockerimageComponent.getEndpoints()) {
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

    dockerimageComponent
        .getVolumes()
        .forEach(
            v ->
                machineConfig
                    .getVolumes()
                    .put(v.getName(), new VolumeImpl().withPath(v.getContainerPath())));

    if (dockerimageComponent.getMountSources()) {
      machineConfig
          .getVolumes()
          .put(PROJECTS_VOLUME_NAME, new VolumeImpl().withPath(projectFolderPath));
    }

    machineConfig
        .getAttributes()
        .put(
            MEMORY_LIMIT_ATTRIBUTE,
            Long.toString(KubernetesSize.toBytes(dockerimageComponent.getMemoryLimit())));

    setEntryPointAttribute(
        machineConfig, CONTAINER_COMMAND_ATTRIBUTE, dockerimageComponent.getCommand());
    setEntryPointAttribute(machineConfig, CONTAINER_ARGS_ATTRIBUTE, dockerimageComponent.getArgs());

    RecipeImpl recipe =
        new RecipeImpl(DockerImageEnvironment.TYPE, null, dockerimageComponent.getImage(), null);
    EnvironmentImpl environment =
        new EnvironmentImpl(recipe, ImmutableMap.of(machineName, machineConfig));
    workspaceConfig.getEnvironments().put(dockerimageComponent.getName(), environment);
    workspaceConfig.setDefaultEnv(dockerimageComponent.getName());

    workspaceConfig
        .getCommands()
        .stream()
        .filter(
            c ->
                dockerimageComponent
                    .getName()
                    .equals(c.getAttributes().get(Constants.COMPONENT_NAME_COMMAND_ATTRIBUTE)))
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
}
