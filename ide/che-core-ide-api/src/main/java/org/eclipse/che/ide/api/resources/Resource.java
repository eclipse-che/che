/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.resources;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project.ProjectRequest;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.api.resources.marker.MarkerChangedEvent;
import org.eclipse.che.ide.resource.Path;

/**
 * The client side analog of file system files and directories. There are exactly three types of
 * resources: files, folders and projects.
 *
 * <p>Workspace root is representing by {@link Container}. In which only {@link Project} is allowed
 * to be created.
 *
 * <p>File resources are similar to files in that they hold data directly. Folder resources are
 * analogous to directories in that they hold other resources but cannot directly hold data. Project
 * resources group files and folders into reusable clusters.
 *
 * <p>Features of resources:
 *
 * <ul>
 *   <li>{@code Resource} objects are handles to state maintained by a workspace. That is, resources
 *       objects do not actually contain data themselves but rather represent resource state and
 *       give it behaviour.
 *   <li>Resources are identified by type and their {@code path}, which is similar to a file system
 *       path. The name of the resource is the last segment of its path. A resource's parent is
 *       located by removing the last segment (the resource's name) from the resource's full path.
 * </ul>
 *
 * <p>To obtain already initialized resource in workspace you just need to inject {@link AppContext}
 * into your component and call {@link AppContext#getProjects()} or {@link
 * AppContext#getWorkspaceRoot()}.
 *
 * <p>Note. This interface is not intended to be implemented by clients.
 *
 * @author Vlad Zhukovskyi
 * @see Container
 * @see File
 * @see Folder
 * @see Project
 * @see AppContext#getProjects()
 * @see AppContext#getWorkspaceRoot()
 * @since 4.4.0
 */
@Beta
public interface Resource extends Comparable<Resource> {
  /**
   * Type constant that describes {@code File} resource.
   *
   * @see Resource#getResourceType()
   * @see Resource#isFile()
   * @see File
   * @since 4.4.0
   */
  int FILE = 0x1;

  /**
   * Type constant that describes {@code Folder} resource.
   *
   * @see Resource#getResourceType()
   * @see Resource#isFolder()
   * @see Folder
   * @since 4.4.0
   */
  int FOLDER = 0x2;

  /**
   * Type constant that describes {@code Project} resource.
   *
   * @see Resource#getResourceType()
   * @see Resource#isProject()
   * @see Project
   * @since 4.4.0
   */
  int PROJECT = 0x4;

  /**
   * Returns {@code true} if current represents a file.
   *
   * @return true if current resource is file based resource.
   * @see Resource#getResourceType()
   * @see Resource#FILE
   * @since 4.4.0
   */
  default boolean isFile() {
    return getResourceType() == FILE;
  }

  /**
   * Casts current resource to the {@link File} if the last one's represents a file.
   *
   * <p>Example of usage:
   *
   * <pre>
   *    public void doSome() {
   *        Resource resource = ...;
   *        if (resource.isFile()) {
   *            File file = resource.asFile();
   *        }
   *    }
   * </pre>
   *
   * @return instance of {@link File}
   * @throws IllegalStateException in case if current resource is not a file
   * @see Resource#getResourceType()
   * @see Resource#FILE
   * @since 5.1.0
   */
  default File asFile() {
    checkState(isFile(), "Current resource is not a file");

    return (File) this;
  }

  /**
   * Returns {@code true} if current represents a folder.
   *
   * @return true if current resource is folder based resource.
   * @see Resource#getResourceType()
   * @see Resource#FOLDER
   * @since 4.4.0
   */
  default boolean isFolder() {
    return getResourceType() == FOLDER;
  }

  /**
   * Casts current resource to the {@link Folder} if the last one's represents a folder.
   *
   * <p>Example of usage:
   *
   * <pre>
   *    public void doSome() {
   *        Resource resource = ...;
   *        if (resource.isFolder()) {
   *            Folder folder = resource.asFolder();
   *        }
   *    }
   * </pre>
   *
   * @return instance of {@link Folder}
   * @throws IllegalStateException in case if current resource is not a folder
   * @see Resource#getResourceType()
   * @see Resource#FOLDER
   * @since 5.1.0
   */
  default Folder asFolder() {
    checkState(isFolder(), "Current resource is not a folder");

    return (Folder) this;
  }

