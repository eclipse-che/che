/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.resources;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.QueryExpression;
import org.eclipse.che.ide.api.resources.Project.ProjectRequest;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.NameUtils;

/**
 * Interface for resource which may contain other resources (termed its members).
 *
 * <p>If {@code location} of current container is equals to {@link Path#ROOT} then it means that
 * current container represent the workspace root. To obtain the workspace root {@link AppContext}
 * should be injected into third- party component and method {@link AppContext#getWorkspaceRoot()}
 * should be called. Only {@link Project}s are allowed to be created in workspace root.
 *
 * <p>Note. This interface is not intended to be implemented by clients.
 *
 * @author Vlad Zhukovskyi
 * @see Project
 * @see Folder
 * @see AppContext
 * @see AppContext#getWorkspaceRoot()
 * @since 4.4.0
 */
@Beta
public interface Container extends Resource {

  /**
   * Returns the {@code Promise} with handle to the file resource identified by the given path in
   * this container.
   *
   * <p>The supplied path should represent relative path to file in this container.
   *
   * @param relativePath the path of the member file
   * @return the {@code Promise} with the handle of the member file
   * @throws IllegalStateException if during resource search failed has been occurred. Reasons
   *     include:
   *     <ul>
   *       <li>Resource with path '/project_path' doesn't exists
   *       <li>Resource with path '/project_path' isn't a project
   *       <li>Not a file
   *     </ul>
   *
   * @see #getContainer(Path)
   * @since 4.4.0
   */
  Promise<Optional<File>> getFile(Path relativePath);

  /**
   * Returns the {@code Promise} with handle to the file resource identified by the given path in
   * this container.
   *
   * <p>The supplied path should represent relative path to file in this container.
   *
   * @param relativePath the path of the member file
   * @return the {@code Promise} with the handle of the member file
   * @throws IllegalStateException if during resource search failed has been occurred. Reasons
   *     include:
   *     <ul>
   *       <li>Resource with path '/project_path' doesn't exists
   *       <li>Resource with path '/project_path' isn't a project
   *       <li>Not a file
   *     </ul>
   *
   * @see #getContainer(Path)
   * @since 4.4.0
   */
  Promise<Optional<File>> getFile(String relativePath);

  /**
   * Returns the {@code Promise} with handle to the container identified by the given path in this
   * container.
   *
   * <p>The supplied path should represent relative path to folder.
   *
   * @param relativePath the path of the member container
   * @return the {@code Promise} with the handle of the member container
   * @throws IllegalStateException if during resource search failed has been occurred. Reasons
   *     include:
   *     <ul>
   *       <li>Resource with path '/project_path' doesn't exists
   *       <li>Resource with path '/project_path' isn't a project
   *       <li>Not a container
   *     </ul>
   *
   * @see #getFile(Path)
   * @since 4.4.0
   */
  Promise<Optional<Container>> getContainer(Path relativePath);

  /**
   * Returns the {@code Promise} with handle to the container identified by the given path in this
   * container.
   *
   * <p>The supplied path should represent relative path to folder.
   *
   * @param relativePath the path of the member container
   * @return the {@code Promise} with the handle of the member container
   * @throws IllegalStateException if during resource search failed has been occurred. Reasons
   *     include:
   *     <ul>
   *       <li>Resource with path '/project_path' doesn't exists
   *       <li>Resource with path '/project_path' isn't a project
   *       <li>Not a container
   *     </ul>
   *
   * @see #getFile(Path)
   * @since 4.4.0
   */
  Promise<Optional<Container>> getContainer(String relativePath);

  /**
   * Returns the {@code Promise} with array of existing member resources (projects, folders and
   * files) in this resource, in particular order. Order is organized by alphabetic resource name
   * ignoring case.
   *
   * <p>Supplied parameter {@code force} instructs that stored children should be updated.
   *
   * <p>Note, that if the result array is empty, then method thinks that children may not be loaded
   * from the server and send a request ot the server to load the children.
   *
   * <p>Method guarantees that resources will be sorted by their {@link #getLocation()} in ascending
   * order.
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#ADDED}. Cached and loaded resource provided by {@link
   * ResourceDelta#getResource()}.
   *
   * <p>Or
   *
   * <p>Delta kind: {@link ResourceDelta#UPDATED}. When resource was cached previously. Updated
   * resource provided by {@link ResourceDelta#getResource()}.
   *
   * @return the {@code Promise} with array of members of this resource
   * @see #getChildren()
   * @since 4.4.0
   */
  Promise<Resource[]> getChildren();

