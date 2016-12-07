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

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.resource.Path;

import java.util.List;
import java.util.Map;

/**
 * Serves the connections with the server side project service.
 * <p/>
 * By design this service is laid on the lowest business level which is operating only with data transfer objects (DTO).
 * This interface is not intended to implementing by the third party components or using it directly.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
public interface ProjectServiceClient {
    /**
     * Returns the projects list. If there is no projects were found on server, empty list is returned.
     *
     * @return {@link Promise} with list of project configuration
     * @see ProjectConfigDto
     * @since 4.4.0
     */
    Promise<List<ProjectConfigDto>> getProjects();

    /**
     * Returns the specific project by given {@code path}. Path to project should be an absolute.
     *
     * @param path
     *         path to project
     * @return {@link Promise} with project configuration
     * @see ProjectConfigDto
     * @see Path
     * @since 4.4.0
     */
    Promise<ProjectConfigDto> getProject(Path path);

    /**
     * Creates the file by given {@code path} with given {@code content}. Content may be an empty.
     *
     * @param path
     *         path to the future file
     * @param content
     *         the file content
     * @return {@link Promise} with the {@link ItemReference}
     * @see ItemReference
     * @see Path
     * @since 4.4.0
     */
    Promise<ItemReference> createFile(Path path, String content);

    /**
     * Creates the folder by given {@code path}.
     *
     * @param path
     *         path to the future folder
     * @return {@link Promise} with the {@link ItemReference}
     * @see ItemReference
     * @see Path
     * @since 4.4.0
     */
    Promise<ItemReference> createFolder(Path path);

    /**
     * Creates the project with given {@code configuration}.
     *
     * @param configuration
     *         the project configuration
     * @param options
     *         additional parameters that need for project generation
     * @return {@link Promise} with the {@link ProjectConfigDto}
     * @see ProjectConfigDto
     * @since 4.4.0
     */
    Promise<ProjectConfigDto> createProject(ProjectConfigDto configuration, Map<String, String> options);

    /**
     * Create batch of projects according to their configurations.
     * <p/>
     * Notes: a project will be created by importing when project configuration contains {@link SourceStorageDto}
     * object, otherwise this one will be created corresponding its {@link NewProjectConfigDto}:
     * <li> - {@link NewProjectConfigDto} object contains only one mandatory {@link NewProjectConfigDto#setPath(String)} field.
     * In this case Project will be created as project of "blank" type </li>
     * <li> - a project will be created as project of "blank" type when declared primary project type is not registered, </li>
     * <li> - a project will be created without mixin project type when declared mixin project type is not registered</li>
     * <li> - for creating a project by generator {@link NewProjectConfigDto#getOptions()} should be specified.</li>
     *
     * @param configurations
     *         the list of configurations to creating projects
     * @return {@link Promise} with the list of {@link ProjectConfigDto}
     * @see ProjectConfigDto
     */
    Promise<List<ProjectConfigDto>> createBatchProjects(List<NewProjectConfigDto> configurations);

    /**
     * Returns the item description by given {@code path}.
     *
     * @param path
     *         path to the item
     * @return {@link Promise} with the {@link ItemReference}
     * @see Path
     * @see ItemReference
     * @since 4.4.0
     */
    Promise<ItemReference> getItem(Path path);

    /**
     * Estimates the given {@code path} to be applied to specified {@code pType} (project type).
     *
     * @param path
     *         path to the folder
     * @param pType
     *         project type to estimate
     * @return {@link Promise} with the {@link SourceEstimation}
     * @see Path
     * @see SourceEstimation
     * @since 4.4.0
     */
    Promise<SourceEstimation> estimate(Path path, String pType);

    /**
     * Updates the project with the new {@code configuration} or creates the new one from existed folder on server side.
     *
     * @param configuration
     *         configuration which which be applied to the existed project
     * @return {@link Promise} with the applied {@link ProjectConfigDto}
     * @see ProjectConfigDto
     * @since 4.4.0
     */
    Promise<ProjectConfigDto> updateProject(ProjectConfigDto configuration);

    /**
     * Reads the file content by given {@code path}.
     *
     * @param path
     *         path to the file
     * @return {@link Promise} with file content
     * @see Path
     * @since 4.4.0
     */
    Promise<String> getFileContent(Path path);

    /**
     * Writes the file {@code content} by given {@code path}.
     *
     * @param path
     *         path to the file
     * @param content
     *         the file content
     * @return {@link Promise} with empty response
     * @see Path
     * @since 4.4.0
     */
    Promise<Void> setFileContent(Path path, String content);

    /**
     * Removes the item by given {@code path} from the server.
     *
     * @param path
     *         path to the item
     * @return {@link Promise} with empty response
     * @see Path
     * @since 4.4.0
     */
    Promise<Void> deleteItem(Path path);

    /**
     * Copies the {@code source} item to given {@code target} with {@code newName}.
     *
     * @param source
     *         the source path to be copied
     * @param target
     *         the target path, should be a container (project or folder)
     * @param newName
     *         the new name of the copied item
     * @param overwrite
     *         overwrite target is such has already exists
     * @return {@link Promise} with empty response
     * @see Path
     * @since 4.4.0
     */
    Promise<Void> copy(Path source, Path target, String newName, boolean overwrite);

    /**
     * Moves the {@code source} item to given {@code target} with {@code newName}.
     *
     * @param source
     *         the source path to be moved
     * @param target
     *         the target path, should be a container (project or folder)
     * @param newName
     *         the new name of the moved item
     * @param overwrite
     *         overwrite target is such has already exists
     * @return {@link Promise} with empty response
     * @see Path
     * @since 4.4.0
     */
    Promise<Void> move(Path source, Path target, String newName, boolean overwrite);

    /**
     * Imports the new project by given {@code source} configuration.
     *
     * @param path
     *         path to the future project
     * @param source
     *         source configuration
     * @return a promise that will resolve when the project has been imported, or rejects with an error
     * @see Path
     * @see SourceStorageDto
     * @since 4.4.0
     */
    Promise<Void> importProject(Path path, SourceStorageDto source);

    /**
     * Reads the project tree starting from {@code path} with given {@code depth}.
     *
     * @param path
     *         the start point path where read should start
     * @param depth
     *         the depth to read, e.g. -1, 0 or less than {@link Integer#MAX_VALUE}
     * @param includeFiles
     *         include files into response
     * @return {@link Promise} with tree response
     * @see Path
     * @see TreeElement
     * @since 4.4.0
     */
    Promise<TreeElement> getTree(Path path, int depth, boolean includeFiles);

    /**
     * Searches an item(s) with the specified criteria given by {@code expression}.
     *
     * @param expression
     *         search query expression
     * @return {@link Promise} with the list of found items
     * @see QueryExpression
     * @see ItemReference
     * @since 4.4.0
     */
    Promise<List<ItemReference>> search(QueryExpression expression);

    /**
     * Gets list of {@link SourceEstimation} for all supposed project types.
     *
     * @param path
     *         path of the project to resolve
     * @return {@link Promise} with the list of resolved estimations
     * @see Path
     * @see SourceEstimation
     * @since 4.4.0
     */
    Promise<List<SourceEstimation>> resolveSources(Path path);
}
