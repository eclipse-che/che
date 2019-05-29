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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static com.google.common.base.Preconditions.checkArgument;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PUBLIC_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentProvisioner;
import org.eclipse.che.api.workspace.server.devfile.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPoint;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPointParser;

/**
 * Provision dockerimage component in {@link DevfileImpl} according to the value of environment with
 * dockerimage recipe if the specified {@link WorkspaceConfigImpl} has such.
 *
 * <p>The {@code dockerimage} devfile components are handled as Kubernetes deployments internally.
 *
 * @author Sergii Leshchenko
 */
public class DockerimageComponentProvisioner implements ComponentProvisioner {

  private final EntryPointParser entryPointParser;

  @Inject
  public DockerimageComponentProvisioner(EntryPointParser entryPointParser) {
    this.entryPointParser = entryPointParser;
  }

  /**
   * Provision dockerimage component in {@link DevfileImpl} according to the value of environment
   * with dockerimage recipe if the specified {@link WorkspaceConfigImpl} has such.
   *
   * @param devfile devfile to which created dockerimage component should be injected
   * @param workspaceConfig workspace config that may contain environment with dockerimage recipe to
   *     convert
   * @throws IllegalArgumentException if the specified workspace config or devfile is null
   * @throws WorkspaceExportException if workspace config has more than one dockerimage environments
   */
  @Override
  public void provision(DevfileImpl devfile, WorkspaceConfigImpl workspaceConfig)
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
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias(environmentName);

    dockerimageComponent.setImage(recipe.getContent());
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);

    if (environment.getMachines().isEmpty()) {
      // environment does not have additional configuration
      devfile.getComponents().add(dockerimageComponent);
      return;
    }

    if (environment.getMachines().size() > 1) {
      throw new WorkspaceExportException(
          "Environment with 'dockerimage' recipe must contain only one machine configuration");
    }

    MachineConfigImpl machineConfig = environment.getMachines().values().iterator().next();

    for (Entry<String, ServerConfigImpl> serverEntry : machineConfig.getServers().entrySet()) {
      dockerimageComponent
          .getEndpoints()
          .add(toEndpoint(serverEntry.getKey(), serverEntry.getValue()));
    }

    for (Entry<String, org.eclipse.che.api.workspace.server.model.impl.VolumeImpl> volumeEntry :
        machineConfig.getVolumes().entrySet()) {
      if (volumeEntry.getKey().equals(PROJECTS_VOLUME_NAME)) {
        dockerimageComponent.setMountSources(true);
        continue;
      }

      dockerimageComponent
          .getVolumes()
          .add(toDevfileVolume(volumeEntry.getKey(), volumeEntry.getValue()));
    }

    dockerimageComponent.setMemoryLimit(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE));

    EntryPoint ep = toEntryPoint(machineConfig);
    dockerimageComponent.setCommand(ep.getCommand());
    dockerimageComponent.setArgs(ep.getArguments());

    machineConfig
        .getEnv()
        .entrySet()
        .stream()
        .map(e -> new EnvImpl(e.getKey(), e.getValue()))
        .forEach(e -> dockerimageComponent.getEnv().add(e));

    devfile.getComponents().add(dockerimageComponent);
  }

  private EntryPoint toEntryPoint(MachineConfig machineConfig) throws WorkspaceExportException {
    try {
      return entryPointParser.parse(machineConfig.getAttributes());
    } catch (InfrastructureException e) {
      throw new WorkspaceExportException(e.getMessage());
    }
  }

  private VolumeImpl toDevfileVolume(
      String name, org.eclipse.che.api.workspace.server.model.impl.VolumeImpl volume) {
    return new VolumeImpl(name, volume.getPath());
  }

  private EndpointImpl toEndpoint(String name, ServerConfigImpl config) {
    String stringPort =
        config.getPort().split("/")[0]; // cut protocol from string port like 8080/TCP

    Map<String, String> attributes = new HashMap<>(config.getAttributes());
    putIfNotNull(attributes, "protocol", config.getProtocol());
    putIfNotNull(attributes, "path", config.getPath());

    String isInternal = config.getAttributes().remove(ServerConfig.INTERNAL_SERVER_ATTRIBUTE);
    if ("true".equals(isInternal)) {
      attributes.put(PUBLIC_ENDPOINT_ATTRIBUTE, "false");
    }

    return new EndpointImpl(name, Integer.parseInt(stringPort), attributes);
  }

  private void putIfNotNull(Map<String, String> attributes, String key, String value) {
    if (value != null) {
      attributes.put(key, value);
    }
  }
}