  /**
   * Returns the {@code Promise} with array of existing member resources (projects, folders and
   * files) in this resource, in particular order. Order is organized by alphabetic resource name
   * ignoring case.
   *
   * <p>Supplied parameter {@code force} instructs that stored children should be updated.
   *
   * <p>Note, that if supplied argument {@code force} is set to {@code false} and result array is
   * empty, then method thinks that children may not be loaded from the server and send a request ot
   * the server to load the children.
   *
   * <p>Method guarantees that resources will be sorted by their {@link #getLocation()} in ascending
   * order.
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#ADDED}. Cached and loaded resource provided by {@link
   * ResourceDelta#getResource()}.
   *
   * <p>Or
   *
   * <p>Delta kind: {@link ResourceDelta#UPDATED}. When resource was cached previously. Updated
   * resource provided by {@link ResourceDelta#getResource()}.
   *
   * <p>May fire {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#REMOVED}. Removed resource provided by {@link
   * ResourceDelta#getResource()}. In case if {@code force} is set in {@code true}.
   *
   * @return the {@code Promise} with array of members of this resource
   * @see #getChildren()
   * @since 4.4.0
   */
  Promise<Resource[]> getChildren(boolean force);

  /**
   * Creates the new {@link Project} in current container.
   *
   * <p>Fires following events: {@link ResourceChangedEvent} when project has successfully created.
   *
   * <p>Calling this method doesn't create a project immediately. To complete the request method
   * {@link ProjectRequest#send()} should be called. {@link ProjectRequest} has ability to
   * reconfigure project during update/create operations.
   *
   * <p>Calling {@link ProjectRequest#send()} produces new {@link Project} resource.
   *
   * <p>The supplied argument {@code name} should be a valid and pass validation within {@link
   * NameUtils#checkProjectName(String)}. The supplied argument {@code type} should be a valid and
   * registered project type.
   *
   * <p>
   *
   * <p>Example of usage for creating a new project:
   *
   * <pre>
   *     ProjectConfig config = ... ;
   *     Container workspace = ... ;
   *
   *     Promise<Project> newProjectPromise = workspace.newProject()
   *                                                   .withBody(config)
   *                                                   .send();
   *
   *     newProjectPromise.then(new Operation<Project>() {
   *         public void apply(Project newProject) throws OperationException {
   *              //do something with new project
   *         }
   *     });
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#ADDED}. Created resource (instance of {@link Project}) provided by {@link
   * ResourceDelta#getResource()}
   *
   * @return the create project request
   * @throws IllegalArgumentException if arguments is not a valid. Reasons include:
   *     <ul>
   *       <li>Invalid project name
   *       <li>Invalid project type
   *     </ul>
   *
   * @throws IllegalStateException if creation was failed. Reasons include:
   *     <ul>
   *       <li>Resource already exists
   *     </ul>
   *
   * @see NameUtils#checkProjectName(String)
   * @see ProjectRequest
   * @see ProjectRequest#send()
   * @since 4.4.0
   */
  ProjectRequest newProject();

