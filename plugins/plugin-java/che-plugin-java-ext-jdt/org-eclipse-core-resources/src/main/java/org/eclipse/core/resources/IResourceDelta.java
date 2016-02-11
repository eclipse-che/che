/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.internal.watson.IElementComparator;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.*;

/**
 * A resource delta represents changes in the state of a resource tree
 * between two discrete points in time.
 * <p>
 * Resource deltas implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see IResource
 * @see Platform#getAdapterManager()
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IResourceDelta extends IAdaptable {

	/*====================================================================
	 * Constants defining resource delta kinds:
	 *====================================================================*/

	/**
	 * Delta kind constant indicating that the resource has not been changed in any way.
	 * 
	 * @see IResourceDelta#getKind()
	 */
	public static final int NO_CHANGE = IElementComparator.K_NO_CHANGE;

	/**
	 * Delta kind constant (bit mask) indicating that the resource has been added
	 * to its parent. That is, one that appears in the "after" state,
	 * not in the "before" one.
	 * 
	 * @see IResourceDelta#getKind()
	 */
	public static final int ADDED = 0x1;

	/**
	 * Delta kind constant (bit mask) indicating that the resource has been removed
	 * from its parent. That is, one that appears in the "before" state,
	 * not in the "after" one. 
	 * 
	 * @see IResourceDelta#getKind()
	 */
	public static final int REMOVED = 0x2;

	/**
	 * Delta kind constant (bit mask) indicating that the resource has been changed. 
	 * That is, one that appears in both the "before" and "after" states.
	 * 
	 * @see IResourceDelta#getKind()
	 */
	public static final int CHANGED = 0x4;

	/**
	 * Delta kind constant (bit mask) indicating that a phantom resource has been added at
	 * the location of the delta node. 
	 * 
	 * @see IResourceDelta#getKind()
	 */
	public static final int ADDED_PHANTOM = 0x8;

	/**
	 * Delta kind constant (bit mask) indicating that a phantom resource has been removed from 
	 * the location of the delta node. 
	 * 
	 * @see IResourceDelta#getKind()
	 */
	public static final int REMOVED_PHANTOM = 0x10;

	/**
	 * The bit mask which describes all possible delta kinds,
	 * including ones involving phantoms.
	 * 
	 * @see IResourceDelta#getKind()
	 */
	public static final int ALL_WITH_PHANTOMS = CHANGED | ADDED | REMOVED | ADDED_PHANTOM | REMOVED_PHANTOM;

	/*====================================================================
	 * Constants which describe resource changes:
	 *====================================================================*/

	/**
	 * Change constant (bit mask) indicating that the content of the resource has changed.
	 * 
	 * @see IResourceDelta#getFlags() 
	 */
	public static final int CONTENT = 0x100;

	/**
	 * Change constant (bit mask) indicating that the resource was moved from another location.
	 * The location in the "before" state can be retrieved using <code>getMovedFromPath()</code>.
	 * 
	 * @see IResourceDelta#getFlags()
	 */
	public static final int MOVED_FROM = 0x1000;

	/**
	 * Change constant (bit mask) indicating that the resource was moved to another location.
	 * The location in the new state can be retrieved using <code>getMovedToPath()</code>.
	 * 
	 * @see IResourceDelta#getFlags()
	 */
	public static final int MOVED_TO = 0x2000;

	/**
	 * Change constant (bit mask) indicating that the resource was copied from another location.
	 * The location in the "before" state can be retrieved using <code>getMovedFromPath()</code>.
	 * This flag is only used when describing potential changes using an {@link IResourceChangeDescriptionFactory}.
	 * 
	 * @see IResourceDelta#getFlags()
	 * @since 3.2
	 */
	public static final int COPIED_FROM = 0x800;
	/**
	 * Change constant (bit mask) indicating that the resource was opened or closed.
	 * This flag is also set when the project did not exist in the "before" state.
	 * For example, if the current state of the resource is open then it was previously closed
	 * or did not exist.
	 * 
	 * @see IResourceDelta#getFlags()
	 */
	public static final int OPEN = 0x4000;

	/**
	 * Change constant (bit mask) indicating that the type of the resource has changed.
	 * 
	 * @see IResourceDelta#getFlags()
	 */
	public static final int TYPE = 0x8000;

	/**
	 * Change constant (bit mask) indicating that the resource's sync status has changed.
	 * This type of change is not included in build deltas, only in those for resource notification.
	 * 
	 * @see IResourceDelta#getFlags()
	 */
	public static final int SYNC = 0x10000;

	/**
	 * Change constant (bit mask) indicating that the resource's markers have changed.
	 * This type of change is not included in build deltas, only in those for resource notification.
	 * 
	 * @see IResourceDelta#getFlags()
	 */
	public static final int MARKERS = 0x20000;

	/**
	 * Change constant (bit mask) indicating that the resource has been
	 * replaced by another at the same location (i.e., the resource has 
	 * been deleted and then added). 
	 * 
	 * @see IResourceDelta#getFlags()
	 */
	public static final int REPLACED = 0x40000;

	/**
	 * Change constant (bit mask) indicating that a project's description has changed. 
	 * 
	 * @see IResourceDelta#getFlags()
	 */
	public static final int DESCRIPTION = 0x80000;

	/**
	 * Change constant (bit mask) indicating that the encoding of the resource has changed.
	 * 
	 * @see IResourceDelta#getFlags() 
	 * @since 3.0
	 */
	public static final int ENCODING = 0x100000;
	
	/**
	 * Change constant (bit mask) indicating that the underlying file or folder of the linked resource has been added or removed.
	 * 
	 * @see IResourceDelta#getFlags() 
	 * @since 3.4
	 */
	public static final int LOCAL_CHANGED = 0x200000;

	/**
	 * Change constant (bit mask) indicating that the derived flag of the resource has changed.
	 * 
	 * @see IResourceDelta#getFlags()
	 * @since 3.6
	 */
	public static final int DERIVED_CHANGED = 0x400000;

	/**
	 * Accepts the given visitor.
	 * The only kinds of resource deltas visited 
	 * are <code>ADDED</code>, <code>REMOVED</code>, 
	 * and <code>CHANGED</code>.
	 * The visitor's <code>visit</code> method is called with this
	 * resource delta if applicable. If the visitor returns <code>true</code>,
	 * the resource delta's children are also visited.
	 * <p>
	 * This is a convenience method, fully equivalent to 
	 * <code>accept(visitor, IResource.NONE)</code>.
	 * Although the visitor will be invoked for this resource delta, it will not be
	 * invoked for any team-private member resources.
	 * </p>
	 *
	 * @param visitor the visitor
	 * @exception CoreException if the visitor failed with this exception.
	 * @see IResourceDeltaVisitor#visit(IResourceDelta)
	 */
	public void accept(IResourceDeltaVisitor visitor) throws CoreException;

	/** 
	 * Accepts the given visitor.
	 * The visitor's <code>visit</code> method is called with this
	 * resource delta. If the visitor returns <code>true</code>,
	 * the resource delta's children are also visited.
	 * <p>
	 * This is a convenience method, fully equivalent to:
	 * <pre>
	 *   accept(visitor, includePhantoms ? INCLUDE_PHANTOMS : IResource.NONE);
	 * </pre>
	 * Although the visitor will be invoked for this resource delta, it will not be
	 * invoked for any team-private member resources.
	 * </p>
	 *
	 * @param visitor the visitor
	 * @param includePhantoms <code>true</code> if phantom resources are
	 *   of interest; <code>false</code> if phantom resources are not of
	 *   interest
	 * @exception CoreException if the visitor failed with this exception.
	 * @see #accept(IResourceDeltaVisitor)
	 * @see IResource#isPhantom()
	 * @see IResourceDeltaVisitor#visit(IResourceDelta)
	 */
	public void accept(IResourceDeltaVisitor visitor, boolean includePhantoms) throws CoreException;

	/** 
	 * Accepts the given visitor.
	 * The visitor's <code>visit</code> method is called with this
	 * resource delta. If the visitor returns <code>true</code>,
	 * the resource delta's children are also visited.
	 * <p>
	 * The member flags determine which child deltas of this resource delta will be visited.
	 * The visitor will always be invoked for this resource delta.
	 * <p>
	 * If the <code>INCLUDE_PHANTOMS</code> member flag is not specified
	 * (recommended), only child resource deltas involving existing resources will be visited
	 * (kinds <code>ADDED</code>, <code>REMOVED</code>, and <code>CHANGED</code>).
	 * If the <code>INCLUDE_PHANTOMS</code> member flag is specified,
	 * the result will also include additions and removes of phantom resources
	 * (kinds <code>ADDED_PHANTOM</code> and <code>REMOVED_PHANTOM</code>).
	 * </p>
	 * <p>
	 * If the <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> member flag is not specified
	 * (recommended), resource deltas involving team private member resources will be 
	 * excluded from the visit. If the <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> member
	 * flag is specified, the visit will also include additions and removes of
	 * team private member resources.
	 * </p>
	 *
	 * @param visitor the visitor
	 * @param memberFlags bit-wise or of member flag constants
	 *   (<code>IContainer.INCLUDE_PHANTOMS</code>, <code>INCLUDE_HIDDEN</code> 
	 *   and <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code>) indicating which members are of interest
	 * @exception CoreException if the visitor failed with this exception.
	 * @see IResource#isPhantom()
	 * @see IResource#isTeamPrivateMember()
	 * @see IResource#isHidden()
	 * @see IContainer#INCLUDE_PHANTOMS
	 * @see IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS
	 * @see IContainer#INCLUDE_HIDDEN
	 * @see IResourceDeltaVisitor#visit(IResourceDelta)
	 * @since 2.0
	 */
	public void accept(IResourceDeltaVisitor visitor, int memberFlags) throws CoreException;

	/**
	 * Finds and returns the descendent delta identified by the given path in
	 * this delta, or <code>null</code> if no such descendent exists.
	 * The supplied path may be absolute or relative; in either case, it is
	 * interpreted as relative to this delta.   Trailing separators are ignored.
	 * If the path is empty this delta is returned.
	 * <p>
	 * This is a convenience method to avoid manual traversal of the delta
	 * tree in cases where the listener is only interested in changes to
	 * particular resources.  Calling this method will generally be
	 * faster than manually traversing the delta to a particular descendent.
	 * </p>
	 * @param path the path of the desired descendent delta
	 * @return the descendent delta, or <code>null</code> if no such
	 * 		descendent exists in the delta
	 * @since 2.0
	 */
	public IResourceDelta findMember(IPath path);

	/**
	 * Returns resource deltas for all children of this resource 
	 * which were added, removed, or changed. Returns an empty
	 * array if there are no affected children.
	 * <p>
	 * This is a convenience method, fully equivalent to:
	 * <pre>
	 *   getAffectedChildren(ADDED | REMOVED | CHANGED, IResource.NONE);
	 * </pre>
	 * Team-private member resources are <b>not</b> included in the result; neither are
	 * phantom resources.
	 * </p>
	 *
	 * @return the resource deltas for all affected children
	 * @see IResourceDelta#ADDED
	 * @see IResourceDelta#REMOVED
	 * @see IResourceDelta#CHANGED
	 * @see #getAffectedChildren(int,int)
	 */
	public IResourceDelta[] getAffectedChildren();

	/**
	 * Returns resource deltas for all children of this resource 
	 * whose kind is included in the given mask. Kind masks are formed
	 * by the bitwise or of <code>IResourceDelta</code> kind constants.
	 * Returns an empty array if there are no affected children.
	 * <p>
	 * This is a convenience method, fully equivalent to:
	 * <pre>
	 *   getAffectedChildren(kindMask, IResource.NONE);
	 * </pre>
	 * Team-private member resources are <b>not</b> included in the result.
	 * </p>
	 *
	 * @param kindMask a mask formed by the bitwise or of <code>IResourceDelta </code> 
	 *    delta kind constants
	 * @return the resource deltas for all affected children
	 * @see IResourceDelta#ADDED
	 * @see IResourceDelta#REMOVED
	 * @see IResourceDelta#CHANGED
	 * @see IResourceDelta#ADDED_PHANTOM
	 * @see IResourceDelta#REMOVED_PHANTOM
	 * @see IResourceDelta#ALL_WITH_PHANTOMS
	 * @see #getAffectedChildren(int,int)
	 */
	public IResourceDelta[] getAffectedChildren(int kindMask);

	/**
	 * Returns resource deltas for all children of this resource 
	 * whose kind is included in the given mask. Masks are formed
	 * by the bitwise or of <code>IResourceDelta</code> kind constants.
	 * Returns an empty array if there are no affected children.
	 * <p>
	 * If the <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> member flag is not specified,
	 * (recommended), resource deltas involving team private member resources will be 
	 * excluded. If the <code>INCLUDE_TEAM_PRIVATE_MEMBERS</code> member
	 * flag is specified, the result will also include resource deltas of the 
	 * specified kinds to team private member resources.
	 * </p>
	 * <p>
	 * If the {@link IContainer#INCLUDE_HIDDEN} member flag is not specified,
	 * (recommended), resource deltas involving hidden resources will be 
	 * excluded. If the {@link IContainer#INCLUDE_HIDDEN} member
	 * flag is specified, the result will also include resource deltas of the 
	 * specified kinds to hidden resources.
	 * </p>
	 * <p>
	 * Specifying the <code>IContainer.INCLUDE_PHANTOMS</code> member flag is equivalent
	 * to including <code>IContainer.ADDED_PHANTOM</code> and <code>IContainer.REMOVED_PHANTOM</code>
	 * in the kind mask.
	 * </p>
	 *
	 * @param kindMask a mask formed by the bitwise or of <code>IResourceDelta</code> 
	 *    delta kind constants
	 * @param memberFlags bit-wise or of member flag constants
	 *   (<code>IContainer.INCLUDE_PHANTOMS</code>, <code>IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS</code> 
	 *   and <code>IContainer.INCLUDE_HIDDEN</code>)
	 *   indicating which members are of interest
	 * @return the resource deltas for all affected children
	 * @see IResourceDelta#ADDED
	 * @see IResourceDelta#REMOVED
	 * @see IResourceDelta#CHANGED
	 * @see IResourceDelta#ADDED_PHANTOM
	 * @see IResourceDelta#REMOVED_PHANTOM
	 * @see IResourceDelta#ALL_WITH_PHANTOMS
	 * @see IContainer#INCLUDE_PHANTOMS
	 * @see IContainer#INCLUDE_TEAM_PRIVATE_MEMBERS
	 * @see IContainer#INCLUDE_HIDDEN
	 * @since 2.0
	 */
	public IResourceDelta[] getAffectedChildren(int kindMask, int memberFlags);

	/**
	 * Returns flags which describe in more detail how a resource has been affected.
	 * <p>
	 * The following codes (bit masks) are used when kind is <code>CHANGED</code>, and
	 * also when the resource is involved in a move:
	 * <ul>
	 * <li><code>CONTENT</code> - The bytes contained by the resource have 
	 * 		been altered, or <code>IResource.touch</code> has been called on 
	 * 		the resource.</li>
	 * <li><code>DERIVED_CHANGED</code> - The derived flag of the resource has
	 * been altered.</li>
	 * <li><code>ENCODING</code> - The encoding of the resource may have been altered.
	 * This flag is not set when the encoding changes due to the file being modified, 
	 * or being moved.</li>
	 * <li><code>DESCRIPTION</code> - The description of the project has been altered,
	 * 		or <code>IResource.touch</code> has been called on the project.
	 * 		This flag is only valid for project resources.</li>
	 * <li><code>OPEN</code> - The project's open/closed state has changed.
	 * 		If it is not open, it was closed, and vice versa.  This flag is only valid for project resources.</li>
	 * <li><code>TYPE</code> - The resource (a folder or file) has changed its type.</li>
	 * <li><code>SYNC</code> - The resource's sync status has changed.</li>
	 * <li><code>MARKERS</code> - The resource's markers have changed.</li>
	 * <li><code>REPLACED</code> - The resource (and all its properties)
	 *  was deleted (either by a delete or move), and was subsequently re-created
	 *  (either by a create, move, or copy).</li>
	 *  <li><code>LOCAL_CHANGED</code> - The resource is a linked resource,
	 *  and the underlying file system object has been added or removed.</li>
	 * </ul>
	 * The following code is only used if kind is <code>REMOVED</code>
	 * (or <code>CHANGED</code> in conjunction with <code>REPLACED</code>):
	 * <ul>
	 * <li><code>MOVED_TO</code> - The resource has moved.
	 * 	<code>getMovedToPath</code> will return the path of where it was moved to.</li>
	 * </ul>
	 * The following code is only used if kind is <code>ADDED</code>
	 * (or <code>CHANGED</code> in conjunction with <code>REPLACED</code>):
	 * <ul>
	 * <li><code>MOVED_FROM</code> - The resource has moved.
	 * 	<code>getMovedFromPath</code> will return the path of where it was moved from.</li>
	 * </ul>
	 * The following code is only used when describing potential changes using an {@link IResourceChangeDescriptionFactory}:
	 * <ul>
	 * <li><code>COPIED_FROM</code> - Change constant (bit mask) indicating that the resource was copied from another location.
	 * The location in the "before" state can be retrieved using <code>getMovedFromPath()</code>.</li>
	 * </ul>
	 * 
	 * A simple move operation would result in the following delta information.
	 * If a resource is moved from A to B (with no other changes to A or B), 
	 * then A will have kind <code>REMOVED</code>, with flag <code>MOVED_TO</code>, 
	 * and <code>getMovedToPath</code> on A will return the path for B.  
	 * B will have kind <code>ADDED</code>, with flag <code>MOVED_FROM</code>, 
	 * and <code>getMovedFromPath</code> on B will return the path for A.
	 * B's other flags will describe any other changes to the resource, as compared
	 * to its previous location at A.
	 * </p>
	 * <p>
	 * Note that the move flags only describe the changes to a single resource; they
	 * don't necessarily imply anything about the parent or children of the resource.  
	 * If the children were moved as a consequence of a subtree move operation, 
	 * they will have corresponding move flags as well.
	 * </p>
	 * <p>
	 * Note that it is possible for a file resource to be replaced in the workspace
	 * by a folder resource (or the other way around).
	 * The resource delta, which is actually expressed in terms of
	 * paths instead or resources, shows this as a change to either the
	 * content or children.
	 * </p>
	 *
	 * @return the flags
	 * @see IResourceDelta#CONTENT
	 * @see IResourceDelta#DERIVED_CHANGED
	 * @see IResourceDelta#DESCRIPTION
	 * @see IResourceDelta#ENCODING
	 * @see IResourceDelta#LOCAL_CHANGED
	 * @see IResourceDelta#OPEN
	 * @see IResourceDelta#MOVED_TO
	 * @see IResourceDelta#MOVED_FROM
	 * @see IResourceDelta#COPIED_FROM
	 * @see IResourceDelta#TYPE
	 * @see IResourceDelta#SYNC
	 * @see IResourceDelta#MARKERS
	 * @see IResourceDelta#REPLACED
	 * @see #getKind()
	 * @see #getMovedFromPath()
	 * @see #getMovedToPath()
	 * @see IResource#move(IPath, int, IProgressMonitor)
	 */
	public int getFlags();

	/**
	 * Returns the full, absolute path of this resource delta.
	 * <p>
	 * Note: the returned path never has a trailing separator.
	 * </p>
	 * @return the full, absolute path of this resource delta
	 * @see IResource#getFullPath()
	 * @see #getProjectRelativePath()
	 */
	public IPath getFullPath();

	/**
	 * Returns the kind of this resource delta.
	 * Normally, one of <code>ADDED</code>, 
	 * <code>REMOVED</code>, <code>CHANGED</code>.
	 * When phantom resources have been explicitly requested,
	 * there are two additional kinds: <code>ADDED_PHANTOM</code> 
	 * and <code>REMOVED_PHANTOM</code>.
	 *
	 * @return the kind of this resource delta
	 * @see IResourceDelta#ADDED
	 * @see IResourceDelta#REMOVED
	 * @see IResourceDelta#CHANGED
	 * @see IResourceDelta#ADDED_PHANTOM
	 * @see IResourceDelta#REMOVED_PHANTOM
	 */
	public int getKind();

	/**
	 * Returns the changes to markers on the corresponding resource.
	 * Returns an empty array if no markers changed.
	 *
	 * @return the marker deltas
	 */
	public IMarkerDelta[] getMarkerDeltas();

	/**
	 * Returns the full path (in the "before" state) from which this resource 
	 * (in the "after" state) was moved.  This value is only valid 
	 * if the <code>MOVED_FROM</code> change flag is set; otherwise,
	 * <code>null</code> is returned.
	 * <p>
	 * Note: the returned path never has a trailing separator.
	 *
	 * @return a path, or <code>null</code>
	 * @see #getMovedToPath()
	 * @see #getFullPath()
	 * @see #getFlags()
	 */
	public IPath getMovedFromPath();

	/**
	 * Returns the full path (in the "after" state) to which this resource 
	 * (in the "before" state) was moved.  This value is only valid if the 
	 * <code>MOVED_TO</code> change flag is set; otherwise,
	 * <code>null</code> is returned.
	 * <p>
	 * Note: the returned path never has a trailing separator.
	 * 
	 * @return a path, or <code>null</code>
	 * @see #getMovedFromPath()
	 * @see #getFullPath()
	 * @see #getFlags()
	 */
	public IPath getMovedToPath();

	/**
	 * Returns the project-relative path of this resource delta.
	 * Returns the empty path for projects and the workspace root.
	 * <p>
	 * A resource's project-relative path indicates the route from the project
	 * to the resource.  Within a workspace, there is exactly one such path
	 * for any given resource. The returned path never has a trailing separator.
	 * </p>
	 * @return the project-relative path of this resource delta
	 * @see IResource#getProjectRelativePath()
	 * @see #getFullPath()
	 * @see Path#EMPTY
	 */
	public IPath getProjectRelativePath();

	/**
	 * Returns a handle for the affected resource.
	 * <p> 
	 * For additions (<code>ADDED</code>), this handle describes the newly-added resource; i.e.,
	 * the one in the "after" state.
	 * <p> 
	 * For changes (<code>CHANGED</code>), this handle also describes the resource in the "after"
	 * state. When a file or folder resource has changed type, the
	 * former type of the handle can be inferred.
	 * <p>
	 * For removals (<code>REMOVED</code>), this handle describes the resource in the "before" 
	 * state. Even though this resource would not normally exist in the
	 * current workspace, the type of resource that was removed can be
	 * determined from the handle.
	 * <p> 
	 * For phantom additions and removals (<code>ADDED_PHANTOM</code>
	 * and <code>REMOVED_PHANTOM</code>), this is the handle of the phantom resource.
	 *
	 * @return the affected resource (handle)
	 */
	public IResource getResource();
}
