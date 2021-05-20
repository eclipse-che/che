/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.core.model.workspace.devfile.Action;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;
import org.eclipse.che.api.core.model.workspace.devfile.Entrypoint;
import org.eclipse.che.api.core.model.workspace.devfile.Env;
import org.eclipse.che.api.core.model.workspace.devfile.Metadata;
import org.eclipse.che.api.core.model.workspace.devfile.PreviewUrl;
import org.eclipse.che.api.core.model.workspace.devfile.Project;
import org.eclipse.che.api.core.model.workspace.devfile.Source;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.shared.dto.MachineDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.ServerConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ServerDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.VolumeDto;
import org.eclipse.che.api.workspace.shared.dto.WarningDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.ComponentDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileActionDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileCommandDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileVolumeDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.EndpointDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.EntrypointDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.EnvDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.MetadataDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.PreviewUrlDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.ProjectDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.SourceDto;

/**
 * Helps to convert to/from DTOs related to workspace.
 *
 * @author Yevhenii Voevodin
 */
public final class DtoConverter {

  /** Converts {@link Workspace} to {@link WorkspaceDto}. */
  public static WorkspaceDto asDto(Workspace workspace) {
    WorkspaceDto workspaceDto =
        newDto(WorkspaceDto.class)
            .withId(workspace.getId())
            .withStatus(workspace.getStatus())
            .withNamespace(workspace.getNamespace())
            .withTemporary(workspace.isTemporary())
            .withAttributes(workspace.getAttributes());

    if (workspace.getConfig() != null) {
      workspaceDto.setConfig(asDto(workspace.getConfig()));
    }

    if (workspace.getDevfile() != null) {
      workspaceDto.setDevfile(asDto(workspace.getDevfile()));
    }

    if (workspace.getRuntime() != null) {
      RuntimeDto runtime = asDto(workspace.getRuntime());
      workspaceDto.setRuntime(runtime);
    }

    return workspaceDto;
  }

  public static DevfileDto asDto(Devfile devfile) {
    List<DevfileCommandDto> commands =
        devfile.getCommands().stream().map(DtoConverter::asDto).collect(toList());
    List<ComponentDto> components =
        devfile.getComponents().stream().map(DtoConverter::asDto).collect(toList());
    List<ProjectDto> projects =
        devfile.getProjects().stream().map(DtoConverter::asDto).collect(toList());
    return newDto(DevfileDto.class)
        .withApiVersion(devfile.getApiVersion())
        .withCommands(commands)
        .withComponents(components)
        .withProjects(projects)
        .withAttributes(devfile.getAttributes())
        .withMetadata(asDto(devfile.getMetadata()));
  }

  private static ProjectDto asDto(Project project) {
    Source source = project.getSource();
    return newDto(ProjectDto.class)
        .withName(project.getName())
        .withClonePath(project.getClonePath())
        .withSource(
            newDto(SourceDto.class)
                .withType(source.getType())
                .withLocation(source.getLocation())
                .withBranch(source.getBranch())
                .withStartPoint(source.getStartPoint())
                .withTag(source.getTag())
                .withCommitId(source.getCommitId())
                .withSparseCheckoutDir(source.getSparseCheckoutDir()));
  }

  private static ComponentDto asDto(Component component) {
    return newDto(ComponentDto.class)
        .withType(component.getType())
        .withAlias(component.getAlias())
        .withAutomountWorkspaceSecrets(component.getAutomountWorkspaceSecrets())
        // chePlugin/cheEditor
        .withId(component.getId())
        .withRegistryUrl(component.getRegistryUrl())
        // chePlugin
        .withPreferences(component.getPreferences())
        // dockerimage
        .withImage(component.getImage())
        .withMemoryLimit(component.getMemoryLimit())
        .withMemoryRequest(component.getMemoryRequest())
        .withCpuLimit(component.getCpuLimit())
        .withCpuRequest(component.getCpuRequest())
        .withCommand(component.getCommand())
        .withArgs(component.getArgs())
        .withEndpoints(component.getEndpoints().stream().map(DtoConverter::asDto).collect(toList()))
        .withEnv(component.getEnv().stream().map(DtoConverter::asDto).collect(toList()))
        .withMountSources(component.getMountSources())
        .withVolumes(component.getVolumes().stream().map(DtoConverter::asDto).collect(toList()))
        // k8s/os
        .withReference(component.getReference())
        .withReferenceContent(component.getReferenceContent())
        .withSelector(component.getSelector())
        .withEntrypoints(
            component.getEntrypoints().stream().map(DtoConverter::asDto).collect(toList()));
  }