  /**
   * Returns {@code true} if current represents a project.
   *
   * @return true if current resource is project based resource.
   * @see Resource#getResourceType()
   * @see Resource#PROJECT
   * @since 4.4.0
   */
  default boolean isProject() {
    return getResourceType() == PROJECT;
  }

  /**
   * Casts current resource to the {@link Project} if the last one's represents a project.
   *
   * <p>Example of usage:
   *
   * <pre>
   *    public void doSome() {
   *        Resource resource = ...;
   *        if (resource.isProject()) {
   *            Project project = resource.asProject();
   *        }
   *    }
   * </pre>
   *
   * @return instance of {@link Project}
   * @throws IllegalStateException in case if current resource is not a project
   * @see Resource#getResourceType()
   * @see Resource#PROJECT
   * @since 5.1.0
   */
  default Project asProject() {
    checkState(isProject(), "Current resource is not a project");

    return (Project) this;
  }

  /**
   * Copies resource to given {@code destination} path. Copy operation performs asynchronously and
   * result of current operation will be provided in {@code Promise} result. Destination path should
   * have write access.
   *
   * <p>Copy operation produces new {@link Resource} which is already cached.
   *
   * <p>Fires following events: {@link ResourceChangedEvent} when resource has successfully copied.
   * This event provides information about copied resource and source resource.
   *
   * <p>Example of usage:
   *
   * <pre>
   *     Resource resource = ... ;
   *     Path copyTo = ... ;
   *
   *     resource.copy(copyTo).then(new Operation<Resource>() {
   *          public void apply(Resource copiedResource) throws OperationException {
   *              //do something with copiedResource
   *          }
   *     })
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#ADDED}. Copied resource provided by {@link ResourceDelta#getResource()}.
   * Contains flags {@link ResourceDelta#COPIED_FROM}. Source resource is accessible by calling
   * {@link ResourceDelta#getFromPath()}.
   *
   * @param destination the destination path
   * @return {@link Promise} with copied {@link Resource}
   * @throws IllegalStateException if this resource could not be copied. Reasons include:
   *     <ul>
   *       <li>Resource already exists
   *       <li>Resource with path '/path' isn't a project
   *     </ul>
   *
   * @throws IllegalArgumentException if current resource can not be copied. Reasons include:
   *     <ul>
   *       <li>Workspace root is not allowed to be copied
   *     </ul>
   *
   * @see ResourceChangedEvent
   * @see Resource
   * @since 4.4.0
   */
  Promise<Resource> copy(Path destination);

  /**
   * Copies resource to given {@code destination} path. Copy operation performs asynchronously and
   * result of current operation will be provided in {@code Promise} result. Destination path should
   * have write access.
   *
   * <p>Copy operation produces new {@link Resource} which is already cached.
   *
   * <p>Fires following events: {@link ResourceChangedEvent} when resource has successfully copied.
   * This event provides information about copied resource and source resource.
   *
   * <p>Passing {@code force} argument as true method will ignore existed resource on the server and
   * overwrite them.
   *
   * <p>Example of usage:
   *
   * <pre>
   *     Resource resource = ... ;
   *     Path copyTo = ... ;
   *
   *     resource.copy(copyTo, true).then(new Operation<Resource>() {
   *          public void apply(Resource copiedResource) throws OperationException {
   *              //do something with copiedResource
   *          }
   *     })
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#ADDED}. Copied resource provided by {@link ResourceDelta#getResource()}.
   * Contains flags {@link ResourceDelta#COPIED_FROM}. Source resource is accessible by calling
   * {@link ResourceDelta#getFromPath()}.
   *
   * @param destination the destination path
   * @param force overwrite existed resource on the server
   * @return {@link Promise} with copied {@link Resource}
   * @throws IllegalStateException if this resource could not be copied. Reasons include:
   *     <ul>
   *       <li>Resource already exists
   *       <li>Resource with path '/path' isn't a project
   *     </ul>
   *
   * @throws IllegalArgumentException if current resource can not be copied. Reasons include:
   *     <ul>
   *       <li>Workspace root is not allowed to be copied
   *     </ul>
   *
   * @see ResourceChangedEvent
   * @see Resource
   * @since 4.4.0
   */
  Promise<Resource> copy(Path destination, boolean force);

