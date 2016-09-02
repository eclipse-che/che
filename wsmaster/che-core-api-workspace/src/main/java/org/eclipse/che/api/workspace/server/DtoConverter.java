/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Snapshot;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.core.model.workspace.ServerConf2;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.eclipse.che.api.workspace.shared.stack.StackSource;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Helps to convert to/from DTOs related to workspace.
 *
 * @author Yevhenii Voevodin
 */
public final class DtoConverter {

    /** Converts {@link Workspace} to {@link WorkspaceDto}. */
    public static WorkspaceDto asDto(Workspace workspace) {
        return newDto(WorkspaceDto.class).withId(workspace.getId())
                                         .withStatus(workspace.getStatus())
                                         .withNamespace(workspace.getNamespace())
                                         .withTemporary(workspace.isTemporary())
                                         .withAttributes(workspace.getAttributes())
                                         .withConfig(asDto(workspace.getConfig()))
                                         .withRuntime(asDto(workspace.getRuntime()));
    }

    /** Converts {@link WorkspaceConfig} to {@link WorkspaceConfigDto}. */
    public static WorkspaceConfigDto asDto(WorkspaceConfig workspace) {
        List<CommandDto> commands = workspace.getCommands()
                                             .stream()
                                             .map(DtoConverter::asDto)
                                             .collect(toList());
        List<ProjectConfigDto> projects = workspace.getProjects()
                                                   .stream()
                                                   .map(DtoConverter::asDto)
                                                   .collect(toList());
        Map<String, EnvironmentDto> environments = workspace.getEnvironments()
                                                            .entrySet()
                                                            .stream()
                                                            .collect(toMap(Map.Entry::getKey,
                                                                           entry -> asDto(entry.getValue())));

        return newDto(WorkspaceConfigDto.class).withName(workspace.getName())
                                               .withDefaultEnv(workspace.getDefaultEnv())
                                               .withCommands(commands)
                                               .withProjects(projects)
                                               .withEnvironments(environments)
                                               .withDescription(workspace.getDescription());
    }

    /** Converts {@link Command} to {@link CommandDto}. */
    public static CommandDto asDto(Command command) {
        return newDto(CommandDto.class).withName(command.getName())
                                       .withCommandLine(command.getCommandLine())
                                       .withType(command.getType())
                                       .withAttributes(command.getAttributes());
    }

    /** Convert {@link StackImpl} to {@link StackDto}. */
    public static StackDto asDto(Stack stack) {
        WorkspaceConfigDto workspaceConfigDto = null;
        if (stack.getWorkspaceConfig() != null) {
            workspaceConfigDto = asDto(stack.getWorkspaceConfig());
        }

        StackSourceDto stackSourceDto = null;
        StackSource source = stack.getSource();
        if (source != null) {
            stackSourceDto = newDto(StackSourceDto.class).withType(source.getType()).withOrigin(source.getOrigin());
        }

        List<StackComponentDto> componentsDto = null;
        if (stack.getComponents() != null) {
            componentsDto = stack.getComponents()
                                 .stream()
                                 .map(component -> newDto(StackComponentDto.class).withName(component.getName())
                                                                                  .withVersion(component.getVersion()))
                                 .collect(toList());
        }

        return newDto(StackDto.class).withId(stack.getId())
                                     .withName(stack.getName())
                                     .withDescription(stack.getDescription())
                                     .withCreator(stack.getCreator())
                                     .withScope(stack.getScope())
                                     .withTags(stack.getTags())
                                     .withComponents(componentsDto)
                                     .withWorkspaceConfig(workspaceConfigDto)
                                     .withSource(stackSourceDto);
    }