  private static EntrypointDto asDto(Entrypoint entrypoint) {
    return newDto(EntrypointDto.class)
        .withContainerName(entrypoint.getContainerName())
        .withParentName(entrypoint.getParentName())
        .withParentSelector(entrypoint.getParentSelector())
        .withCommand(entrypoint.getCommand())
        .withArgs(entrypoint.getArgs());
  }

  private static DevfileVolumeDto asDto(
      org.eclipse.che.api.core.model.workspace.devfile.Volume volume) {
    return newDto(DevfileVolumeDto.class)
        .withName(volume.getName())
        .withContainerPath(volume.getContainerPath());
  }

  private static EnvDto asDto(Env env) {
    return newDto(EnvDto.class).withName(env.getName()).withValue(env.getValue());
  }

  private static EndpointDto asDto(Endpoint endpoint) {
    return newDto(EndpointDto.class)
        .withName(endpoint.getName())
        .withPort(endpoint.getPort())
        .withAttributes(endpoint.getAttributes());
  }

  private static DevfileCommandDto asDto(
      org.eclipse.che.api.core.model.workspace.devfile.Command command) {
    List<DevfileActionDto> actions =
        command.getActions().stream().map(DtoConverter::asDto).collect(toList());

    DevfileCommandDto commandDto =
        newDto(DevfileCommandDto.class)
            .withName(command.getName())
            .withActions(actions)
            .withAttributes(command.getAttributes());

    if (command.getPreviewUrl() != null) {
      commandDto.setPreviewUrl(asDto(command.getPreviewUrl()));
    }
    return commandDto;
  }

  private static PreviewUrlDto asDto(PreviewUrl previewUrl) {
    final PreviewUrlDto previewUrlDto = newDto(PreviewUrlDto.class);
    if (previewUrl != null) {
      if (previewUrl.getPath() != null) {
        previewUrlDto.setPath(previewUrl.getPath());
      }
      if (previewUrl.getPort() != 0) {
        previewUrlDto.setPort(previewUrl.getPort());
      }
    }
    return previewUrlDto;
  }

  private static DevfileActionDto asDto(Action action) {
    return newDto(DevfileActionDto.class)
        .withComponent(action.getComponent())
        .withType(action.getType())
        .withWorkdir(action.getWorkdir())
        .withCommand(action.getCommand())
        .withReference(action.getReference())
        .withReferenceContent(action.getReferenceContent());
  }

  /** Converts {@link WorkspaceConfig} to {@link WorkspaceConfigDto}. */
  public static WorkspaceConfigDto asDto(WorkspaceConfig workspace) {
    List<CommandDto> commands =
        workspace.getCommands().stream().map(DtoConverter::asDto).collect(toList());
    List<ProjectConfigDto> projects =
        workspace.getProjects().stream().map(DtoConverter::asDto).collect(toList());
    Map<String, EnvironmentDto> environments =
        workspace
            .getEnvironments()
            .entrySet()
            .stream()
            .collect(toMap(Map.Entry::getKey, entry -> asDto(entry.getValue())));

    return newDto(WorkspaceConfigDto.class)
        .withName(workspace.getName())
        .withDefaultEnv(workspace.getDefaultEnv())
        .withCommands(commands)
        .withProjects(projects)
        .withEnvironments(environments)
        .withAttributes(workspace.getAttributes())
        .withDescription(workspace.getDescription());
  }

  /** Converts {@link Command} to {@link CommandDto}. */
  public static CommandDto asDto(Command command) {
    return newDto(CommandDto.class)
        .withName(command.getName())
        .withCommandLine(command.getCommandLine())
        .withType(command.getType())
        .withAttributes(command.getAttributes());
  }

  /** Converts {@link ProjectConfig} to {@link ProjectConfigDto}. */
  public static ProjectConfigDto asDto(ProjectConfig projectCfg) {
    final ProjectConfigDto projectConfigDto =
        newDto(ProjectConfigDto.class)
            .withName(projectCfg.getName())
            .withDescription(projectCfg.getDescription())
            .withPath(projectCfg.getPath())
            .withType(projectCfg.getType())
            .withAttributes(projectCfg.getAttributes())
            .withMixins(projectCfg.getMixins());
    final SourceStorage source = projectCfg.getSource();
    if (source != null) {
      projectConfigDto.withSource(
          newDto(SourceStorageDto.class)
              .withLocation(source.getLocation())
              .withType(source.getType())
              .withParameters(source.getParameters()));
    }
    return projectConfigDto;
  }

