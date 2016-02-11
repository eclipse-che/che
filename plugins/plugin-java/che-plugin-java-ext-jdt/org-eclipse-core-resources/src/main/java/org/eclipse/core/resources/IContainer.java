/*******************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] add addFilter/removeFilter/getFilters
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.runtime.*;

/**
 * Interface for resources which may contain
 * other resources (termed its <em>members</em>). While the 
 * workspace itself is not considered a container in this sense, the
 * workspace root resource is a container.
 * <p>
 * Containers implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager()
 * @see IProject
 * @see IFolder
 * @see IWorkspaceRoot
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IContainer extends IResource, IAdaptable {

	/*====================================================================
	 * Constants defining which members are wanted:
	 *====================================================================*/

	/**
	 * Member constant (bit mask value 1) indicating that phantom member resources are
	 * to be included.
	 * 
	 * @see IResource#isPhantom()
	 * @since 2.0
	 */
	public static final int INCLUDE_PHANTOMS = 1;

	/**
	 * Member constant (bit mask value 2) indicating that team private members are
	 * to be included.
	 * 
	 * @see IResource#isTeamPrivateMember()
	 * @since 2.0
	 */
	public static final int INCLUDE_TEAM_PRIVATE_MEMBERS = 2;

	/**
	 * Member constant (bit mask value 4) indicating that derived resources
	 * are to be excluded.
	 * 
	 * @see IResource#isDerived()
	 * @since 3.1
	 */
	public static final int EXCLUDE_DERIVED = 4;
	
	/**
	 * Member constant (bit mask value 8) indicating that hidden resources
	 * are to be included.
	 * 
	 * @see IResource#isHidden()
	 * @since 3.4
	 */
	public static final int INCLUDE_HIDDEN = 8;

	/**
	 * Member constant (bit mask value 16) indicating that a resource
	 * should not be checked for existence.
	 * 
	 * @see IResource#accept(IResourceProxyVisitor, int)
	 * @see IResource#accept(IResourceVisitor, int, int)
	 * @since 3.8
	 */
	public static final int DO_NOT_CHECK_EXISTENCE = 16;

	/**
	 * Returns whether a resource of some type with the given path 
	 * exists relative to this resource.
	 * The supplied path may be absolute or relative; in either case, it is
	 * interpreted as relative to this resource.  Trailing separators are ignored.
	 * If the path is empty this container is checked for existence.
	 *
	 * @param path the path of the resource
	 * @return <code>true</code> if a resource of some type with the given path 
	 *     exists relative to this resource, and <code>false</code> otherwise
	 * @see IResource#exists()
	 */
	public boolean exists(IPath path);

	/**
	 * Finds and returns the member resource identified by the given path in
	 * this container, or <code>null</code> if no such resource exists.
	 * The supplied path may be absolute or relative; in either case, it is
	 * interpreted as relative to this resource. Trailing separators and the path's
	 * device are ignored. If the path is empty this container is returned. Parent
	 * references in the supplied path are discarded if they go above the workspace
	 * root.
	 * <p>
	 * Note that no attempt is made to exclude team-private member resources
	 * as with <code>members</code>.
	 * </p><p>
	 * N.B. Unlike the methods which traffic strictly in resource
	 * handles, this method infers the resulting resource's type from the
	 * resource existing at the calculated path in the workspace.
	 * </p><p>
	 * Note that <code>path</code> contains a relative path to the resource
	 * and all path special characters will be interpreted. Passing an empty string
	 * will result in returning this {@link IContainer} itself.
	 * </p>
	 *
	 * @param path the relative path to the member resource, must be a valid path or path segment
	 * @return the member resource, or <code>null</code> if no such
	 * 		resource exists
	 * @see #members()
	 * @see IPath#isValidPath(String)
	 * @see IPath#isValidSegment(String)
	 */
	public IResource findMember(String path);

	/**
	 * Finds and returns the member resource identified by the given path in
	 * this container, or <code>null</code> if no such resource exists.
	 * The supplied path may be absolute or relative; in either case, it is
	 * interpreted as relative to this resource. Trailing separators and the path's
	 * device are ignored. If the path is empty this container is returned. Parent
	 * references in the supplied path are discarded if they go above the workspace
	 * root.
	 * <p>
	 * If the <code>includePhantoms</code> argument is <code>false</code>, 
	 * only a member resource with the given path that exists will be returned.
	 * If the <code>includePhantoms</code> argument is <code>true</code>,
	 * the method also returns a resource if the workspace is keeping track of a
	 * phantom with that path.
	 * </p><p>
	 * Note that no attempt is made to exclude team-private member resources
	 * as with <code>members</code>.
	 * </p><p>
	 * N.B. Unlike the methods which traffic strictly in resource
	 * handles, this method infers the resulting resource's type from the
	 * resource (or phantom) existing at the calculated path in the workspace.
	 * </p><p>
	 * Note that <code>path</code> contains a relative path to the resource
	 * and all path special characters will be interpreted. Passing an empty string
	 * will result in returning this {@link IContainer} itself.
	 * </p>
	 *
	 * @param path the relative path to the member resource, must be a valid path or path segment
	 * @param includePhantoms <code>true</code> if phantom resources are
	 *   of interest; <code>false</code> if phantom resources are not of
	 *   interest
	 * @return the member resource, or <code>null</code> if no such
	 * 		resource exists
	 * @see #members(boolean)
	 * @see IResource#isPhantom()
	 * @see IPath#isValidPath(String)
	 * @see IPath#isValidSegment(String)
	 */
	public IResource findMember(String path, boolean includePhantoms);

	/**
	 * Finds and returns the member resource identified by the given path in
	 * this container, or <code>null</code> if no such resource exists.
	 * The supplied path may be absolute or relative; in either case, it is
	 * interpreted as relative to this resource. Trailing separators and the path's
	 * device are ignored. If the path is empty this container is returned. Parent
	 * references in the supplied path are discarded if they go above the workspace
	 * root.
	 * <p>
	 * Note that no attempt is made to exclude team-private member resources
	 * as with <code>members</code>.
	 * </p><p>
	 * N.B. Unlike the methods which traffic strictly in resource
	 * handles, this method infers the resulting resource's type from the
	 * resource existing at the calculated path in the workspace.
	 * </p>
	 *
	 * @param path the path of the desired resource
	 * @return the member resource, or <code>null</code> if no such
	 * 		resource exists
	 */
	public IResource findMember(IPath path);

	/**
	 * Finds and returns the member resource identified by the given path in
	 * this container, or <code>null</code> if no such resource exists.
	 * The supplied path may be absolute or relative; in either case, it is
	 * interpreted as relative to this resource. Trailing separators and the path's
	 * device are ignored. If the path is empty this container is returned. Parent
	 * references in the supplied path are discarded if they go above the workspace
	 * root.
	 * <p>
	 * If the <code>includePhantoms</code> argument is <code>false</code>, 
	 * only a member resource with the given path that exists will be returned.
	 * If the <code>includePhantoms</code> argument is <code>true</code>,
	 * the method also returns a resource if the workspace is keeping track of a
	 * phantom with that path.
	 * </p><p>
	 * Note that no attempt is made to exclude team-private member resources
	 * as with <code>members</code>.
	 * </p><p>
	 * N.B. Unlike the methods which traffic strictly in resource
	 * handles, this method infers the resulting resource's type from the
	 * resource (or phantom) existing at the calculated path in the workspace.
	 * </p>
	 *
	 * @param path the path of the desired resource
	 * @param includePhantoms <code>true</code> if phantom resources are
	 *   of interest; <code>false</code> if phantom resources are not of
	 *   interest
	 * @return the member resource, or <code>null</code> if no such
	 * 		resource exists
	 * @see #members(boolean)
	 * @see IResource#isPhantom()
	 */
	public IResource findMember(IPath path, boolean includePhantoms);

	/**
	 * Returns the default charset for resources in this container.
	 * <p>
	 * This is a convenience method, fully equivalent to:
	 * <pre>
	 *   getDefaultCharset(true);
	 * </pre>
	 * </p><p>
	 * Note that  this method does not check whether the result is a supported
	 * charset name. Callers should be prepared to handle 
	 * <code>UnsupportedEncodingException</code> where this charset is used. 
	 * </p>
	 *
	 * @return the name of the default charset encoding for this container 
	 * @exception CoreException if this method fails
	 * @see IContainer#getDefaultCharset(boolean) 
	 * @see IFile#getCharset()
	 * @since 3.0
	 */
	public String getDefaultCharset() throws CoreException;

	/**
	 * Returns the default charset for resources in this container.
	 * <p>
	 * If checkImplicit is <code>false</code>, this method 
	 * will return the charset defined by calling #setDefaultCharset, provided this 
	 * container exists, or <code>null</code> otherwise.
	 * </p><p>
	 * If checkImplicit is <code>true</code>, this method uses the following 
	 * algorithm to determine the charset to be returned:
	 * <ol>
	 * <li>the one explicitly set by calling #setDefaultCharset 
	 * (with a non-null argument) on this container, if any, and this container 
	 * exists, or</li>  
	 * <li>the parent's default charset, if this container has a parent (is not the 
	 * workspace root), or</li>
	 * <li>the charset returned by ResourcesPlugin#getEncoding.</li> 
	 * </ol>
	 *  </p><p>
	 * Note that  this method does not check whether the result is a supported
	 * charset name. Callers should be prepared to handle 
	 * <code>UnsupportedEncodingException</code> where this charset is used. 
	 * </p>
	 * @return the name of the default charset encoding for this container,
	 * or <code>null</code> 
	 * @exception CoreException if this method fails
	 * @see IFile#getCharset()
	 * @since 3.0
	 */
	public String getDefaultCharset(boolean checkImplicit) throws CoreException;

	/**
	 * Returns a handle to the file identified by the given path in this
	 * container.
	 * <p> 
	 * This is a resource handle operation; neither the resource nor
	 * the result need exist in the workspace.
	 * The validation check on the resource name/path is not done
	 * when the resource handle is constructed; rather, it is done
	 * automatically as the resource is created.
	 * <p>
	 * The supplied path may be absolute or relative; in either case, it is
	 * interpreted as relative to this resource and is appended
	 * to this container's full path to form the full path of the resultant resource.
	 * A trailing separator is ignored. The path of the resulting resource must 
	 * have at least two segments.
	 * </p>
	 *
	 * @param path the path of the member file
	 * @return the (handle of the) member file
	 * @see #getFolder(IPath)
	 */
	public IFile getFile(IPath path);

	/**
	 * Returns a handle to the folder identified by the given path in this
	 * container.
	 * <p> 
	 * This is a resource handle operation; neither the resource nor
	 * the result need exist in the workspace.
	 * The validation check on the resource name/path is not done
	 * when the resource handle is constructed; rather, it is done
	 * automatically as the resource is created. 
	 * <p>
	 * The supplied path may be absolute or relative; in either case, it is
	 * interpreted as relative to this resource and is appended
	 * to this container's full path to form the full path of the resultant resource.
	 * A trailing separator is ignored. The path of the resulting resource must
	 * have at least two segments.
	 * </p>
	 *
	 * @param path the path of the member folder
	 * @return the (handle of the) member folder
	 * @see #getFile(IPath)
	 */
	public IFolder getFolder(IPath path);

	/**
	 * Returns a list of existing member resources (projects, folders and files)
	 * in this resource, in no particular order.
	 * <p>
	 * This is a convenience method, fully equivalent to <code>members(IResource.NONE)</code>.
	 * Team-private member resources are <b>not</b> included in the result.
	 * </p><p>
	 * Note that the members of a project or folder are the files and folders
	 * immediately contained within it.  The members of the workspace root
	 * are the projects in the workspace.
	 * </p>
	 *
	 * @return an array of members of this resource
	 * @exception CoreException if this request fails. Reasons include:
	 * <ul>
	 * <li> This resource does not exist.</li>
	 * <li> This resource is a project that is not open.</li>
	 * </ul>
	 * @see #findMember(IPath)
	 * @see IResource#isAccessible()
	 */
	public IResource[] members() throws CoreException;

	/**
	 * Returns a list of all member resources (projects, folders and files)
	 * in this resource, in no particular order.
	 * <p>
	 * This is a convenience method, fully equivalent to:
	 * <pre>
	 *   members(includePhantoms ? INCLUDE_PHANTOMS : IResource.NONE);
	 * </pre>
	 * Team-private member resources are <b>not</b> included in the result.
	 * </p>
	 *
	 * @param includePhantoms <code>true</code> if phantom resources are
	 *   of interest; <code>false</code> if phantom resources are not of
	 *   interest
	 * @return an array of members of this resource
	 * @exception CoreException if this request fails. Reasons include:
	 * <ul>
	 * <li> This resource does not exist.</li>
	 * <li> <code>includePhantoms</code> is <code>false</code> and
	 *     this resource does not exist.</li>
	 * <li> <code>includePhantoms</code> is <code>false</code> and
	 *     this resource is a project that is not open.</li>
	 * </ul>
	 * @see #members(int)
	 * @see IResource#exists()
	 * @see IResource#isPhantom()
	 */
	public IResource[] members(boolean includePhantoms) throws CoreException;

	/**
	 * Returns a list of all member resources (projects, folders and files)
	 * in this resource, in no particular order.
	 * <p>
	 * If the <code>INCLUDE_PHANTOMS</code> flag is not specified in the member 
	 * flags (recommended), only member resources that exist will be returned.
	 * If the <code>INCLUDE_PHANTOMS</code> flag is specified,
	 * the result will also include any phantom member resources the
	 * workspace is keeping track of.
	 * </p><p>
	 * If the <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> flag is specified 
	 * in the member flags, team private members will be included along with
	 * the others. If the <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> flag
	 * is not specified (recommended), the result will omit any team private
	 * member resources.
	 * </p><p>
	 * If the {@link #INCLUDE_HIDDEN} flag is specified in the member flags, hidden 
	 * members will be included along with the others. If the {@link #INCLUDE_HIDDEN} flag
	 * is not specified (recommended), the result will omit any hidden
	 * member resources.
	 * </p>
	 * <p>
	 * If the <code>EXCLUDE_DERIVED</code> flag is not specified, derived 
	 * resources are included. If the <code>EXCLUDE_DERIVED</code> flag is 
	 * specified in the member flags, derived resources are not included.
	 * </p>
	 *
	 * @param memberFlags bit-wise or of member flag constants
	 *   ({@link #INCLUDE_PHANTOMS}, {@link #INCLUDE_TEAM_PRIVATE_MEMBERS},
	 *   {@link #INCLUDE_HIDDEN} and {@link #EXCLUDE_DERIVED}) indicating which members are of interest
	 * @return an array of members of this resource
	 * @exception CoreException if this request fails. Reasons include:
	 * <ul>
	 * <li> This resource does not exist.</li>
	 * <li> the <code>INCLUDE_PHANTOMS</code> flag is not specified and
	 *     this resource does not exist.</li>
	 * <li> the <code>INCLUDE_PHANTOMS</code> flag is not specified and
	 *     this resource is a project that is not open.</li>
	 * </ul>
	 * @see IResource#exists()
	 * @since 2.0
	 */
	public IResource[] members(int memberFlags) throws CoreException;

	/**
	 * Returns a list of recently deleted files inside this container that
	 * have one or more saved states in the local history. The depth parameter
	 * determines how deep inside the container to look. This resource may or
	 * may not exist in the workspace.
	 * <p>
	 * When applied to an existing project resource, this method returns recently 
	 * deleted files with saved states in that project. Note that local history is
	 * maintained with each individual project, and gets discarded when a project
	 * is deleted from the workspace. If applied to a deleted project, this method
	 * returns the empty list.
	 * </p><p>
	 * When applied to the workspace root resource (depth infinity), this method
	 * returns all recently deleted files with saved states in all existing projects.
	 * </p><p>
	 * When applied to a folder (or project) resource (depth one),
	 * this method returns all recently deleted member files with saved states.
	 * </p><p>
	 * When applied to a folder resource (depth zero),
	 * this method returns an empty list unless there was a recently deleted file
	 * with saved states at the same path as the folder.
	 * </p><p>
	 * This method is long-running; progress and cancellation are provided
	 * by the given progress monitor. 
	 * </p>
	 * 
	 * @param depth depth limit: one of <code>DEPTH_ZERO</code>, <code>DEPTH_ONE</code>
	 *    or <code>DEPTH_INFINITE</code>
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return an array of recently deleted files
	 * @exception CoreException if this method fails
	 * @see IFile#getHistory(IProgressMonitor)
	 * @since 2.0
	 */
	public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor) throws CoreException;

	/**
	 * Sets the default charset for this container. Passing a value of <code>null</code>
	 * will remove the default charset setting for this resource.
	 *
	 * @param charset a charset string, or <code>null</code>
	 * @exception CoreException if this method fails Reasons include:
	 * <ul>
	 * <li> This resource does not exist.</li>
	 * <li> An error happened while persisting this setting.</li>
	 * </ul>
	 * @see IContainer#getDefaultCharset()
	 * @since 3.0
	 * @deprecated Replaced by {@link #setDefaultCharset(String, IProgressMonitor)} which 
	 * 	is a workspace operation and reports changes in resource deltas. 
	 */
	@Deprecated
	public void setDefaultCharset(String charset) throws CoreException;

	/**
	 * Sets the default charset for this container. Passing a value of <code>null</code>
	 * will remove the default charset setting for this resource.
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that the encoding of affected resources has been changed.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided
	 * by the given progress monitor. 
	 * </p>
	 *
	 * @param charset a charset string, or <code>null</code>
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting is not desired
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * @exception CoreException if this method fails Reasons include:
	 * <ul>
	 * <li> This resource is not accessible.</li>
	 * <li> An error happened while persisting this setting.</li>
	 * <li> Resource changes are disallowed during certain types of resource change 
	 *       event notification. See {@link IResourceChangeEvent} for more details.</li>
	 * </ul>
	 * @see IContainer#getDefaultCharset()
	 * @see IResourceRuleFactory#charsetRule(IResource)
	 * @since 3.0
	 */
	public void setDefaultCharset(String charset, IProgressMonitor monitor) throws CoreException;

	/**
	 * Adds a new filter to this container. Filters restrict the set of files and directories
	 * in the underlying file system that will be included as members of this container.
	 * <p>
	 * The {@link IResource#BACKGROUND_REFRESH} update flag controls when
	 * changes to the resource hierarchy under this container resulting from the new
	 * filter take effect. If this flag is specified, the resource hierarchy is updated in a 
	 * separate thread after this method returns. If the flag is not specified, any resource 
	 * changes resulting from the new filter  will occur before this method returns.
	 * </p>
	 * <p> 
	 * This operation changes resources; these changes will be reported
	 * in a subsequent resource change event that will include an indication of any 
	 * resources that have been removed as a result of the new filter.
	 * </p>
	 * <p>
	 * This operation is long-running; progress and cancellation are provided
	 * by the given progress monitor. 
	 * </p>
	 * 
	 * @param type ({@link IResourceFilterDescription#INCLUDE_ONLY} or 
	 * {@link IResourceFilterDescription#EXCLUDE_ALL} and/or {@link IResourceFilterDescription#INHERITABLE})
	 * @param matcherDescription the description of the matcher that will determine
	 * which {@link IFileInfo} instances will be excluded from the resource tree
	 * @param updateFlags bit-wise or of update flag constants
	 *   ({@link IResource#BACKGROUND_REFRESH})
	 * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
	 * @return the description of the added filter
	 * @exception CoreException if this filter could not be added. Reasons include:
	 * <ul>
	 * <li> Resource changes are disallowed during certain types of resource change 
	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
	 * </ul>
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * @see #getFilters()
	 * @see IResourceFilterDescription#delete(int, IProgressMonitor)
	 * 
	 * @since 3.6
	 */
	public IResourceFilterDescription createFilter(int type, FileInfoMatcherDescription matcherDescription, int updateFlags, IProgressMonitor monitor) throws CoreException;

	/**
	 * Retrieve all filters on this container.
	 * If no filters exist for this resource, an empty array is returned.
	 * 
	 * @return an array of filters
	 * @exception CoreException if this resource's filters could not be retrieved. Reasons include:
	 * <ul>
	 * <li> This resource is not a folder.</li>
	 * 
	 * @see #createFilter(int, FileInfoMatcherDescription, int, IProgressMonitor)
	 * @see IResourceFilterDescription#delete(int, IProgressMonitor)
	 * @since 3.6
	 */
	public IResourceFilterDescription[] getFilters() throws CoreException;
}