  /**
   * Creates the new {@link Project} in current container with specified source storage (in other
   * words, imports a remote project).
   *
   * <p>Fires following events: {@link ResourceChangedEvent} when project has successfully created.
   *
   * <p>Calling this method doesn't import a project immediately. To complete the request method
   * {@link ProjectRequest#send()} should be called.
   *
   * <p>Calling {@link ProjectRequest#send()} produces new {@link Project} resource.
   *
   * <p>The supplied argument {@code name} should be a valid and pass validation within {@link
   * NameUtils#checkProjectName(String)}.
   *
   * <p>
   *
   * <p>Example of usage for creating a new project:
   *
   * <pre>
   *     ProjectConfig config = ... ;
   *     Container workspace = ... ;
   *
   *     Promise<Project> newProjectPromise = workspace.importProject()
   *                                                   .withBody(config)
   *                                                   .send();
   *
   *     newProjectPromise.then(new Operation<Project>() {
   *         public void apply(Project newProject) throws OperationException {
   *              //do something with new project
   *         }
   *     });
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#ADDED}. Created resource (instance of {@link Project}) provided by {@link
   * ResourceDelta#getResource()}
   *
   * @return the create project request
   * @throws IllegalArgumentException if arguments is not a valid. Reasons include:
   *     <ul>
   *       <li>Invalid project name
   *     </ul>
   *
   * @throws IllegalStateException if creation was failed. Reasons include:
   *     <ul>
   *       <li>Resource already exists
   *     </ul>
   *
   * @see NameUtils#checkProjectName(String)
   * @see ProjectRequest
   * @see ProjectRequest#send()
   * @since 4.4.0
   */
  ProjectRequest importProject();

  /**
   * Creates the new {@link Folder} in current container.
   *
   * <p>Fires following events: {@link ResourceChangedEvent} when folder has successfully created.
   *
   * <p>Method produces new {@link Folder}.
   *
   * <p>The supplied argument {@code name} should be a valid and pass validation within {@link
   * NameUtils#checkFolderName(String)}.
   *
   * <p>Note. That folders can not be created in workspace root (obtained by {@link
   * AppContext#getWorkspaceRoot()}). Creating folder in this container will be failed.
   *
   * <p>Example of usage:
   *
   * <pre>
   *     Container workspace = ... ;
   *
   *     workspace.newFolder("name").then(new Operation<Folder>() {
   *         public void apply(Folder newFolder) throws OperationException {
   *              //do something with new folder
   *         }
   *     });
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#ADDED}. Created resource (instance of {@link Folder}) provided by {@link
   * ResourceDelta#getResource()}
   *
   * @param name the name of the folder
   * @return the {@link Promise} with created {@link Folder}
   * @throws IllegalArgumentException if arguments is not a valid. Reasons include:
   *     <ul>
   *       <li>Invalid folder name
   *       <li>Failed to create folder in workspace root
   *     </ul>
   *
   * @throws IllegalStateException if creation was failed. Reasons include:
   *     <ul>
   *       <li>Resource already exists
   *     </ul>
   *
   * @see NameUtils#checkFolderName(String)
   * @since 4.4.0
   */
  Promise<Folder> newFolder(String name);

  /**
   * Creates the new {@link File} in current container.
   *
   * <p>Fires following events: {@link ResourceChangedEvent} when file has successfully created.
   *
   * <p>Method produces new {@link File}.
   *
   * <p>The supplied argument {@code name} should be a valid and pass validation within {@link
   * NameUtils#checkFileName(String)} (String)}.
   *
   * <p>Note. That files can not be created in workspace root (obtained by {@link
   * AppContext#getWorkspaceRoot()}). Creating folder in this container will be failed.
   *
   * <p>The file content may be a {@code null} or empty.
   *
   * <p>Example of usage:
   *
   * <pre>
   *     Container workspace = ... ;
   *
   *     workspace.newFile("name", "content").then(new Operation<File>() {
   *         public void apply(File newFile) throws OperationException {
   *              //do something with new file
   *         }
   *     });
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#ADDED}. Created resource (instance of {@link File}) provided by {@link
   * ResourceDelta#getResource()}
   *
   * @param name the name of the file
   * @param content the file content
   * @return the {@link Promise} with created {@link File}
   * @throws IllegalArgumentException if arguments is not a valid. Reasons include:
   *     <ul>
   *       <li>Invalid file name
   *       <li>Failed to create file in workspace root
   *     </ul>
   *
   * @throws IllegalStateException if creation was failed. Reasons include:
   *     <ul>
   *       <li>Resource already exists
   *     </ul>
   *
   * @see NameUtils#checkFileName(String)
   * @since 4.4.0
   */
  Promise<File> newFile(String name, String content);

