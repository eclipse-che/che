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
package org.eclipse.che.ide.api.workspace;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;

import java.util.List;

/**
 * GWT Client for Workspace Service.
 *
 * @author Yevhenii Voevodin
 * @author Igor Vinokur
 */
public interface WorkspaceServiceClient {

    /**
     * Creates new workspace.
     *
     * @param newWorkspace
     *         the configuration to create the new workspace
     * @param account
     *         the account id related to this operation
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#create(WorkspaceConfigDto, List, Boolean, String)
     */
    Promise<WorkspaceDto> create(WorkspaceConfigDto newWorkspace, String account);

    /**
     * Gets users workspace by key.
     *
     * @param wsId
     *         workspace ID
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#getByKey(String)
     */
    Promise<WorkspaceDto> getWorkspace(String wsId);

    /**
     * Gets workspace by namespace and name
     *
     * @param namespace
     *         namespace
     * @param  workspaceName
     *         workspace name
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#getByKey(String)
     */
    Promise<WorkspaceDto> getWorkspace(String namespace, String workspaceName);

    /**
     * Gets all workspaces of current user.
     *
     * @param skip
     *         the number of the items to skip
     * @param limit
     *         the limit of the items in the response, default is 30
     * @return a promise that will provide a list of {@link WorkspaceDto}, or rejects with an error
     * @see #getWorkspaces(int, int)
     */
    Promise<List<WorkspaceDto>> getWorkspaces(int skip, int limit);

    /**
     * Updates workspace.
     *
     * @param wsId
     *         workspace ID
     * @param update
     *         the new configuration to update the workspace
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#update(String, WorkspaceDto)
     */
    Promise<WorkspaceDto> update(String wsId, WorkspaceDto update);

    /**
     * Removes workspace.
     *
     * @param wsId
     *         workspace ID
     * @return a promise that will resolve when the workspace has been removed, or rejects with an error
     * @see WorkspaceService#delete(String)
     */
    Promise<Void> delete(String wsId);

    /**
     * Starts temporary workspace based on given workspace configuration.
     *
     * @param cfg
     *         the configuration to start the workspace from
     * @param accountId
     *         the account id related to this operation
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#startFromConfig(WorkspaceConfigDto, Boolean, String)
     */
    Promise<WorkspaceDto> startFromConfig(WorkspaceConfigDto cfg, boolean isTemporary, String accountId);

    /**
     * Starts workspace based on workspace id and environment.
     *
     * @param id
     *         workspace ID
     * @param envName
     *         the name of the workspace environment that should be used for start
     * @param restore
     *         if <code>true</code> workspace will be restored from snapshot if snapshot exists,
     *         if <code>false</code> workspace will not be restored from snapshot
     *         even if auto-restore is enabled and snapshot exists
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#startById(String, String, Boolean, String)
     */
    Promise<WorkspaceDto> startById(String id, String envName, boolean restore);

    /**
     * Stops running workspace.
     *
     * @param wsId
     *         workspace ID
     * @return a promise that will resolve when the workspace has been stopped, or rejects with an error
     * @see WorkspaceService#stop(String, Boolean)
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
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#addCommand(String, CommandDto)
     */
    Promise<WorkspaceDto> addCommand(String wsId, CommandDto newCommand);

    /**
     * Updates command.
     *
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#updateCommand(String, String, CommandDto)
     */
    Promise<WorkspaceDto> updateCommand(String wsId, String commandName, CommandDto commandUpdate);

    /**
     * Removes command from workspace.
     *
     * @param wsId
     *         workspace ID
     * @param commandName
     *         the name of the command to remove
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#deleteCommand(String, String)
     */
    Promise<WorkspaceDto> deleteCommand(String wsId, String commandName);

    /**
     * Adds environment to workspace.
     *
     * @param wsId
     *         workspace ID
     * @param newEnv
     *         the new environment
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#addEnvironment(String, EnvironmentDto)
     */
    Promise<WorkspaceDto> addEnvironment(String wsId, EnvironmentDto newEnv);

    /**
     * Updates environment.
     *
     * @param wsId
     *         workspace ID
     * @param environmentUpdate
     *         the environment to update
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#updateEnvironment(String, String, EnvironmentDto)
     */
    Promise<WorkspaceDto> updateEnvironment(String wsId, String envName, EnvironmentDto environmentUpdate);

    /**
     * Removes environment.
     *
     * @param wsId
     *         workspace ID
     * @param envName
     *         the name of the environment to remove
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#deleteEnvironment(String, String)
     */
    Promise<WorkspaceDto> deleteEnvironment(String wsId, String envName);

    /**
     * Adds project configuration to workspace.
     *
     * @param wsId
     *         workspace ID
     * @param newProject
     *         the new project
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#addProject(String, ProjectConfigDto)
     */
    Promise<WorkspaceDto> addProject(String wsId, ProjectConfigDto newProject);

    /**
     * Updates project configuration.
     *
     * @param wsId
     *         workspace ID
     * @param newEnv
     *         the new project configuration
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#updateProject(String, String, ProjectConfigDto)
     */
    Promise<WorkspaceDto> updateProject(String wsId, String path, ProjectConfigDto newEnv);

    /**
     * Removes project from workspace.
     *
     * @param wsId
     *         workspace ID
     * @param projectName
     *         the name of the project to remove
     * @return a promise that resolves to the {@link WorkspaceDto}, or rejects with an error
     * @see WorkspaceService#deleteProject(String, String)
     */
    Promise<WorkspaceDto> deleteProject(String wsId, String projectName);

    /**
     * Creates machine in workspace.
     *
     * @param wsId
     *         workspace ID
     * @param machineConfig
     *         the new machine configuration
     * @return a promise that resolves to the {@link MachineDto}, or rejects with an error
     * @see WorkspaceService#createMachine(String, MachineConfigDto)
     */
    Promise<MachineDto> createMachine(String wsId, MachineConfigDto machineConfig);

    /**
     * Returns workspace's snapshot.
     *
     * @param workspaceId
     *         workspace ID
     * @return a promise that will provide a list of {@link SnapshotDto}s, or rejects with an error
     * @see WorkspaceService#getSnapshot(String)
     */
    Promise<List<SnapshotDto>> getSnapshot(String workspaceId);

    /**
     * Creates snapshot of workspace.
     *
     * @param workspaceId
     *         workspace ID
     * @return a promise that will resolve when the snapshot has been created, or rejects with an error
     * @see WorkspaceService#createSnapshot(String)
     */
    Promise<Void> createSnapshot(String workspaceId);

}