  /**
   * Moves resource to given new {@code destination}. Move operation performs asynchronously and
   * result of current operation will be displayed in {@code Promise} result.
   *
   * <p>Move operation produces new {@link Resource} which is already cached.
   *
   * <p>Fires following events: {@link ResourceChangedEvent} when resource has successfully moved.
   * This event provides information about moved resource.
   *
   * <p>Before moving mechanism remembers deepest depth which was read and tries to restore it after
   * move.
   *
   * <p>Example of usage:
   *
   * <pre>
   *     Resource resource = ... ;
   *     Path moveTo = ... ;
   *
   *     resource.move(moveTo).then(new Operation<Resource>() {
   *          public void apply(Resource movedResource) throws OperationException {
   *              //do something with movedResource
   *          }
   *     })
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#REMOVED}. Removed resource is provided by {@link
   * ResourceDelta#getResource()}.
   *
   * <p>Also fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta
   * kind: {@link ResourceDelta#ADDED}. Moved resource provided by {@link
   * ResourceDelta#getResource()}. Contains flags {@link ResourceDelta#MOVED_FROM} and {@link
   * ResourceDelta#MOVED_TO}. Source resource is accessible by calling {@link
   * ResourceDelta#getFromPath()}. Moved resource (or new resource) is accessible by calling {@link
   * ResourceDelta#getToPath()}.
   *
   * @param destination the destination path
   * @return {@code Promise} with move moved {@link Resource}
   * @throws IllegalStateException if this resource could not be moved. Reasons include:
   *     <ul>
   *       <li>Resource already exists
   *       <li>Resource with path '/path' isn't a project
   *     </ul>
   *
   * @throws IllegalArgumentException if current resource can not be moved. Reasons include:
   *     <ul>
   *       <li>Workspace root is not allowed to be moved
   *     </ul>
   *
   * @see ResourceChangedEvent
   * @see Resource
   * @since 4.4.0
   */
  Promise<Resource> move(Path destination);

  /**
   * Moves resource to given new {@code destination}. Move operation performs asynchronously and
   * result of current operation will be displayed in {@code Promise} result.
   *
   * <p>Move operation produces new {@link Resource} which is already cached.
   *
   * <p>Fires following events: {@link ResourceChangedEvent} when resource has successfully moved.
   * This event provides information about moved resource.
   *
   * <p>Before moving mechanism remembers deepest depth which was read and tries to restore it after
   * move.
   *
   * <p>Passing {@code force} argument as true method will ignore existed resource on the server and
   * overwrite them.
   *
   * <p>Example of usage:
   *
   * <pre>
   *     Resource resource = ... ;
   *     Path moveTo = ... ;
   *
   *     resource.move(moveTo, true).then(new Operation<Resource>() {
   *          public void apply(Resource movedResource) throws OperationException {
   *              //do something with movedResource
   *          }
   *     })
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#REMOVED}. Removed resource is provided by {@link
   * ResourceDelta#getResource()}.
   *
   * <p>Also fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta
   * kind: {@link ResourceDelta#ADDED}. Moved resource provided by {@link
   * ResourceDelta#getResource()}. Contains flags {@link ResourceDelta#MOVED_FROM} and {@link
   * ResourceDelta#MOVED_TO}. Source resource is accessible by calling {@link
   * ResourceDelta#getFromPath()}. Moved resource (or new resource) is accessible by calling {@link
   * ResourceDelta#getToPath()}.
   *
   * @param destination the destination path
   * @return {@code Promise} with move moved {@link Resource}
   * @throws IllegalStateException if this resource could not be moved. Reasons include:
   *     <ul>
   *       <li>Resource already exists
   *       <li>Resource with path '/path' isn't a project
   *     </ul>
   *
   * @throws IllegalArgumentException if current resource can not be moved. Reasons include:
   *     <ul>
   *       <li>Workspace root is not allowed to be moved
   *     </ul>
   *
   * @see ResourceChangedEvent
   * @see Resource
   * @since 4.4.0
   */
  Promise<Resource> move(Path destination, boolean force);

