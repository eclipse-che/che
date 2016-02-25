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
package org.eclipse.che.api.workspace.gwt.client;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;

import java.util.List;

/**
 * GWT Client for Workspace Service.
 *
 * @author Yevhenii Voevodin
 */
public interface WorkspaceServiceClient {

    /**
     * Creates new workspace.
     *
     * @param newWorkspace
     *         the configuration to create the new workspace
     * @param account
     *         the account id related to this operation
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> create(WorkspaceConfigDto newWorkspace, String account);

    /**
     * Gets users workspace by id.
     *
     * @param wsId
     *         workspace ID
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> getUsersWorkspace(String wsId);

    /**
     * Gets runtime workspace by id.
     *
     * @param wsId
     *         workspace ID
     * @return a promise that resolves to the {@link RuntimeWorkspaceDto}, or rejects with an error
     */
    Promise<RuntimeWorkspaceDto> getRuntimeWorkspace(String wsId);

    /**
     * Gets all workspaces of current user.
     *
     * @param skip
     *         the number of the items to skip
     * @param limit
     *         the limit of the items in the response, default is 30
     * @return a promise that will provide a list of {@link UsersWorkspaceDto}s, or rejects with an error
     */
    Promise<List<UsersWorkspaceDto>> getWorkspaces(int skip, int limit);

    /**
     * Gets all runtime workspaces of current user.
     *
     * @param skip
     *         the number of the items to skip
     * @param limit
     *         the limit of the items in the response, default is 30
     * @return a promise that will provide a list of {@link RuntimeWorkspaceDto}s, or rejects with an error
     */
    Promise<List<RuntimeWorkspaceDto>> getRuntimeWorkspaces(int skip, int limit);

    /**
     * Updates workspace.
     *
     * @param wsId
     *         workspace ID
     * @param newCfg
     *         the new configuration to update the workspace
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> update(String wsId, WorkspaceConfigDto newCfg);

    /**
     * Removes workspace.
     *
     * @param wsId
     *         workspace ID
     * @return a promise that will resolve when the workspace has been removed, or rejects with an error
     */
    Promise<Void> delete(String wsId);

    /**
     * Starts temporary workspace based on given workspace configuration.
     *
     * @param cfg
     *         the configuration to start the workspace from
     * @param accountId
     *         the account id related to this operation
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<RuntimeWorkspaceDto> startTemporary(WorkspaceConfigDto cfg, String accountId);

    /**
     * Starts workspace based on workspace id and environment.
     *
     * @param id
     *         workspace ID
     * @param envName
     *         the name of the workspace environment that should be used for start
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> startById(String id, String envName);

    /**
     * Starts workspace based on workspace name and environment.
     *
     * @param name
     *         the name of the workspace to start
     * @param envName
     *         the name of the workspace environment that should be used for start
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> startByName(String name, String envName);

    /**
     * Stops running workspace.
     *
     * @param wsId
     *         workspace ID
     * @return a promise that will resolve when the workspace has been stopped, or rejects with an error
     */
    Promise<Void> stop(String wsId);

    /**
     * Get all commands from the specified workspace.
     *
     * @param wsId
     *         workspace ID
     * @return a promise that will provide a list of {@link CommandDto}s, or rejects with an error
     */
    Promise<List<CommandDto>> getCommands(String wsId);

    /**
     * Adds command to workspace
     *
     * @param wsId
     *         workspace ID
     * @param newCommand
     *         the new workspace command
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> addCommand(String wsId, CommandDto newCommand);

    /**
     * Updates command.
     *
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     * @see WorkspaceService#updateCommand(String, CommandDto)
     */
    Promise<UsersWorkspaceDto> updateCommand(String wsId, CommandDto commandUpdate);

    /**
     * Removes command from workspace.
     *
     * @param wsId
     *         workspace ID
     * @param commandName
     *         the name of the command to remove
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> deleteCommand(String wsId, String commandName);

    /**
     * Adds environment to workspace.
     *
     * @param wsId
     *         workspace ID
     * @param newEnv
     *         the new environment
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> addEnvironment(String wsId, EnvironmentDto newEnv);

    /**
     * Updates environment.
     *
     * @param wsId
     *         workspace ID
     * @param environmentUpdate
     *         the environment to update
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> updateEnvironment(String wsId, EnvironmentDto environmentUpdate);

    /**
     * Removes environment.
     *
     * @param wsId
     *         workspace ID
     * @param envName
     *         the name of the environment to remove
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> deleteEnvironment(String wsId, String envName);

    /**
     * Adds project configuration to workspace.
     *
     * @param wsId
     *         workspace ID
     * @param newProject
     *         the new project
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> addProject(String wsId, ProjectConfigDto newProject);

    /**
     * Updates project configuration.
     *
     * @param wsId
     *         workspace ID
     * @param newEnv
     *         the new project configuration
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> updateProject(String wsId, ProjectConfigDto newEnv);

    /**
     * Removes project from workspace.
     *
     * @param wsId
     *         workspace ID
     * @param projectName
     *         the name of the project to remove
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> deleteProject(String wsId, String projectName);

    /**
     * Creates machine in workspace.
     *
     * @param wsId
     *         workspace ID
     * @param machineConfig
     *         the new machine configuration
     * @return a promise that resolves to the {@link MachineDto}, or rejects with an error
     */
    Promise<MachineDto> createMachine(String wsId, MachineConfigDto machineConfig);

    /**
     * Returns workspace's snapshot.
     *
     * @param workspaceId
     *         workspace ID
     * @return a promise that will provide a list of {@link SnapshotDto}s, or rejects with an error
     */
    Promise<List<SnapshotDto>> getSnapshot(String workspaceId);

    /**
     * Creates snapshot of workspace.
     *
     * @param workspaceId
     *         workspace ID
     * @return a promise that will resolve when the snapshot has been created, or rejects with an error
     */
    Promise<Void> createSnapshot(String workspaceId);

    /**
     * Recovers workspace from snapshot.
     *
     * @param workspaceId
     *         workspace ID
     * @param envName
     *         the name of the workspace environment to recover from
     * @param accountId
     *         the account id related to this operation
     * @return a promise that resolves to the {@link UsersWorkspaceDto}, or rejects with an error
     */
    Promise<UsersWorkspaceDto> recoverWorkspace(String workspaceId, String envName, String accountId);
}
