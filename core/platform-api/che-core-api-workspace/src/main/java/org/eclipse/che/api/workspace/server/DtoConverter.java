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
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.EnvironmentState;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.RuntimeWorkspace;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.machine.shared.Permissions;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineStateDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.machine.shared.dto.recipe.GroupDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.PermissionsDescriptor;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.model.stack.StackSource;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentStateDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

// TODO! use global registry for DTO converters

/**
 * Helps to convert to/from DTOs related to workspace.
 *
 * @author Eugene Voevodin
 */
public final class DtoConverter {

    /**
     * Converts {@link UsersWorkspace} to {@link UsersWorkspaceDto}.
     */
    public static UsersWorkspaceDto asDto(UsersWorkspace workspace) {
        final List<CommandDto> commands = workspace.getCommands()
                                                   .stream()
                                                   .map(DtoConverter::asDto)
                                                   .collect(toList());
        final List<ProjectConfigDto> projects = workspace.getProjects()
                                                         .stream()
                                                         .map(DtoConverter::asDto)
                                                         .collect(toList());
        final List<EnvironmentStateDto> environments = workspace.getEnvironments()
                                                                .stream()
                                                                .map(DtoConverter::asDto)
                                                                .collect(toList());

        return newDto(UsersWorkspaceDto.class).withId(workspace.getId())
                                              .withStatus(workspace.getStatus())
                                              .withName(workspace.getName())
                                              .withOwner(workspace.getOwner())
                                              .withDefaultEnv(workspace.getDefaultEnv())
                                              .withCommands(commands)
                                              .withProjects(projects)
                                              .withEnvironments(environments)
                                              .withDescription(workspace.getDescription())
                                              .withAttributes(workspace.getAttributes());
    }

    /**
     * Converts {@link WorkspaceConfig} to {@link WorkspaceConfigDto}.
     */
    public static WorkspaceConfigDto asDto(WorkspaceConfig workspace) {
        final List<CommandDto> commands = workspace.getCommands()
                                                   .stream()
                                                   .map(DtoConverter::asDto)
                                                   .collect(toList());
        final List<ProjectConfigDto> projects = workspace.getProjects()
                                                         .stream()
                                                         .map(DtoConverter::asDto)
                                                         .collect(toList());
        final List<EnvironmentDto> environments = workspace.getEnvironments()
                                                           .stream()
                                                           .map(DtoConverter::asDto)
                                                           .collect(toList());

        return newDto(WorkspaceConfigDto.class).withName(workspace.getName())
                                               .withDefaultEnv(workspace.getDefaultEnv())
                                               .withCommands(commands)
                                               .withProjects(projects)
                                               .withEnvironments(environments)
                                               .withDescription(workspace.getDescription())
                                               .withAttributes(workspace.getAttributes());
    }

    /**
     * Converts {@link Command} to {@link CommandDto}.
     */
    public static CommandDto asDto(Command command) {
        return newDto(CommandDto.class).withName(command.getName())
                                       .withCommandLine(command.getCommandLine())
                                       .withType(command.getType())
                                       .withAttributes(command.getAttributes());
    }

    /**
     * Convert {@link StackImpl} to {@link StackDto}
     */
    public static StackDto asDto(StackImpl stack) {
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

        PermissionsDescriptor permissionsDescriptor = null;
        if (stack.getPermissions() != null) {
            Permissions permissions = stack.getPermissions();

            List<GroupDescriptor> groups = permissions.getGroups()
                                                      .stream()
                                                      .map(descriptor -> newDto(GroupDescriptor.class).withName(descriptor.getName())
                                                                                                      .withAcl(descriptor.getAcl())
                                                                                                      .withUnit(descriptor.getUnit()))
                                                      .collect(toList());

            permissionsDescriptor = newDto(PermissionsDescriptor.class).withGroups(groups)
                                                                       .withUsers(permissions.getUsers());
        }

        return newDto(StackDto.class).withId(stack.getId())
                                     .withName(stack.getName())
                                     .withDescription(stack.getDescription())
                                     .withCreator(stack.getCreator())
                                     .withScope(stack.getScope())
                                     .withTags(stack.getTags())
                                     .withComponents(componentsDto)
                                     .withWorkspaceConfig(workspaceConfigDto)
                                     .withSource(stackSourceDto)
                                     .withPermissions(permissionsDescriptor);
    }

