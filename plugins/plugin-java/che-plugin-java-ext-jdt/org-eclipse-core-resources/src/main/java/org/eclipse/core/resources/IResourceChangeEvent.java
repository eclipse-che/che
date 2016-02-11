/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Resource change events describe changes to resources.
 * <p>
 * There are currently five different types of resource change events:
 * <ul>
 *   <li>
 *    Before-the-fact batch reports of arbitrary creations, 
 *    deletions and modifications to one or more resources expressed
 *    as a hierarchical resource delta. Event type is
 *    <code>PRE_BUILD</code>, and <code>getDelta</code> returns
 *    the hierarchical delta rooted at the workspace root. 
 *    The <code>getBuildKind</code> method returns
 *    the kind of build that is about to occur, and the <code>getSource</code>
 *    method returns the scope of the build (either the workspace or a single project).
 *    These events are broadcast to interested parties immediately 
 *    before each build operation. If autobuilding is not enabled, these events still 
 *    occur at times when autobuild would have occurred. The workspace is open 
 *    for change during notification of these events. The delta reported in this event 
 *    cycle is identical across all listeners registered for this type of event.
 *    Resource changes attempted during a <code>PRE_BUILD</code> callback
 *    <b>must</b> be done in the thread doing the notification.
 *   </li>
 *   <li>
 *    After-the-fact batch reports of arbitrary creations, 
 *    deletions and modifications to one or more resources expressed
 *    as a hierarchical resource delta. Event type is
 *    <code>POST_BUILD</code>, and <code>getDelta</code> returns
 *    the hierarchical delta rooted at the workspace root.
 *    The <code>getBuildKind</code> method returns
 *    the kind of build that occurred, and the <code>getSource</code>
 *    method returns the scope of the build (either the workspace or a single project).
 *    These events are broadcast to interested parties at the end of every build operation.
 *    If autobuilding is not enabled, these events still occur at times when autobuild
 *    would have occurred. The workspace is open for change during notification of 
 *    these events. The delta reported in this event cycle is identical across
 *    all listeners registered for this type of event.
 *    Resource changes attempted during a <code>POST_BUILD</code> callback
 *    <b>must</b> be done in the thread doing the notification.
 *   </li>
 *   <li>
 *    After-the-fact batch reports of arbitrary creations, 
 *    deletions and modifications to one or more resources expressed
 *    as a hierarchical resource delta. Event type is
 *    <code>POST_CHANGE</code>, and <code>getDelta</code> returns
 *    the hierarchical delta. The resource delta is rooted at the 
 *    workspace root.  These events are broadcast to interested parties after
 *    a set of resource changes and happen whether or not autobuilding is enabled.  
 *    The workspace is closed for change during notification of these events.
 *    The delta reported in this event cycle is identical across all listeners registered for 
 *    this type of event.
 *   </li>
 *   <li>
 *    Before-the-fact reports of the impending closure of a single
 *    project. Event type is <code>PRE_CLOSE</code>, 
 *    and <code>getResource</code> returns the project being closed.
 *    The workspace is closed for change during  notification of these events.
 *   </li>
 *   <li>
 *    Before-the-fact reports of the impending deletion of a single
 *    project. Event type is <code>PRE_DELETE</code>, 
 *    and <code>getResource</code> returns the project being deleted.
 *    The workspace is closed for change during  notification of these events.
 *   </li>
 *   <li>
 *    Before-the-fact reports of the impending refresh of a single project or the workspace. 
 *    Event type is <code>PRE_REFRESH</code> and the <code>getSource</code>
 *    method returns the scope of the refresh (either the workspace or a single project).
 *    If the event is fired by a project refresh the <code>getResource</code>
 *    method returns the project being refreshed.
 *    The workspace is closed for changes during notification of these events.
 *   </li>
 * </ul>
 * <p>
 * In order to handle additional event types that may be introduced
 * in future releases of the platform, clients should do not write code
 * that presumes the set of event types is closed.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IResourceChangeEvent {
	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report of creations, deletions, and modifications
	 * to one or more resources expressed as a hierarchical
	 * resource delta as returned by <code>getDelta</code>.
	 * See class comments for further details.
	 *
	 * @see #getType()
	 * @see #getDelta()
	 */
	public static final int POST_CHANGE = 1;

	/**
	 * Event type constant (bit mask) indicating a before-the-fact 
	 * report of the impending closure of a single
	 * project as returned by <code>getResource</code>.
	 * See class comments for further details.
	 *
	 * @see #getType()
	 * @see #getResource()
	 */
	public static final int PRE_CLOSE = 2;

	/**
	 * Event type constant (bit mask) indicating a before-the-fact 
	 * report of the impending deletion of a single
	 * project as returned by <code>getResource</code>.
	 * See class comments for further details.
	 *
	 * @see #getType()
	 * @see #getResource()
	 */
	public static final int PRE_DELETE = 4;

	/**
	 * @deprecated This event type has been renamed to
	 * <code>PRE_BUILD</code>
	 */
	@Deprecated
	public static final int PRE_AUTO_BUILD = 8;

	/**
	 * Event type constant (bit mask) indicating a before-the-fact 
	 * report of a build. The event contains a hierarchical resource delta
	 * as returned by <code>getDelta</code>.
	 * See class comments for further details.
	 *
	 * @see #getBuildKind()
	 * @see #getSource()
	 * @since 3.0
	 */
	public static final int PRE_BUILD = 8;

	/**
	 * @deprecated This event type has been renamed to
	 * <code>POST_BUILD</code>
	 */
	@Deprecated
	public static final int POST_AUTO_BUILD = 16;

	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report of a build. The event contains a hierarchical resource delta
	 * as returned by <code>getDelta</code>.
	 * See class comments for further details.
	 *
	 * @see #getBuildKind()
	 * @see #getSource()
	 * @since 3.0
	 */
	public static final int POST_BUILD = 16;
	
	/**
	 * Event type constant (bit mask) indicating a before-the-fact 
	 * report of refreshing the workspace or a project.
	 * See class comments for further details.
	 *
	 * @see #getType()
	 * @see #getSource()
	 * @see #getResource()
	 * @since 3.4
	 */
	public static final int PRE_REFRESH = 32;

	/**
	 * Returns all marker deltas of the specified type that are associated
	 * with resource deltas for this event. If <code>includeSubtypes</code>
	 * is <code>false</code>, only marker deltas whose type exactly matches 
	 * the given type are returned.  Returns an empty array if there 
	 * are no matching marker deltas.
	 * <p>
	 * Calling this method is equivalent to walking the entire resource
	 * delta for this event, and collecting all marker deltas of a given type.
	 * The speed of this method will be proportional to the number of changed
	 * markers, regardless of the size of the resource delta tree.
	 * </p>
	 * @param type the type of marker to consider, or <code>null</code> to indicate all types
	 * @param includeSubtypes whether or not to consider sub-types of the given type
	 * @return an array of marker deltas
	 * @since 2.0
	 */
	public IMarkerDelta[] findMarkerDeltas(String type, boolean includeSubtypes);

	/**
	 * Returns the kind of build that caused this event,
	 * or <code>0</code> if not applicable to this type of event.
	 * <p>
	 * If the event is a <code>PRE_BUILD</code> or <code>POST_BUILD</code>
	 * then this will be the kind of build that occurred to cause the event.
	 * </p>
	 *
	 * @see IProject#build(int, IProgressMonitor)
	 * @see IWorkspace#build(int, IProgressMonitor)
	 * @see IncrementalProjectBuilder#AUTO_BUILD
	 * @see IncrementalProjectBuilder#FULL_BUILD
	 * @see IncrementalProjectBuilder#INCREMENTAL_BUILD
	 * @see IncrementalProjectBuilder#CLEAN_BUILD
	 * @return the kind of build, or <code>0</code> if not applicable
	 * @since 3.1
	 */
	public int getBuildKind();

	/**
	 * Returns a resource delta, rooted at the workspace, describing the set
	 * of changes that happened to resources in the workspace. 
	 * Returns <code>null</code> if not applicable to this type of event.
	 *
	 * @return the resource delta, or <code>null</code> if not
	 *   applicable
	 */
	public IResourceDelta getDelta();

	/**
	 * Returns the resource in question or <code>null</code>
	 * if not applicable to this type of event. 
	 * <p>
	 * If the event is of type <code>PRE_CLOSE</code>,
	 * <code>PRE_DELETE</code>, or <code>PRE_REFRESH</code>, then the resource 
	 * will be the affected project. Otherwise the resource will be <code>null</code>.
	 * </p>
	 * @return the resource, or <code>null</code> if not applicable
	 */
	public IResource getResource();

	/**
	 * Returns an object identifying the source of this event.
	 * <p>
	 * If the event is a <code>PRE_BUILD</code>, <code>POST_BUILD</code>,
	 * or <code>PRE_REFRESH</code> then this will be the scope of the build 
	 * (either the {@link IWorkspace} or a single {@link IProject}).
	 * </p>
	 *
	 * @return an object identifying the source of this event 
	 * @see java.util.EventObject
	 */
	public Object getSource();
	
	/**
	 * Returns the type of event being reported.
	 *
	 * @return one of the event type constants
	 * @see #POST_CHANGE
	 * @see #POST_BUILD
	 * @see #PRE_BUILD
	 * @see #PRE_CLOSE
	 * @see #PRE_DELETE
	 * @see #PRE_REFRESH
	 */
	public int getType();
}