  /** Converts {@link Environment} to {@link EnvironmentDto}. */
  public static EnvironmentDto asDto(Environment env) {
    final EnvironmentDto envDto = newDto(EnvironmentDto.class);
    if (env.getMachines() != null) {
      envDto.withMachines(
          env.getMachines()
              .entrySet()
              .stream()
              .collect(toMap(Map.Entry::getKey, entry -> asDto(entry.getValue()))));
    }
    if (env.getRecipe() != null) {
      envDto.withRecipe(
          newDto(RecipeDto.class)
              .withType(env.getRecipe().getType())
              .withContentType(env.getRecipe().getContentType())
              .withLocation(env.getRecipe().getLocation())
              .withContent(env.getRecipe().getContent()));
    }
    return envDto;
  }

  /** Converts {@link MachineConfig} to {@link MachineConfigDto}. */
  public static MachineConfigDto asDto(MachineConfig machine) {
    MachineConfigDto machineDto = newDto(MachineConfigDto.class);
    if (machine.getServers() != null) {
      machineDto.setServers(
          machine
              .getServers()
              .entrySet()
              .stream()
              .collect(toMap(Map.Entry::getKey, entry -> asDto(entry.getValue()))));
    }
    if (machine.getAttributes() != null) {
      machineDto.setAttributes(machine.getAttributes());
    }
    if (machine.getVolumes() != null) {
      machineDto.setVolumes(
          machine
              .getVolumes()
              .entrySet()
              .stream()
              .collect(toMap(Map.Entry::getKey, entry -> asDto(entry.getValue()))));
    }
    if (machine.getEnv() != null) {
      machineDto.setEnv(machine.getEnv());
    }
    return machineDto;
  }

  /** Converts {@link ServerConfig} to {@link ServerConfigDto}. */
  public static ServerConfigDto asDto(ServerConfig serverConf) {
    return newDto(ServerConfigDto.class)
        .withPort(serverConf.getPort())
        .withProtocol(serverConf.getProtocol())
        .withPath(serverConf.getPath())
        .withAttributes(serverConf.getAttributes());
  }

  /** Converts {@link Runtime} to {@link RuntimeDto}. */
  public static RuntimeDto asDto(Runtime runtime) {
    if (runtime == null) {
      return null;
    }
    RuntimeDto runtimeDto = newDto(RuntimeDto.class).withActiveEnv(runtime.getActiveEnv());
    if (runtime.getMachines() != null) {
      runtimeDto.setMachines(
          runtime
              .getMachines()
              .entrySet()
              .stream()
              .collect(toMap(Map.Entry::getKey, entry -> asDto(entry.getValue()))));
    }
    if (runtime.getWarnings() != null) {
      runtimeDto.setWarnings(
          runtime.getWarnings().stream().map(DtoConverter::asDto).collect(toList()));
    }

    if (runtime.getCommands() != null) {
      runtimeDto.setCommands(
          runtime.getCommands().stream().map(DtoConverter::asDto).collect(toList()));
    }
    return runtimeDto;
  }

  /** Converts {@link RuntimeIdentity} to {@link RuntimeIdentityDto}. */
  public static RuntimeIdentityDto asDto(RuntimeIdentity identity) {
    return newDto(RuntimeIdentityDto.class)
        .withWorkspaceId(identity.getWorkspaceId())
        .withEnvName(identity.getEnvName())
        .withOwnerId(identity.getOwnerId());
  }

  /** Converts {@link Volume} to {@link VolumeDto}. */
  public static VolumeDto asDto(Volume volume) {
    return newDto(VolumeDto.class).withPath(volume.getPath());
  }

  /** Converts {@link Warning} to {@link WarningDto}. */
  public static WarningDto asDto(Warning warning) {
    return newDto(WarningDto.class).withCode(warning.getCode()).withMessage(warning.getMessage());
  }

  /** Converts {@link Server} to {@link ServerDto}. */
  public static ServerDto asDto(Server server) {
    return newDto(ServerDto.class)
        .withUrl(server.getUrl())
        .withStatus(server.getStatus())
        .withAttributes(server.getAttributes());
  }

  /** Converts {@link Machine} to {@link MachineDto}. */
  public static MachineDto asDto(Machine machine) {
    MachineDto machineDto =
        newDto(MachineDto.class)
            .withAttributes(machine.getAttributes())
            .withStatus(machine.getStatus());
    if (machine.getServers() != null) {
      machineDto.withServers(
          machine
              .getServers()
              .entrySet()
              .stream()
              .collect(toMap(Map.Entry::getKey, entry -> asDto(entry.getValue()))));
    }
    return machineDto;
  }

  /** Converts {@link Metadata} to {@link MetadataDto}. */
  public static MetadataDto asDto(Metadata metadata) {
    return newDto(MetadataDto.class)
        .withName(metadata.getName())
        .withGenerateName(metadata.getGenerateName());
  }

  private DtoConverter() {}
}