    /**
     * Converts {@link ProjectConfig} to {@link ProjectConfigDto}.
     */
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

    //TODO add recipe

    /**
     * Converts {@link Environment} to {@link EnvironmentDto}.
     */
    public static EnvironmentDto asDto(Environment environment) {
        final List<MachineConfigDto> machineConfigs = environment.getMachineConfigs()
                                                                 .stream()
                                                                 .map(org.eclipse.che.api.machine.server.DtoConverter::asDto)
                                                                 .collect(toList());
        return newDto(EnvironmentDto.class).withName(environment.getName()).withMachineConfigs(machineConfigs);
    }

    /**
     * Converts {@link EnvironmentState} to {@link EnvironmentStateDto}.
     */
    public static EnvironmentStateDto asDto(EnvironmentState environment) {
        final List<MachineStateDto> machineConfigs = environment.getMachineConfigs()
                                                                .stream()
                                                                .map(org.eclipse.che.api.machine.server.DtoConverter::asDto)
                                                                .collect(toList());
        return newDto(EnvironmentStateDto.class).withName(environment.getName()).withMachineConfigs(machineConfigs);
    }

    /**
     * Converts {@link RuntimeWorkspace} to {@link RuntimeWorkspaceDto}.
     */
    public static RuntimeWorkspaceDto asDto(RuntimeWorkspace workspace) {
        final List<MachineDto> machines = workspace.getMachines()
                                                   .stream()
                                                   .map(org.eclipse.che.api.machine.server.DtoConverter::asDto)
                                                   .collect(toList());
        final List<CommandDto> commands = workspace.getCommands()
                                                   .stream()
                                                   .map(DtoConverter::asDto)
                                                   .collect(toList());
        final List<ProjectConfigDto> projects = workspace.getProjects()
                                                         .stream()
                                                         .map(DtoConverter::asDto)
                                                         .collect(toList());
        final List<EnvironmentStateDto> environments = workspace.getEnvironments()
                                                               .stream()
                                                               .map(DtoConverter::asDto)
                                                               .collect(toList());

        return newDto(RuntimeWorkspaceDto.class).withId(workspace.getId())
                                                .withName(workspace.getName())
                                                .withStatus(workspace.getStatus())
                                                .withOwner(workspace.getOwner())
                                                .withActiveEnvName(workspace.getActiveEnvName())
                                                .withDefaultEnv(workspace.getDefaultEnv())
                                                .withCommands(commands)
                                                .withProjects(projects)
                                                .withEnvironments(environments)
                                                .withAttributes(workspace.getAttributes())
                                                .withDevMachine(
                                                        org.eclipse.che.api.machine.server.DtoConverter.asDto(workspace.getDevMachine()))
                                                .withRootFolder(workspace.getRootFolder())
                                                .withMachines(machines)
                                                .withDescription(workspace.getDescription());
    }

    public static SnapshotDto asDto(Snapshot snapshot) {
        return newDto(SnapshotDto.class).withId(snapshot.getId())
                                        .withCreationDate(snapshot.getCreationDate())
                                        .withDescription(snapshot.getDescription())
                                        .withDev(snapshot.isDev())
                                        .withOwner(snapshot.getOwner())
                                        .withType(snapshot.getType())
                                        .withWorkspaceId(snapshot.getWorkspaceId())
                                        .withEnvName(snapshot.getEnvName())
                                        .withMachineName(snapshot.getEnvName());
    }

    private DtoConverter() {}
}