  /**
   * Deletes current resource. Delete operation performs asynchronously and result of current
   * operation will be displayed in {@code Promise} result as {@code void}.
   *
   * <p>Fires following events: {@link ResourceChangedEvent} when resource has successfully removed.
   *
   * <p>Example of usage:
   *
   * <pre>
   *     Resource resource = ... ;
   *
   *     resource.delete().then(new Operation<Void>() {
   *         public void apply(Void ignored) throws OperationException {
   *             //do something
   *         }
   *     })
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#REMOVED}. Removed resource provided by {@link ResourceDelta#getResource()}
   *
   * @return {@code Promise} with {@code void}
   * @throws IllegalArgumentException if current resource can not be removed. Reasons include:
   *     <ul>
   *       <li>Workspace root is not allowed to be removed
   *     </ul>
   *
   * @see ResourceChangedEvent
   * @since 4.4.0
   */
  Promise<Void> delete();

  /**
   * Returns the full, absolute path of this resource relative to the project's root. e.g. {@code
   * "/project_name/path/to/resource"}.
   *
   * @return the absolute path of this resource
   * @see Path
   * @since 4.4.0
   */
  Path getLocation();

  /**
   * Returns the name of the resource. The name of a resource is synonymous with the last segment of
   * its full (or project-relative) path.
   *
   * @return the name of the resource
   * @since 4.4.0
   */
  String getName();

  /**
   * Returns the resource which is the parent of this resource or {@code null} if such parent
   * doesn't exist. (This means that this resource is 'root' project)
   *
   * @return the resource's parent {@link Container}
   * @see Container
   * @since 5.1.0
   */
  Container getParent();

  /**
   * Returns the {@code Project} which contains this resource. Returns itself for projects. A
   * resource's project is the one named by the first segment of its full path.
   *
   * <p>By design, each node should be bound to specified {@link Project}.
   *
   * @return the {@link Optional} with related project
   * @see Project
   * @since 4.4.0
   * @deprecated use {@link #getProject()}
   */
  @Deprecated
  Optional<Project> getRelatedProject();

  /**
   * Returns the {@link Project} which is bound to this resource or {@code null}.
   *
   * <p>Returns itself for projects.
   *
   * @return the bound instance of {@link Project} or null
   * @since 5.1.0
   */
  @Nullable
  Project getProject();

  /**
   * Returns the type of this resource. Th returned value will be on of {@code FILE}, {@code
   * FOLDER}, {@code PROJECT}.
   *
   * <p>
   *
   * <ul>
   *   <li>All resources of type {@code FILE} implement {@code File}.
   *   <li>All resources of type {@code FOLDER} implement {@code Folder}.
   *   <li>All resources of type {@code PROJECT} implement {@code Project}.
   * </ul>
   *
   * @return the type of this resource
   * @see #FILE
   * @see #FOLDER
   * @see #PROJECT
   * @since 4.4.0
   */
  int getResourceType();

  /**
   * Returns the URL of this resource. The URL allows to download locally current resource.
   *
   * <p>For container based resource the URL link will allow download container as zip archive.
   *
   * @return the URL of the resource
   * @throws IllegalArgumentException if URL is requested on workspace root. Reasons include:
   *     <ul>
   *       <li>Workspace root doesn't have export URL
   *     </ul>
   *
   * @since 4.4.0
   */
  String getURL();

