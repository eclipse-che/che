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
package org.eclipse.che.ide.api.project;

import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import java.util.List;

/**
 * Client for Project service.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 */
public interface ProjectServiceClient {

    /**
     * Get all projects in current workspace.
     *
     * @param workspaceId
     *         id of current workspace
     * @param callback
     *         the callback to use for the response
     */
    void getProjects(DevMachine devMachine, AsyncRequestCallback<List<ProjectConfigDto>> callback);

    /**
     * Get all projects in current workspace.
     *
     * @param workspaceId
     *         id of current workspace
     * @return a promise that will provide a list of {@link ProjectConfigDto}s, or rejects with an error
     */
    Promise<List<ProjectConfigDto>> getProjects(DevMachine devMachine);

    /**
     * Get project.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to the project to get
     * @param callback
     *         the callback to use for the response
     */
    void getProject(DevMachine devMachine, String path, AsyncRequestCallback<ProjectConfigDto> callback);

    /**
     * Get project.
     *
     * @param path
     *         path to the project
     * @return a promise that resolves to the {@link ProjectConfigDto}, or rejects with an error
     */
    Promise<ProjectConfigDto> getProject(DevMachine devMachine, String path);

    /**
     * Get item.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to the item to get
     * @param callback
     *         the callback to use for the response
     */
    void getItem(DevMachine devMachine, String path, AsyncRequestCallback<ItemReference> callback);

    /**
     * Create project.
     *
     * @param workspaceId
     *         id of current workspace
     * @param projectConfig
     *         descriptor of the project to create
     * @param callback
     *         the callback to use for the response
     */
    void createProject(DevMachine devMachine, ProjectConfigDto projectConfig, AsyncRequestCallback<ProjectConfigDto> callback);


    /**
     * Estimates if the folder supposed to be project of certain type.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path of the project to estimate
     * @param projectType
     *         Project Type ID to estimate against
     * @param callback
     *         the callback to use for the response
     */
    void estimateProject(DevMachine devMachine, String path, String projectType, AsyncRequestCallback<SourceEstimation> callback);


    /**
     * Gets list of {@link SourceEstimation} for all supposed project types.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path of the project to resolve
     * @param callback
     *         the callback to use for the response
     *
     * @deprecated instead of this method should use {@link ProjectServiceClient#resolveSources(String, String)}
     */
    void resolveSources(DevMachine devMachine, String path, AsyncRequestCallback<List<SourceEstimation>> callback);

     /**
      * Gets list of {@link SourceEstimation} for all supposed project types.
      *
      * @param workspaceId
      *         id of current workspace
      * @param path
      *         path of the project to resolve
      * @return a promise that will provide a list of {@code SourceEstimation} for the given {@code workspaceId} and {@code path},
      *         or rejects with on error
      */
    Promise<List<SourceEstimation>> resolveSources(DevMachine devMachine, String path);

    /**
     * Get sub-project.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to the parent project
     * @param callback
     *         the callback to use for the response
     */
    void getModules(DevMachine devMachine, String path, AsyncRequestCallback<List<ProjectConfigDto>> callback);

    /**
     * Create sub-project.
     *
     * @param workspaceId
     *         id of current workspace
     * @param parentProjectPath
     *         path to the parent project
     * @param projectConfig
     *         descriptor of the project to create
     * @param callback
     *         the callback to use for the response
     */
    void createModule(DevMachine devMachine,
                      String parentProjectPath,
                      ProjectConfigDto projectConfig,
                      AsyncRequestCallback<ProjectConfigDto> callback);

    /**
     * Update project.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to the project to get
     * @param descriptor
     *         descriptor of the project to update
     * @param callback
     *         the callback to use for the response
     *
     * @deprecated instead of this method should use {@link ProjectServiceClient#updateProject(String, String, ProjectConfigDto)}
     */
    void updateProject(DevMachine devMachine, String path, ProjectConfigDto descriptor, AsyncRequestCallback<ProjectConfigDto> callback);

    /**
     * Update project.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to the project to get
     * @param descriptor
     *         descriptor of the project to update
     * @return a promise that will provide updated {@link ProjectConfigDto} for {@code workspaceId}, {@code path}, {@code descriptor}
     *         or rejects with an error
     */
    Promise<ProjectConfigDto> updateProject(DevMachine devMachine, String path, ProjectConfigDto descriptor);