    /** Converts {@link ProjectConfig} to {@link ProjectConfigDto}. */
    public static ProjectConfigDto asDto(ProjectConfig projectCfg) {
        final ProjectConfigDto projectConfigDto = newDto(ProjectConfigDto.class).withName(projectCfg.getName())
                                                                                .withDescription(projectCfg.getDescription())
                                                                                .withPath(projectCfg.getPath())
                                                                                .withType(projectCfg.getType())
                                                                                .withAttributes(projectCfg.getAttributes())
                                                                                .withMixins(projectCfg.getMixins());
        final SourceStorage source = projectCfg.getSource();
        if (source != null) {
            projectConfigDto.withSource(newDto(SourceStorageDto.class).withLocation(source.getLocation())
                                                                      .withType(source.getType())
                                                                      .withParameters(source.getParameters()));
        }
        return projectConfigDto;
    }

    /** Converts {@link Environment} to {@link EnvironmentDto}. */
    public static EnvironmentDto asDto(Environment env) {
        final EnvironmentDto envDto = newDto(EnvironmentDto.class);
        if (env.getMachines() != null) {
            envDto.withMachines(env.getMachines()
                                   .entrySet()
                                   .stream()
                                   .collect(toMap(Map.Entry::getKey,
                                                  entry -> asDto(entry.getValue()))));
        }
        if (env.getRecipe() != null) {
            envDto.withRecipe(newDto(EnvironmentRecipeDto.class).withType(env.getRecipe().getType())
                                                                .withContentType(env.getRecipe().getContentType())
                                                                .withLocation(env.getRecipe().getLocation())
                                                                .withContent(env.getRecipe().getContent()));
        }
        return envDto;
    }

    /** Converts {@link ExtendedMachine} to {@link ExtendedMachineDto}. */
    public static ExtendedMachineDto asDto(ExtendedMachine machine) {
        ExtendedMachineDto machineDto = newDto(ExtendedMachineDto.class).withAgents(machine.getAgents());
        if (machine.getServers() != null) {
            machineDto.setServers(machine.getServers()
                                         .entrySet()
                                         .stream()
                                         .collect(toMap(Map.Entry::getKey,
                                                        entry -> asDto(entry.getValue()))));
        }
        if (machine.getAttributes() != null) {
            machineDto.setAttributes(machine.getAttributes());
        }
        return machineDto;
    }

    /** Converts {@link ServerConf2} to {@link ServerConf2Dto}. */
    public static ServerConf2Dto asDto(ServerConf2 serverConf) {
        return newDto(ServerConf2Dto.class).withPort(serverConf.getPort())
                                           .withProtocol(serverConf.getProtocol())
                                           .withProperties(serverConf.getProperties());
    }

    /** Converts {@link WorkspaceRuntime} to {@link WorkspaceRuntimeDto}. */
    public static WorkspaceRuntimeDto asDto(WorkspaceRuntime runtime) {
        if (runtime == null) {
            return null;
        }
        final WorkspaceRuntimeDto runtimeDto = newDto(WorkspaceRuntimeDto.class).withActiveEnv(runtime.getActiveEnv())
                                                                                .withRootFolder(runtime.getRootFolder());
        runtimeDto.withMachines(runtime.getMachines()
                                       .stream()
                                       .map(org.eclipse.che.api.machine.server.DtoConverter::asDto)
                                       .collect(toList()));
        if (runtime.getDevMachine() != null) {
            runtimeDto.withDevMachine(org.eclipse.che.api.machine.server.DtoConverter.asDto(runtime.getDevMachine()));
        }
        return runtimeDto;
    }

    /** Converts {@link Snapshot} to {@link SnapshotDto}. */
    public static SnapshotDto asDto(Snapshot snapshot) {
        return newDto(SnapshotDto.class).withId(snapshot.getId())
                                        .withCreationDate(snapshot.getCreationDate())
                                        .withDescription(snapshot.getDescription())
                                        .withDev(snapshot.isDev())
                                        .withNamespace(snapshot.getNamespace())
                                        .withType(snapshot.getType())
                                        .withWorkspaceId(snapshot.getWorkspaceId())
                                        .withEnvName(snapshot.getEnvName())
                                        .withMachineName(snapshot.getMachineName());
    }

    private DtoConverter() {}
}