  /**
   * Synchronizes the cached container and its children with the local file system.
   *
   * <p>For refreshing entire workspace root this method should be called on the container, which
   * obtained from {@link AppContext#getWorkspaceRoot()}.
   *
   * <p>Fires following events: {@link ResourceChangedEvent} when the synchronized resource has
   * changed.
   *
   * <p>Method doesn't guarantees the sorted order of the returned resources.
   *
   * @return the array of resource which where affected by synchronize operation
   * @since 4.4.0
   */
  Promise<Resource[]> synchronize();

  /**
   * Synchronizes the given {@code deltas} with already cached resources. Method is useful for
   * third-party components which performs changes with resources outside of client side resource
   * management.
   *
   * <p>Method should be called on the workspace root {@link AppContext#getWorkspaceRoot()}.
   *
   * @param deltas the deltas which should be resolved
   * @return the {@link Promise} with resolved deltas
   * @throws IllegalStateException in case if method has been called outside of workspace root.
   *     Reasons include:
   *     <ul>
   *       <li>External deltas should be applied on the workspace root
   *     </ul>
   *
   * @see ExternalResourceDelta
   * @see ResourceDelta
   * @since 4.4.0
   */
  Promise<ResourceDelta[]> synchronize(ResourceDelta... deltas);

  /**
   * Searches the all possible files which matches given file or content mask.
   *
   * <p>Supplied file mask may supports wildcard:
   *
   * <ul>
   *   <li>{@code *} - which matches any character sequence (including the empty one)
   *   <li>{@code ?} - which matches any single character
   * </ul>
   *
   * <p>Method doesn't guarantees the sorted order of the returned resources.
   *
   * @param fileMask the file name mask
   * @param contentMask the content entity mask
   * @return the {@link Promise} with array of found results
   * @since 4.4.0
   */
  Promise<SearchResult> search(String fileMask, String contentMask);

  /**
   * Searches the all possible files which configured into {@link QueryExpression}.
   *
   * <p>Method doesn't guarantees the sorted order of the returned resources.
   *
   * @param queryExpression the search query expression includes search parameters
   * @return the {@link Promise} with array of found results
   */
  Promise<SearchResult> search(QueryExpression queryExpression);

  /**
   * Creates the search expression which matches given file or content mask.
   *
   * <p>Supplied file mask may supports wildcard:
   *
   * <ul>
   *   <li>{@code *} - which matches any character sequence (including the empty one)
   *   <li>{@code ?} - which matches any single character
   * </ul>
   *
   * @param fileMask the file name mask
   * @param query the content entity mask
   * @return the instance of {@link QueryExpression}
   */
  QueryExpression createSearchQueryExpression(String fileMask, String query);

  /**
   * Returns the plain list of file tree with given {@code depth}.
   *
   * <p>Input {@code depth} should be within the range from -1 to {@link Integer#MAX_VALUE}.
   *
   * <p>In case if {@code depth} equals to 0, then empty resource is returned. In case if {@code
   * depth} equals to -1, then whole file tree is loaded and returned.
   *
   * <p>Method doesn't guarantee that resources will be sorted by their {@link #getLocation()} in
   * any order.
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#ADDED}. Cached and loaded resource provided by {@link
   * ResourceDelta#getResource()}.
   *
   * <p>Or
   *
   * <p>Delta kind: {@link ResourceDelta#UPDATED}. When resource was cached previously. Updated
   * resource provided by {@link ResourceDelta#getResource()}.
   *
   * @param depth the depth
   * @return plain array of loaded resources
   * @throws IllegalArgumentException in case if invalid depth passed as argument. i.e. depth equals
   *     -2, -3 and so on. Reasons include:
   *     <ul>
   *       <li>Invalid depth
   *     </ul>
   *
   * @since 4.4.0
   */
  Promise<Resource[]> getTree(int depth);

  /**
   * Estimates if the current container supposed to be a project within certain {@code projectType}.
   *
   * @param projectType the project type to estimate for
   * @return the {@link SourceEstimation} with estimated attributes
   * @throws IllegalArgumentException in case if project type is {@code null} or empty. Reasons
   *     include:
   *     <ul>
   *       <li>Null project type
   *       <li>Empty project type
   *     </ul>
   *
   * @see SourceEstimation
   * @see ProjectType#getId()
   * @since 4.4.0
   */
  Promise<SourceEstimation> estimate(String projectType);
}