    /**
     * Create new file in the specified folder.
     *
     * @param workspaceId
     *         id of current workspace
     * @param parentPath
     *         path to parent for new file
     * @param name
     *         file name
     * @param content
     *         file content
     * @param callback
     *         the callback to use for the response
     */
    void createFile(DevMachine devMachine, String parentPath, String name, String content, AsyncRequestCallback<ItemReference> callback);

    /**
     * Get file content.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to file
     * @param callback
     *         the callback to use for the response
     */
    void getFileContent(DevMachine devMachine, String path, AsyncRequestCallback<String> callback);

    /**
     * Update file content.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to file
     * @param content
     *         new content of file
     * @param callback
     *         the callback to use for the response
     */
    void updateFile(DevMachine devMachine, String path, String content, AsyncRequestCallback<Void> callback);

    /**
     * Create new folder in the specified folder.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to parent for new folder
     * @param callback
     *         the callback to use for the response
     */
    void createFolder(DevMachine devMachine, String path, AsyncRequestCallback<ItemReference> callback);

    /**
     * Delete item.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to item to delete
     * @param callback
     *         the callback to use for the response
     */
    void delete(DevMachine devMachine, String path, AsyncRequestCallback<Void> callback);

    /**
     * Delete module.
     *
     * @param workspaceId
     *         id of current workspace
     * @param pathToParent
     *         path to module's parent
     * @param modulePath
     *         path to module to delete
     * @param callback
     *         the callback to use for the response
     */
    void deleteModule(DevMachine devMachine, String pathToParent, String modulePath, AsyncRequestCallback<Void> callback);

    /**
     * Copy an item with new name to the specified target path. Original item name is used if new name isn't set.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to the item to copy
     * @param newParentPath
     *         path to the target item
     * @param newName
     *         new resource name. Set <code>null</code> to copy without renaming
     * @param callback
     *         the callback to use for the response
     */
    void copy(DevMachine devMachine, String path, String newParentPath, String newName, AsyncRequestCallback<Void> callback);

    /**
     * Move an item to the specified target path. Set new name to rename the resource when moving.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to the item to move
     * @param newParentPath
     *         path to the target item
     * @param newName
     *         new resource name. Set <code>null</code> to move without renaming
     * @param callback
     *         the callback to use for the response
     */
    void move(DevMachine devMachine, String path, String newParentPath, String newName, AsyncRequestCallback<Void> callback);

    /**
     * Rename and/or set new media type for item.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to the item to rename
     * @param newName
     *         new name
     * @param newMediaType
     *         new media type. May be <code>null</code>
     * @param callback
     *         the callback to use for the response
     */
    void rename(DevMachine devMachine, String path, String newName, String newMediaType, AsyncRequestCallback<Void> callback);

    /**
     * Import sources into project.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to the project to import sources
     * @param force
     *         set true for force rewrite existed project
     * @param sourceStorage
     *         {@link SourceStorageDto}
     * @param callback
     *         the callback to use for the response
     */
    void importProject(DevMachine devMachine, String path, boolean force, SourceStorageDto sourceStorage, RequestCallback<Void> callback);

    /**
     * Import sources into project.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to the project to import sources
     * @param force
     *         if it's true then rewrites existing project
     * @param sourceStorage
     *         {@link SourceStorageDto}
     * @return a promise that will resolve when the project has been imported, or rejects with an error
     */
    Promise<Void> importProject(DevMachine devMachine, String path, boolean force, SourceStorageDto sourceStorage);

    /**
     * Get children for the specified path.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to get its children
     * @param callback
     *         the callback to use for the response
     */
    void getChildren(DevMachine devMachine, String path, AsyncRequestCallback<List<ItemReference>> callback);

    /**
     * Get folders tree starts from the specified path.
     *
     * @param workspaceId
     *         id of current workspace
     * @param path
     *         path to get its folder tree
     * @param depth
     *         depth for discover children
     * @param callback
     *         the callback to use for the response
     */
    void getTree(DevMachine devMachine, String path, int depth, AsyncRequestCallback<TreeElement> callback);

    /**
     * Search an item(s) by the specified criteria.
     *
     * @param workspaceId
     *         id of current workspace
     * @param expression
     *         search query expression
     * @return a promise that will provide a list of {@link ItemReference}s, or rejects with an error
     */
    Promise<List<ItemReference>> search(DevMachine devMachine, QueryExpression expression);
}