  /**
   * Returns the marker handle with given {@code type} for the resource. The resource is not checked
   * to see if it has such a marker. The returned marker need not exist.
   *
   * @param type the known marker type
   * @return the {@link Optional} with specified registered marker
   * @throws IllegalArgumentException in case if given marker type is invalid (null or empty).
   *     Reasons include:
   *     <ul>
   *       <li>Invalid marker type occurred
   *     </ul>
   *
   * @see Marker#getType()
   * @see #getMarkers()
   * @since 4.4.0
   */
  Optional<Marker> getMarker(String type);

  /**
   * Returns all markers of the specified type on this resource. If there is no marker bound to the
   * resource, then empty array will be returned.
   *
   * @return the array of markers
   * @see #getMarker(String)
   * @since 4.4.0
   */
  Marker[] getMarkers();

  /**
   * Bound given {@code marker} to current resource. if such marker is already bound to the resource
   * it will be overwritten.
   *
   * <p>Fires following events: {@link MarkerChangedEvent} with status {@link Marker#UPDATED} when
   * existed marker has been replaced with new one. {@link MarkerChangedEvent} with status {@link
   * Marker#CREATED} when marker has been added to the current resource.
   *
   * @param marker the resource marker
   * @throws IllegalArgumentException in case if given marker is invalid. Reasons include:
   *     <ul>
   *       <li>Null marker occurred
   *     </ul>
   *
   * @see MarkerChangedEvent
   * @since 4.4.0
   */
  void addMarker(Marker marker);

  /**
   * Delete specified marker with given {@code type}.
   *
   * <p>Fires following event: {@link MarkerChangedEvent} with status {@link Marker#REMOVED} when
   * given marker has been removed from current resource.
   *
   * @param type the marker type
   * @return true if specified marker removed
   * @throws IllegalArgumentException in case if given marker type is invalid (null or empty).
   *     Reasons include:
   *     <ul>
   *       <li>Invalid marker type occurred
   *     </ul>
   *
   * @see Marker#getType()
   * @see MarkerChangedEvent
   * @since 4.4.0
   */
  boolean deleteMarker(String type);

  /**
   * Delete all markers which is bound to current resource.
   *
   * @return true if all markers has been removed
   * @since 4.4.0
   */
  boolean deleteAllMarkers();

  /**
   * Returns the nearest parent resource which has given marker {@code type}.
   *
   * @param type the marker type
   * @return the {@link Optional} with specified registered marker
   * @throws IllegalArgumentException in case if given marker type is invalid (null or empty).
   *     Reasons include:
   *     <ul>
   *       <li>Invalid marker type occurred
   *     </ul>
   *
   * @since 4.4.0
   */
  Optional<Resource> getParentWithMarker(String type);

  /** {@inheritDoc} */
  @Override
  boolean equals(Object other);

  /** {@inheritDoc} */
  @Override
  int hashCode();

  /**
   * Base interface for resource modification request.
   *
   * @param <R> the resource type, should extends {@link Resource}
   * @param <O> the body which is used to construct the request
   * @see ProjectRequest
   * @since 4.4.0
   */
  @Beta
  interface Request<R extends Resource, O> {

    /**
     * The body which is used to transform input data into request which is responsible for resource
     * modification.
     *
     * @param object the request body
     * @return instance of current {@link Request}
     * @see #getBody()
     * @since 4.4.0
     */
    Request<R, O> withBody(O object);

    /**
     * Returns the body which is used to transform input data into request.
     *
     * @return the request body
     * @see #withBody(Object)
     * @since 4.4.0
     */
    O getBody();

    /**
     * Sends the request to server and returns the {@link Promise} with new instance of {@link R}
     * which belongs to provided request configuration.
     *
     * <p>Uses to modify state of concrete resource.
     *
     * @return the {@link Promise} with new instance {@link R}
     * @see Container#newProject()
     * @see Container#importProject()
     * @see Project#update()
     * @since 4.4.0
     */
    Promise<R> send();
  }
}
