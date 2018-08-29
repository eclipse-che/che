/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * ****************************************************************************
 */
package org.eclipse.core.resources.mapping;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A remote mapping context provides a model element with a view of the remote state of local
 * resources as they relate to a repository operation that is in progress. A repository provider can
 * pass an instance of this interface to a model element when obtaining a set of traversals for a
 * model element. This allows the model element to query the remote state of a resource in order to
 * determine if there are resources that exist remotely but do not exist locally that should be
 * included in the traversal.
 *
 * <p>This class may be subclassed by clients.
 *
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * @see org.eclipse.core.resources.mapping.ResourceMappingContext
 * @since 3.2
 */
public abstract class RemoteResourceMappingContext extends ResourceMappingContext {

  /**
   * Refresh flag constant (bit mask value 1) indicating that the mapping will be making use of the
   * contents of the files covered by the traversals being refreshed.
   */
  public static final int FILE_CONTENTS_REQUIRED = 1;

  /**
   * Refresh flag constant (bit mask value 0) indicating that no additional refresh behavior is
   * required.
   */
  public static final int NONE = 0;

  /**
   * For three-way comparisons, returns an instance of IStorage in order to allow the caller to
   * access the contents of the base resource that corresponds to the given local resource. The base
   * of a resource is the contents of the resource before any local modifications were made. If the
   * base file does not exist, or if this is a two-way comparison, <code>null</code> is returned.
   * The provided local file handle need not exist locally. A exception is thrown if the
   * corresponding base resource is not a file.
   *
   * <p>This method may be long running as a server may need to be contacted to obtain the contents
   * of the file.
   *
   * @param file the local file
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @return a storage that provides access to the contents of the local resource's corresponding
   *     remote resource. If the remote file does not exist, <code>null</code> is returned
   * @exception CoreException if the contents could not be fetched. Reasons include:
   *     <ul>
   *       <li>The server could not be contacted for some reason.
   *       <li>The corresponding remote resource is not a container (status code will be {@link
   *           IResourceStatus#RESOURCE_WRONG_TYPE}).
   *     </ul>
   */
  public abstract IStorage fetchBaseContents(IFile file, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Returns the members of the base resource corresponding to the given container. The container
   * and the returned members need not exist locally and may not include all children that exist
   * locally. An empty list is returned if the base resource is empty or does not exist. An
   * exception is thrown if the base resource is not capable of having members. This method returns
   * <code>null</code> if the base members cannot be computed, in which case clients should call
   * {@link #fetchMembers( IContainer , IProgressMonitor)} which returns the combined members for
   * the base and remote.
   *
   * <p>This method may be long running as a server may need to be contacted to obtain the members
   * of the base resource.
   *
   * <p>This default implementation always returns <code>null</code>, but subclasses may override.
   *
   * @param container the local container
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @return the members of the base resource corresponding to the given container
   * @exception CoreException if the members could not be fetched. Reasons include:
   *     <ul>
   *       <li>The server could not be contacted for some reason.
   *       <li>The base resource is not a container (status code will be {@link
   *           IResourceStatus#RESOURCE_WRONG_TYPE}).
   *     </ul>
   *
   * @since 3.3
   */
  public IResource[] fetchBaseMembers(IContainer container, IProgressMonitor monitor)
      throws CoreException {
    // default implementation does nothing
    // thwart compiler warning
    return null;
  }

  /**
   * Returns the combined members of the base and remote resources corresponding to the given
   * container. The container need not exist locally and the result may include entries that do not
   * exist locally and may not include all local children. An empty list is returned if the remote
   * resource which corresponds to the container is empty or if the remote does not exist. An
   * exception is thrown if the corresponding remote is not capable of having members.
   *
   * <p>This method may be long running as a server may need to be contacted to obtain the members
   * of the container's corresponding remote resource.
   *
   * @param container the local container
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @return returns the combined members of the base and remote resources corresponding to the
   *     given container.
   * @exception CoreException if the members could not be fetched. Reasons include:
   *     <ul>
   *       <li>The server could not be contacted for some reason.
   *       <li>The corresponding remote resource is not a container (status code will be {@link
   *           IResourceStatus#RESOURCE_WRONG_TYPE}).
   *     </ul>
   */
  public abstract IResource[] fetchMembers(IContainer container, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Returns an instance of IStorage in order to allow the caller to access the contents of the
   * remote that corresponds to the given local resource. If the remote file does not exist, <code>
   * null</code> is returned. The provided local file handle need not exist locally. A exception is
   * thrown if the corresponding remote resource is not a file.
   *
   * <p>This method may be long running as a server may need to be contacted to obtain the contents
   * of the file.
   *
   * @param file the local file
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @return a storage that provides access to the contents of the local resource's corresponding
   *     remote resource. If the remote file does not exist, <code>null</code> is returned
   * @exception CoreException if the contents could not be fetched. Reasons include:
   *     <ul>
   *       <li>The server could not be contacted for some reason.
   *       <li>The corresponding remote resource is not a container (status code will be {@link
   *           IResourceStatus#RESOURCE_WRONG_TYPE}).
   *     </ul>
   */
  public abstract IStorage fetchRemoteContents(IFile file, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Returns the members of the remote resource corresponding to the given container. The container
   * and the returned members need not exist locally and may not include all children that exist
   * locally. An empty list is returned if the remote resource is empty or does not exist. An
   * exception is thrown if the remote resource is not capable of having members. This method
   * returns <code>null</code> if the remote members cannot be computed, in which case clients
   * should call {@link #fetchMembers( IContainer , IProgressMonitor)} which returns the combined
   * members for the base and remote.
   *
   * <p>This method may be long running as a server may need to be contacted to obtain the members
   * of the remote resource.
   *
   * <p>This default implementation always returns <code>null</code>, but subclasses may override.
   *
   * @param container the local container
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @return the members of the remote resource corresponding to the given container
   * @exception CoreException if the members could not be fetched. Reasons include:
   *     <ul>
   *       <li>The server could not be contacted for some reason.
   *       <li>The remote resource is not a container (status code will be {@link
   *           IResourceStatus#RESOURCE_WRONG_TYPE}).
   *     </ul>
   *
   * @since 3.3
   */
  public IResource[] fetchRemoteMembers(IContainer container, IProgressMonitor monitor)
      throws CoreException {
    // default implementation does nothing
    // thwart compiler warning
    return null;
  }

  /**
   * Return the list of projects that apply to this context. In other words, the context is only
   * capable of querying the remote state for projects that are contained in the returned list.
   *
   * @return the list of projects that apply to this context
   */
  public abstract IProject[] getProjects();

  /**
   * For three-way comparisons, this method indicates whether local modifications have been made to
   * the given resource. For two-way comparisons, calling this method has the same effect as calling
   * {@link #hasRemoteChange(IResource, IProgressMonitor)}.
   *
   * @param resource the resource being tested
   * @param monitor a progress monitor
   * @return whether the resource contains local modifications
   * @exception CoreException if the contents could not be compared. Reasons include:
   *     <ul>
   *       <li>The server could not be contacted for some reason.
   *       <li>The corresponding remote resource is not a container (status code will be {@link
   *           IResourceStatus#RESOURCE_WRONG_TYPE}).
   *     </ul>
   */
  public abstract boolean hasLocalChange(IResource resource, IProgressMonitor monitor)
      throws CoreException;

  /**
   * For two-way comparisons, return whether the contents of the corresponding remote differs from
   * the content of the local file in the context of the current operation. By this we mean that
   * this method will return <code>true</code> if the remote contents differ from the local
   * contents.
   *
   * <p>For three-way comparisons, return whether the contents of the corresponding remote differ
   * from the contents of the base. In other words, this method returns <code>true</code> if the
   * corresponding remote has changed since the last time the local resource was updated with the
   * remote contents.
   *
   * <p>For two-way comparisons, return <code>true</code> if the remote contents differ from the
   * local contents. In this case, this method is equivalent to {@link #hasLocalChange(IResource,
   * IProgressMonitor)}
   *
   * <p>This can be used by clients to determine if they need to fetch the remote contents in order
   * to determine if the resources that constitute the model element are different in the remote
   * location. If the local file exists and the remote file does not, or the remote file exists and
   * the local does not then the contents will be said to differ (i.e. <code>true</code> is
   * returned). Also, implementors will most likely use a timestamp based comparison to determine if
   * the contents differ. This may lead to a situation where <code>true</code> is returned but the
   * actual contents do not differ. Clients must be prepared handle this situation.
   *
   * @param resource the local resource
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @return whether the contents of the corresponding remote differ from the base.
   * @exception CoreException if the contents could not be compared. Reasons include:
   *     <ul>
   *       <li>The server could not be contacted for some reason.
   *       <li>The corresponding remote resource is not a container (status code will be {@link
   *           IResourceStatus#RESOURCE_WRONG_TYPE}).
   *     </ul>
   */
  public abstract boolean hasRemoteChange(IResource resource, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Return <code>true</code> if the context is associated with an operation that is using a
   * three-way comparison and <code>false</code> if it is using a two-way comparison.
   *
   * @return whether the context is a three-way or two-way
   */
  public abstract boolean isThreeWay();

  /**
   * Refresh the known remote state for any resources covered by the given traversals. Clients who
   * require the latest remote state should invoke this method before invoking any others of the
   * class. Mappings can use this method as a hint to the context provider of which resources will
   * be required for the mapping to generate the proper set of traversals.
   *
   * <p>Note that this is really only a hint to the context provider. It is up to implementors to
   * decide, based on the provided traversals, how to efficiently perform the refresh. In the ideal
   * case, calls to {@link #hasRemoteChange( IResource , IProgressMonitor)} and {@link
   * #fetchMembers} would not need to contact the server after a call to a refresh with appropriate
   * traversals. Also, ideally, if {@link #FILE_CONTENTS_REQUIRED} is on of the flags, then the
   * contents for these files will be cached as efficiently as possible so that calls to {@link
   * #fetchRemoteContents} will also not need to contact the server. This may not be possible for
   * all context providers, so clients cannot assume that the above mentioned methods will not be
   * long running. It is still advisable for clients to call {@link #refresh} with as much details
   * as possible since, in the case where a provider is optimized, performance will be much better.
   *
   * @param traversals the resource traversals that indicate which resources are to be refreshed
   * @param flags additional refresh behavior. For instance, if {@link #FILE_CONTENTS_REQUIRED} is
   *     one of the flags, this indicates that the client will be accessing the contents of the
   *     files covered by the traversals. {@link #NONE} should be used when no additional behavior
   *     is required
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if the refresh fails. Reasons include:
   *     <ul>
   *       <li>The server could not be contacted for some reason.
   *     </ul>
   */
  public abstract void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor)
      throws CoreException;
}
