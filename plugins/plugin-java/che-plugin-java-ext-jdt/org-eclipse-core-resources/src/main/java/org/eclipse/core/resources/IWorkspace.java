/*******************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Incorporated - loadProjectDescription(InputStream)
 *     Broadcom Corporation - build configurations and references
 *******************************************************************************/
package org.eclipse.core.resources;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import org.eclipse.core.resources.team.FileModificationValidationContext;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Workspaces are the basis for Eclipse Platform resource management. There is
 * only one workspace per running platform. All resources exist in the context
 * of this workspace.
 * <p>
 * A workspace corresponds closely to discreet areas in the local file system.
 * Each project in a workspace maps onto a specific area of the file system. The
 * folders and files within a project map directly onto the corresponding
 * directories and files in the file system. One sub-directory, the workspace
 * metadata area, contains internal information about the workspace and its
 * resources. This metadata area should be accessed only by the Platform or via
 * Platform API calls.
 * </p>
 * <p>
 * Workspaces add value over using the file system directly in that they allow
 * for comprehensive change tracking (through <code>IResourceDelta</code> s),
 * various forms of resource metadata (e.g., markers and properties) as well as
 * support for managing application/tool state (e.g., saving and restoring).
 * </p>
 * <p>
 * The workspace as a whole is thread safe and allows one writer concurrent with
 * multiple readers. It also supports mechanisms for saving and snapshotting the
 * current resource state.
 * </p>
 * <p>
 * The workspace is provided by the Resources plug-in and is automatically
 * created when that plug-in is activated. The default workspace data area
 * (i.e., where its resources are stored) overlap exactly with the platform's
 * data area. That is, by default, the workspace's projects are found directly
 * in the platform's data area. Individual project locations can be specified
 * explicitly.
 * </p>
 * <p>
 * The workspace resource namespace is always case-sensitive and 
 * case-preserving. Thus the workspace allows multiple sibling resources to exist
 * with names that differ only in case.  The workspace also imposes no
 * restrictions on valid characters in resource names, the length of resource names,
 * or the size of resources on disk.  In situations where one or more resources
 * are stored in a file system that is not case-sensitive, or that imposes restrictions
 * on resource names, any failure to store or retrieve those resources will
 * be propagated back to the caller of workspace API.
 * </p>
 * <p>
 * Workspaces implement the <code>IAdaptable</code> interface; extensions are
 * managed by the platform's adapter manager.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IWorkspace extends IAdaptable {
	/**
	 * flag constant (bit mask value 1) indicating that resource change
	 * notifications should be avoided during the invocation of a compound
	 * resource changing operation.
	 * 
	 * @see IWorkspace#run(IWorkspaceRunnable, ISchedulingRule, int, IProgressMonitor)
	 * @since 3.0
	 */
	public static final int AVOID_UPDATE = 1;

	/**
	 * Constant that can be passed to {@link #validateEdit(org.eclipse.core.resources.IFile[], Object)}
	 * to indicate that the caller does not have access to a UI context but would still
	 * like to have UI-based validation if possible.
	 * @since 3.3
	 * @see #validateEdit(IFile[], Object)
	 */
	public static final Object VALIDATE_PROMPT = FileModificationValidationContext.VALIDATE_PROMPT;

	/**
	 * The name of the IWorkspace OSGi service (value "org.eclipse.core.resources.IWorkspace").
	 * @since 3.5
	 */
	public static final String SERVICE_NAME = IWorkspace.class.getName();

	/**
	 * Adds the given listener for resource change events to this workspace. Has
	 * no effect if an identical listener is already registered.
	 * <p>
	 * This method is equivalent to:
	 * 
	 * <pre>
	 * addResourceChangeListener(listener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param listener the listener
	 * @see IResourceChangeListener
	 * @see IResourceChangeEvent
	 * @see #addResourceChangeListener(IResourceChangeListener, int)
	 * @see #removeResourceChangeListener(IResourceChangeListener)
	 */
	public void addResourceChangeListener(IResourceChangeListener listener);

	/**
	 * Adds the given listener for the specified resource change events to this
	 * workspace. Has no effect if an identical listener is already registered
	 * for these events. After completion of this method, the given listener
	 * will be registered for exactly the specified events. If they were
	 * previously registered for other events, they will be de-registered.
	 * <p>
	 * Once registered, a listener starts receiving notification of changes to
	 * resources in the workspace. The resource deltas in the resource change
	 * event are rooted at the workspace root. Most resource change
	 * notifications occur well after the fact; the exception is
	 * pre-notification of impending project closures and deletions. The
	 * listener continues to receive notifications until it is replaced or
	 * removed.
	 * </p>
	 * <p>
	 * Listeners can listen for several types of event as defined in
	 * <code>IResourceChangeEvent</code>. Clients are free to register for
	 * any number of event types however if they register for more than one, it
	 * is their responsibility to ensure they correctly handle the case where
	 * the same resource change shows up in multiple notifications. Clients are
	 * guaranteed to receive only the events for which they are registered.
	 * </p>
	 * 
	 * @param listener the listener
	 * @param eventMask the bit-wise OR of all event types of interest to the
	 * listener
	 * @see IResourceChangeListener
	 * @see IResourceChangeEvent
	 * @see #removeResourceChangeListener(IResourceChangeListener)
	 */
	public void addResourceChangeListener(IResourceChangeListener listener, int eventMask);

	/**
	 * Registers the given plug-in's workspace save participant, and returns an
	 * object describing the workspace state at the time of the last save in
	 * which the plug-in participated.
	 * <p>
	 * Once registered, the workspace save participant will actively participate
	 * in the saving of this workspace.
	 * </p>
	 * 
	 * @param plugin the plug-in
	 * @param participant the participant
	 * @return the last saved state in which the plug-in participated, or
	 * <code>null</code> if the plug-in has not participated before
	 * @exception CoreException if the method fails to add the participant.
	 * Reasons include:
	 * <ul>
	 * <li>The previous state could not be recovered.</li>
	 * </ul>
	 * @see ISaveParticipant
	 * @see #removeSaveParticipant(Plugin)
	 * @deprecated Use {@link #addSaveParticipant(String, ISaveParticipant)} instead
	 */
	@Deprecated
	public ISavedState addSaveParticipant(Plugin plugin, ISaveParticipant participant) throws CoreException;
	
	/**
	 * Registers the given plug-in's workspace save participant, and returns an
	 * object describing the workspace state at the time of the last save in
	 * which the bundle participated.
	 * <p>
	 * Once registered, the workspace save participant will actively participate
	 * in the saving of this workspace.
	 * </p>
	 * 
	 * @param pluginId the unique identifier of the plug-in
	 * @param participant the participant
	 * @return the last saved state in which the plug-in participated, or
	 * <code>null</code> if the plug-in has not participated before
	 * @exception CoreException if the method fails to add the participant.
	 * Reasons include:
	 * <ul>
	 * <li>The previous state could not be recovered.</li>
	 * </ul>
	 * @see ISaveParticipant
	 * @see #removeSaveParticipant(String)
	 * @since 3.6
	 */
	public ISavedState addSaveParticipant(String pluginId, ISaveParticipant participant) throws CoreException;

	/**
	 * Builds all projects in this workspace. Projects are built in the order
	 * specified in this workspace's description. Projects not mentioned in the
	 * order or for which the order cannot be determined are built in an
	 * undefined order after all other projects have been built. If no order is
	 * specified, the workspace computes an order determined by project
	 * references.
	 * <p>
	 * This method may change resources; these changes will be reported in a
	 * subsequent resource change event.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * 
	 * @param kind the kind of build being requested. Valid values are
	 *	<ul>
	 * <li>{@link IncrementalProjectBuilder#FULL_BUILD}- indicates a full build.</li>
	 * <li>{@link IncrementalProjectBuilder#INCREMENTAL_BUILD}- indicates a incremental build.</li>
	 * <li>{@link IncrementalProjectBuilder#CLEAN_BUILD}- indicates a clean request.  Clean does
	 * not actually build anything, but rather discards all problems and build states.</li>
	 *	</ul>
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @exception CoreException if the build fails.
	 *		The status contained in the exception may be a generic {@link IResourceStatus#BUILD_FAILED}
	 *		code, but it could also be any other status code; it might
	 *		also be a {@link MultiStatus}.
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * 
	 * @see IWorkspace#build(IBuildConfiguration[], int, boolean, IProgressMonitor)
	 * @see IProject#build(int, IProgressMonitor)
	 * @see #computeProjectOrder(IProject[])
	 * @see IncrementalProjectBuilder#FULL_BUILD
	 * @see IncrementalProjectBuilder#INCREMENTAL_BUILD
	 * @see IncrementalProjectBuilder#CLEAN_BUILD
	 * @see IResourceRuleFactory#buildRule()
	 */
	public void build(int kind, IProgressMonitor monitor) throws CoreException;

	/**
	 * Build the build configurations specified in the passed in build configuration array.
	 * <p>
	 * Build order is determined by the workspace description and the project build configuration 
	 * reference graph.
	 * </p>
	 * <p>
	 * If buildReferences is true, build configurations reachable through the build configuration graph are
	 * built as part of this build invocation.
	 * </p>
	 * <p>
	 * This method may change resources; these changes will be reported in a
	 * subsequent resource change event.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * @param buildConfigs array of configurations to build
	 * @param kind the kind of build being requested. Valid values are
	 *	<ul>
	 * <li>{@link IncrementalProjectBuilder#FULL_BUILD}- indicates a full build.</li>
	 * <li>{@link IncrementalProjectBuilder#INCREMENTAL_BUILD}- indicates a incremental build.</li>
	 * <li>{@link IncrementalProjectBuilder#CLEAN_BUILD}- indicates a clean request.  Clean does
	 * not actually build anything, but rather discards all problems and build states.</li>
	 *	</ul>
	 * @param buildReferences boolean indicating if references should be transitively built.
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @exception CoreException if the build fails.
	 *		The status contained in the exception may be a generic {@link IResourceStatus#BUILD_FAILED}
	 *		code, but it could also be any other status code; it might
	 *		also be a {@link MultiStatus}.
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancellation can occur even if no progress monitor is provided.
	 * 
	 * @see IProject#build(int, IProgressMonitor)
	 * @see IncrementalProjectBuilder#FULL_BUILD
	 * @see IncrementalProjectBuilder#INCREMENTAL_BUILD
	 * @see IncrementalProjectBuilder#CLEAN_BUILD
	 * @see IResourceRuleFactory#buildRule()
	 * @since 3.7
	 */
	public void build(IBuildConfiguration[] buildConfigs, int kind, boolean buildReferences, IProgressMonitor monitor) throws CoreException;

	/**
	 * Checkpoints the operation currently in progress. This method is used in
	 * the middle of a group of operations to force a background autobuild (if
	 * the build argument is true) and send an interim notification of resource
	 * change events.
	 * <p>
	 * When invoked in the dynamic scope of a call to the
	 * <code>IWorkspace.run</code> method, this method reports a single
	 * resource change event describing the net effect of all changes done to
	 * resources since the last round of notifications. When the outermost
	 * <code>run</code> method eventually completes, it will do another
	 * autobuild (if enabled) and report the resource changes made after this
	 * call.
	 * </p>
	 * <p>
	 * This method has no effect if invoked outside the dynamic scope of a call
	 * to the <code>IWorkspace.run</code> method.
	 * </p>
	 * <p>
	 * This method should be used under controlled circumstance (e.g., to break
	 * up extremely long-running operations).
	 * </p>
	 * 
	 * @param build whether or not to run a build
	 * @see IWorkspace#run(IWorkspaceRunnable, ISchedulingRule, int, IProgressMonitor)
	 */
	public void checkpoint(boolean build);

	/**
	 * Returns the prerequisite ordering of the given projects. The computation
	 * is done by interpreting the projects' active build configuration references
	 * as dependency relationships.
	 * For example if A references B and C, and C references B, this method,
	 * given the list A, B, C will return the order B, C, A. That is, projects
	 * with no dependencies are listed first.
	 * <p>
	 * The return value is a two element array of project arrays. The first
	 * project array is the list of projects which could be sorted (as outlined
	 * above). The second element of the return value is an array of the
	 * projects which are ambiguously ordered (e.g., they are part of a cycle).
	 * </p>
	 * <p>
	 * Cycles and ambiguities are handled by elimination. Projects involved in
	 * cycles are simply cut out of the ordered list and returned in an
	 * undefined order. Closed and non-existent projects are ignored and do not
	 * appear in the returned value at all.
	 * </p>
	 * 
	 * @param projects the projects to order
	 * @return the projects in sorted order and a list of projects which could
	 * not be ordered
	 * @deprecated Replaced by <code>IWorkspace.computeProjectOrder</code>,
	 * which produces a more usable result when there are cycles in project
	 * reference graph.
	 */
	@Deprecated
	public IProject[][] computePrerequisiteOrder(IProject[] projects);

	/**
	 * Data structure for holding the multi-part outcome of
	 * <code>IWorkspace.computeProjectOrder</code>.
	 * <p>
	 * This class is not intended to be instantiated by clients.
	 * </p>
	 * 
	 * @see IWorkspace#computeProjectOrder(IProject[])
	 * @since 2.1
	 */
	public final class ProjectOrder {
		/**
		 * Creates an instance with the given values.
		 * <p>
		 * This class is not intended to be instantiated by clients.
		 * </p>
		 * 
		 * @param projects initial value of <code>projects</code> field
		 * @param hasCycles initial value of <code>hasCycles</code> field
		 * @param knots initial value of <code>knots</code> field
		 */
		public ProjectOrder(IProject[] projects, boolean hasCycles, IProject[][] knots) {
			this.projects = projects;
			this.hasCycles = hasCycles;
			this.knots = knots;
		}

		/**
		 * A list of projects ordered so as to honor the project reference, and
		 * build configuration reference, relationships between these projects
		 * wherever possible.
		 * The elements are a subset of the ones passed as the <code>projects</code>
		 * parameter to <code>IWorkspace.computeProjectOrder</code>, where
		 * inaccessible (closed or non-existent) projects have been omitted.
		 */
		public IProject[] projects;
		/**
		 * Indicates whether any of the accessible projects in
		 * <code>projects</code> are involved in non-trivial cycles.
		 * <code>true</code> if the reference graph contains at least
		 * one cycle involving two or more of the projects in
		 * <code>projects</code>, and <code>false</code> if none of the
		 * projects in <code>projects</code> are involved in cycles.
		 */
		public boolean hasCycles;
		/**
		 * A list of knots in the project reference graph. This list is empty if
		 * the project reference graph does not contain cycles. If the project
		 * reference graph contains cycles, each element is a knot of two or
		 * more accessible projects from <code>projects</code> that are
		 * involved in a cycle of mutually dependent references.
		 */
		public IProject[][] knots;
	}

	/**
	 * Computes a total ordering of the given projects based on both static and
	 * dynamic project references. If an existing and open project P references
	 * another existing and open project Q also included in the list, then Q
	 * should come before P in the resulting ordering. Closed and non-existent
	 * projects are ignored, and will not appear in the result. References to
	 * non-existent or closed projects are also ignored, as are any
	 * self-references. The total ordering is always consistent with the global
	 * total ordering of all open projects in the workspace.
	 * <p>
	 * When there are choices, the choice is made in a reasonably stable way.
	 * For example, given an arbitrary choice between two projects, the one with
	 * the lower collating project name is usually selected.
	 * </p>
	 * <p>
	 * When the project reference graph contains cyclic references, it is
	 * impossible to honor all of the relationships. In this case, the result
	 * ignores as few relationships as possible. For example, if P2 references
	 * P1, P4 references P3, and P2 and P3 reference each other, then exactly
	 * one of the relationships between P2 and P3 will have to be ignored. The
	 * outcome will be either [P1, P2, P3, P4] or [P1, P3, P2, P4]. The result
	 * also contains complete details of any cycles present.
	 * </p>
	 * <p>
	 * This method is time-consuming and should not be called unnecessarily.
	 * There are a very limited set of changes to a workspace that could affect
	 * the outcome: creating, renaming, or deleting a project; opening or
	 * closing a project; adding or removing a project reference.
	 * </p>
	 * 
	 * @param projects the projects to order
	 * @return result describing the project order
	 * @since 2.1
	 */
	public ProjectOrder computeProjectOrder(IProject[] projects);

	/**
	 * Copies the given sibling resources so that they are located as members of
	 * the resource at the given path; the names of the copies are the same as
	 * the corresponding originals.
	 * <p>
	 * This is a convenience method, fully equivalent to:
	 * 
	 * <pre>
	 * copy(resources, destination, (force ? IResource.FORCE : IResource.NONE), monitor);
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * This method changes resources; these changes will be reported in a
	 * subsequent resource change event that will include an indication that the
	 * resources have been added to the new parent.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * 
	 * @param resources the resources to copy
	 * @param destination the destination container path
	 * @param force a flag controlling whether resources that are not in sync
	 * with the local file system will be tolerated
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @return a status object with code <code>OK</code> if there were no
	 * problems; otherwise a description (possibly a multi-status) consisting of
	 * low-severity warnings or informational messages
	 * @exception CoreException if the method fails to copy some resources. The
	 * status contained in the exception may be a multi-status indicating where
	 * the individual failures occurred.
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * @see #copy(IResource[],IPath,int,IProgressMonitor)
	 */
	public IStatus copy(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException;

	/**
	 * Copies the given sibling resources so that they are located as members of
	 * the resource at the given path; the names of the copies are the same as
	 * the corresponding originals.
	 * <p>
	 * This method can be expressed as a series of calls to
	 * <code>IResource.copy(IPath,int,IProgressMonitor)</code>, with "best
	 * effort" semantics:
	 * <ul>
	 * <li>Resources are copied in the order specified, using the given update
	 * flags.</li>
	 * <li>Duplicate resources are only copied once.</li>
	 * <li>The method fails if the resources are not all siblings.</li>
	 * <li>The failure of an individual copy does not necessarily prevent the
	 * method from attempting to copy other resources.</li>
	 * <li>The method fails if there are projects among the resources.</li>
	 * <li>The method fails if the path of the resources is a prefix of the
	 * destination path.</li>
	 * <li>This method also fails if one or more of the individual resource
	 * copy steps fails.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * After successful completion, corresponding new resources will now exist
	 * as members of the resource at the given path.
	 * </p>
	 * <p>
	 * The supplied path may be absolute or relative. Absolute paths fully
	 * specify the new location for the resource, including its project.
	 * Relative paths are considered to be relative to the container of the
	 * resources being copied. A trailing separator is ignored.
	 * </p>
	 * <p>
	 * This method changes resources; these changes will be reported in a
	 * subsequent resource change event that will include an indication that the
	 * resources have been added to the new parent.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * 
	 * @param resources the resources to copy
	 * @param destination the destination container path
	 * @param updateFlags bit-wise or of update flag constants
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @return a status object with code <code>OK</code> if there were no
	 * problems; otherwise a description (possibly a multi-status) consisting of
	 * low-severity warnings or informational messages
	 * @exception CoreException if the method fails to copy some resources. The
	 * status contained in the exception may be a multi-status indicating where
	 * the individual failures occurred. Reasons include:
	 * <ul>
	 * <li>One of the resources does not exist.</li>
	 * <li>The resources are not siblings.</li>
	 * <li>One of the resources, or one of its descendents, is not local.</li>
	 * <li>The resource corresponding to the destination path does not exist.
	 * </li>
	 * <li>The resource corresponding to the parent destination path is a
	 * closed project.</li>
	 * <li>A corresponding target resource does exist.</li>
	 * <li>A resource of a different type exists at the target path.</li>
	 * <li>One of the resources is a project.</li>
	 * <li>The path of one of the resources is a prefix of the destination
	 * path.</li>
	 * <li>One of the resources, or one of its descendents, is out of sync with
	 * the local file system and <code>FORCE</code> is not specified.</li>
	 * <li>Resource changes are disallowed during certain types of resource
	 * change event notification. See <code>IResourceChangeEvent</code> for
	 * more details.</li>
	 * </ul>
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * @see IResource#copy(IPath,int,IProgressMonitor)
	 * @see IResourceRuleFactory#copyRule(IResource, IResource)
	 * @since 2.0
	 */
	public IStatus copy(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException;

	/**
	 * Deletes the given resources.
	 * <p>
	 * This is a convenience method, fully equivalent to:
	 * 
	 * <pre>
	 * delete(resources, IResource.KEEP_HISTORY | (force ? IResource.FORCE : IResource.NONE), monitor);
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * This method changes resources; these changes will be reported in a
	 * subsequent resource change event.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * 
	 * @param resources the resources to delete
	 * @param force a flag controlling whether resources that are not in sync
	 * with the local file system will be tolerated
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @return status with code <code>OK</code> if there were no problems;
	 * otherwise a description (possibly a multi-status) consisting of
	 * low-severity warnings or informational messages
	 * @exception CoreException if the method fails to delete some resource. The
	 * status contained in the exception is a multi-status indicating where the
	 * individual failures occurred.
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * @see #delete(IResource[],int,IProgressMonitor)
	 */
	public IStatus delete(IResource[] resources, boolean force, IProgressMonitor monitor) throws CoreException;

	/**
	 * Deletes the given resources.
	 * <p>
	 * This method can be expressed as a series of calls to
	 * <code>IResource.delete(int,IProgressMonitor)</code>.
	 * </p>
	 * <p>
	 * The semantics of multiple deletion are:
	 * <ul>
	 * <li>Resources are deleted in the order presented, using the given update
	 * flags.</li>
	 * <li>Resources that do not exist are ignored.</li>
	 * <li>An individual deletion fails if the resource still exists
	 * afterwards.</li>
	 * <li>The failure of an individual deletion does not prevent the method
	 * from attempting to delete other resources.</li>
	 * <li>This method fails if one or more of the individual resource
	 * deletions fails; that is, if at least one of the resources in the list
	 * still exists at the end of this method.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * This method changes resources; these changes will be reported in a
	 * subsequent resource change event.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * 
	 * @param resources the resources to delete
	 * @param updateFlags bit-wise or of update flag constants
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @return status with code <code>OK</code> if there were no problems;
	 * otherwise a description (possibly a multi-status) consisting of
	 * low-severity warnings or informational messages
	 * @exception CoreException if the method fails to delete some resource. The
	 * status contained in the exception is a multi-status indicating where the
	 * individual failures occurred.
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * @see IResource#delete(int,IProgressMonitor)
	 * @see IResourceRuleFactory#deleteRule(IResource)
	 * @since 2.0
	 */
	public IStatus delete(IResource[] resources, int updateFlags, IProgressMonitor monitor) throws CoreException;

	/**
	 * Removes the given markers from the resources with which they are
	 * associated. Markers that do not exist are ignored.
	 * <p>
	 * This method changes resources; these changes will be reported in a
	 * subsequent resource change event.
	 * </p>
	 * 
	 * @param markers the markers to remove
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>Resource changes are disallowed during certain types of resource
	 * change event notification. See <code>IResourceChangeEvent</code> for
	 * more details.</li>
	 * </ul>
	 * @see IResourceRuleFactory#markerRule(IResource)
	 */
	public void deleteMarkers(IMarker[] markers) throws CoreException;

	/**
	 * Forgets any resource tree being saved for the plug-in with the given
	 * name. If the plug-in id is <code>null</code>, all trees are forgotten.
	 * <p>
	 * Clients should not call this method unless they have a reason to do so. A
	 * plug-in which uses <code>ISaveContext.needDelta</code> in the process
	 * of a save indicates that it would like to be fed the resource delta the
	 * next time it is reactivated. If a plug-in never gets reactivated (or if
	 * it fails to successfully register to participate in workspace saves), the
	 * workspace nevertheless retains the necessary information to generate the
	 * resource delta if asked. This method allows such a long term leak to be
	 * plugged.
	 * </p>
	 * 
	 * @param pluginId the unique identifier of the plug-in, or <code>null</code>
	 * @see ISaveContext#needDelta()
	 */
	public void forgetSavedTree(String pluginId);

	/**
	 * Returns all filter matcher descriptors known to this workspace. Returns an empty
	 * array if there are no installed filter matchers.
	 * 
	 * @return the filter matcher descriptors known to this workspace
	 * @since 3.6
	 */
	public IFilterMatcherDescriptor[] getFilterMatcherDescriptors();

	/**
	 * Returns the filter descriptor with the given unique identifier, or
	 * <code>null</code> if there is no such filter.
	 * 
	 * @param filterMatcherId the filter matcher extension identifier (e.g.
	 * <code>"com.example.coolFilter"</code>).
	 * @return the filter matcher descriptor, or <code>null</code>
	 * @since 3.6
	 */
	public IFilterMatcherDescriptor getFilterMatcherDescriptor(String filterMatcherId);

	/**
	 * Returns all nature descriptors known to this workspace. Returns an empty
	 * array if there are no installed natures.
	 * 
	 * @return the nature descriptors known to this workspace
	 * @since 2.0
	 */
	public IProjectNatureDescriptor[] getNatureDescriptors();

	/**
	 * Returns the nature descriptor with the given unique identifier, or
	 * <code>null</code> if there is no such nature.
	 * 
	 * @param natureId the nature extension identifier (e.g.
	 * <code>"com.example.coolNature"</code>).
	 * @return the nature descriptor, or <code>null</code>
	 * @since 2.0
	 */
	public IProjectNatureDescriptor getNatureDescriptor(String natureId);

	/**
	 * Finds all dangling project references in this workspace. Projects which
	 * are not open are ignored. Returns a map with one entry for each open
	 * project in the workspace that has at least one dangling project
	 * reference; the value of the entry is an array of projects which are
	 * referenced by that project but do not exist in the workspace. Returns an
	 * empty Map if there are no projects in the workspace.
	 * 
	 * @return a map (key type: <code>IProject</code>, value type:
	 * <code>IProject[]</code>) from project to dangling project references
	 */
	public Map<IProject,IProject[]> getDanglingReferences();

	/**
	 * Returns the workspace description. This object is responsible for
	 * defining workspace preferences. The returned value is a modifiable copy
	 * but changes are not automatically applied to the workspace. In order to
	 * changes take effect, <code>IWorkspace.setDescription</code> needs to be
	 * called. The workspace description values are store in the preference
	 * store.
	 * 
	 * @return the workspace description
	 * @see #setDescription(IWorkspaceDescription)
	 */
	public IWorkspaceDescription getDescription();

	/**
	 * Returns the root resource of this workspace.
	 * 
	 * @return the workspace root
	 */
	public IWorkspaceRoot getRoot();

	/**
	 * Returns a factory for obtaining scheduling rules prior to modifying
	 * resources in the workspace.
	 * 
	 * @see IResourceRuleFactory
	 * @return a resource rule factory
	 * @since 3.0
	 */
	public IResourceRuleFactory getRuleFactory();

	/**
	 * Returns the synchronizer for this workspace.
	 * 
	 * @return the synchronizer
	 * @see ISynchronizer
	 */
	public ISynchronizer getSynchronizer();

	/**
	 * Returns whether this workspace performs autobuilds.
	 * 
	 * @return <code>true</code> if autobuilding is on, <code>false</code>
	 * otherwise
	 */
	public boolean isAutoBuilding();

	/**
	 * Returns whether the workspace tree is currently locked. Resource changes
	 * are disallowed during certain types of resource change event
	 * notification. See <code>IResourceChangeEvent</code> for more details.
	 * 
	 * @return boolean <code>true</code> if the workspace tree is locked,
	 * <code>false</code> otherwise
	 * @see IResourceChangeEvent
	 * @since 2.1
	 */
	public boolean isTreeLocked();

	/**
	 * Reads the project description file (".project") from the given location
	 * in the local file system. This object is useful for discovering the
	 * correct name for a project before importing it into the workspace.
	 * <p>
	 * The returned value is writeable.
	 * </p>
	 * 
	 * @param projectDescriptionFile the path in the local file system of an
	 * existing project description file
	 * @return a new project description
	 * @exception CoreException if the operation failed. Reasons include:
	 * <ul>
	 * <li>The project description file does not exist.</li>
	 * <li>The file cannot be opened or read.</li>
	 * <li>The file cannot be parsed as a legal project description.</li>
	 * </li>
	 * @see #newProjectDescription(String)
	 * @see IProject#getDescription()
	 * @since 2.0
	 */
	public IProjectDescription loadProjectDescription(IPath projectDescriptionFile) throws CoreException;

	/**
	 * Reads the project description file (".project") from the given InputStream.
	 * This object will not attempt to set the location since the project may not
	 * have a valid location on the local file system.
	 * This object is useful for discovering the correct name for a project before 
	 * importing it into the workspace.
	 * <p>
	 * The returned value is writeable.
	 * </p>
	 * 
	 * @param projectDescriptionFile an InputStream pointing to an existing project
	 * description file
	 * @return a new project description
	 * @exception CoreException if the operation failed. Reasons include:
	 * <ul>
	 * <li>The stream could not be read.</li>
	 * <li>The stream does not contain a legal project description.</li>
	 * </li>
	 * @see #newProjectDescription(String)
	 * @see IProject#getDescription()
	 * @see IWorkspace#loadProjectDescription(IPath)
	 * @since 3.1
	 */
	public IProjectDescription loadProjectDescription(InputStream projectDescriptionFile) throws CoreException;

	/**
	 * Moves the given sibling resources so that they are located as members of
	 * the resource at the given path; the names of the new members are the
	 * same.
	 * <p>
	 * This is a convenience method, fully equivalent to:
	 * 
	 * <pre>
	 * move(resources, destination, IResource.KEEP_HISTORY | (force ? IResource.FORCE : IResource.NONE), monitor);
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * This method changes resources; these changes will be reported in a
	 * subsequent resource change event that will include an indication that the
	 * resources have been removed from their parent and that corresponding
	 * resources have been added to the new parent. Additional information
	 * provided with resource delta shows that these additions and removals are
	 * pairwise related.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * 
	 * @param resources the resources to move
	 * @param destination the destination container path
	 * @param force a flag controlling whether resources that are not in sync
	 * with the local file system will be tolerated
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @return status with code <code>OK</code> if there were no problems;
	 * otherwise a description (possibly a multi-status) consisting of
	 * low-severity warnings or informational messages.
	 * @exception CoreException if the method fails to move some resources. The
	 * status contained in the exception may be a multi-status indicating where
	 * the individual failures occurred.
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * @see #move(IResource[],IPath,int,IProgressMonitor)
	 */
	public IStatus move(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException;

	/**
	 * Moves the given sibling resources so that they are located as members of
	 * the resource at the given path; the names of the new members are the
	 * same.
	 * <p>
	 * This method can be expressed as a series of calls to
	 * <code>IResource.move</code>, with "best effort" semantics:
	 * <ul>
	 * <li>Resources are moved in the order specified.</li>
	 * <li>Duplicate resources are only moved once.</li>
	 * <li>The <code>force</code> flag has the same meaning as it does on the
	 * corresponding single-resource method.</li>
	 * <li>The method fails if the resources are not all siblings.</li>
	 * <li>The method fails the path of any of the resources is a prefix of the
	 * destination path.</li>
	 * <li>The failure of an individual move does not necessarily prevent the
	 * method from attempting to move other resources.</li>
	 * <li>This method also fails if one or more of the individual resource
	 * moves fails; that is, if at least one of the resources in the list still
	 * exists at the end of this method.</li>
	 * <li>History is kept for moved files. When projects are moved, no history
	 * is kept</li>
	 * </ul>
	 * </p>
	 * <p>
	 * After successful completion, the resources and descendents will no longer
	 * exist; but corresponding new resources will now exist as members of the
	 * resource at the given path.
	 * </p>
	 * <p>
	 * The supplied path may be absolute or relative. Absolute paths fully
	 * specify the new location for the resource, including its project.
	 * Relative paths are considered to be relative to the container of the
	 * resources being moved. A trailing separator is ignored.
	 * </p>
	 * <p>
	 * This method changes resources; these changes will be reported in a
	 * subsequent resource change event that will include an indication that the
	 * resources have been removed from their parent and that corresponding
	 * resources have been added to the new parent. Additional information
	 * provided with resource delta shows that these additions and removals are
	 * pairwise related.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * 
	 * @param resources the resources to move
	 * @param destination the destination container path
	 * @param updateFlags bit-wise or of update flag constants
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @return status with code <code>OK</code> if there were no problems;
	 * otherwise a description (possibly a multi-status) consisting of
	 * low-severity warnings or informational messages.
	 * @exception CoreException if the method fails to move some resources. The
	 * status contained in the exception may be a multi-status indicating where
	 * the individual failures occurred. Reasons include:
	 * <ul>
	 * <li>One of the resources does not exist.</li>
	 * <li>The resources are not siblings.</li>
	 * <li>One of the resources, or one of its descendents, is not local.</li>
	 * <li>The resource corresponding to the destination path does not exist.
	 * </li>
	 * <li>The resource corresponding to the parent destination path is a
	 * closed project.</li>
	 * <li>A corresponding target resource does exist.</li>
	 * <li>A resource of a different type exists at the target path.</li>
	 * <li>The path of one of the resources is a prefix of the destination
	 * path.</li>
	 * <li>One of the resources, or one of its descendents, is out of sync with
	 * the local file system and <code>FORCE</code> is <code>false</code>.
	 * </li>
	 * <li>Resource changes are disallowed during certain types of resource
	 * change event notification. See <code>IResourceChangeEvent</code> for
	 * more details.</li>
	 * </ul>
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * @see IResource#move(IPath,int,IProgressMonitor)
	 * @see IResourceRuleFactory#moveRule(IResource, IResource)
	 * @since 2.0
	 */
	public IStatus move(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns a new build configuration for the project, with the given name.  
	 * The name is a human readable unique name for the build configuration in the
	 * project.  The project need not exist.
	 *<p>
	 * This API can be used to create {@link IBuildConfiguration}s that will be used as references
	 * to {@link IBuildConfiguration}s in other projects.  These references are set using
	 * {@link IProjectDescription#setBuildConfigReferences(String, IBuildConfiguration[])}
	 * and may have a <code>null</code> configuration name which will resolve to the referenced
	 * project's active configuration when the reference is used.
	 *</p>
	 *<p>
	 * Build configuration do not become part of a project
	 * description until set using {@link IProjectDescription#setBuildConfigs(String[])}.
	 *</p>
	 *
	 * @param projectName the name of the project on which the configuration will exist
	 * @param configName the name of the new build configuration
	 * @return a build configuration
	 * @see IProjectDescription#setBuildConfigs(String[])
	 * @see IProjectDescription#setBuildConfigReferences(String, IBuildConfiguration[])
	 * @see IBuildConfiguration
	 * @since 3.7
	 */
	public IBuildConfiguration newBuildConfig(String projectName, String configName);

	/**
	 * Creates and returns a new project description for a project with the
	 * given name. This object is useful when creating, moving or copying
	 * projects.
	 * <p>
	 * The project description is initialized to:
	 * <ul>
	 * <li>the given project name</li>
	 * <li>no references to other projects</li>
	 * <li>an empty build spec</li>
	 * <li>an empty comment</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The returned value is writeable.
	 * </p>
	 * 
	 * @param projectName the name of the project
	 * @return a new project description
	 * @see IProject#getDescription()
	 * @see IProject#create(IProjectDescription, IProgressMonitor)
	 * @see IResource#copy(IProjectDescription, int, IProgressMonitor)
	 * @see IProject#move(IProjectDescription, boolean, IProgressMonitor)
	 */
	public IProjectDescription newProjectDescription(String projectName);

	/**
	 * Removes the given resource change listener from this workspace. Has no
	 * effect if an identical listener is not registered.
	 * 
	 * @param listener the listener
	 * @see IResourceChangeListener
	 * @see #addResourceChangeListener(IResourceChangeListener)
	 */
	public void removeResourceChangeListener(IResourceChangeListener listener);

	/**
	 * Removes the workspace save participant for the given plug-in from this
	 * workspace. If no such participant is registered, no action is taken.
	 * <p>
	 * Once removed, the workspace save participant no longer actively
	 * participates in any future saves of this workspace.
	 * </p>
	 * 
	 * @param plugin the plug-in
	 * @see ISaveParticipant
	 * @see #addSaveParticipant(Plugin, ISaveParticipant)
	 * @deprecated Use {@link #removeSaveParticipant(String)} instead
	 */
	@Deprecated
	public void removeSaveParticipant(Plugin plugin);
	
	/**
	 * Removes the workspace save participant for the given plug-in from this
	 * workspace. If no such participant is registered, no action is taken.
	 * <p>
	 * Once removed, the workspace save participant no longer actively
	 * participates in any future saves of this workspace.
	 * </p>
	 * 
	 * @param pluginId the unique identifier of the plug-in
	 * @see ISaveParticipant
	 * @see #addSaveParticipant(String, ISaveParticipant)
	 * @since 3.6
	 */
	public void removeSaveParticipant(String pluginId);

	/**
	 * Runs the given action as an atomic workspace operation.
	 * <p>
	 * After running a method that modifies resources in the workspace,
	 * registered listeners receive after-the-fact notification of what just
	 * transpired, in the form of a resource change event. This method allows
	 * clients to call a number of methods that modify resources and only have
	 * resource change event notifications reported at the end of the entire
	 * batch.
	 * </p>
	 * <p>
	 * If this method is called outside the dynamic scope of another such call,
	 * this method runs the action and then reports a single resource change
	 * event describing the net effect of all changes done to resources by the
	 * action.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such call, this
	 * method simply runs the action.
	 * </p>
	 * <p>
	 * The supplied scheduling rule is used to determine whether this operation
	 * can be run simultaneously with workspace changes in other threads. If the
	 * scheduling rule conflicts with another workspace change that is currently
	 * running, the calling thread will be blocked until that change completes.
	 * If the action attempts to make changes to the workspace that were not
	 * specified in the scheduling rule, it will fail. If no scheduling rule is
	 * supplied, there are no scheduling restrictions for this operation. 
	 * If a non-<code>null</code> scheduling rule is supplied, this operation 
	 * must always support cancelation in the case where this operation becomes 
	 * blocked by a long running background operation.
	 * </p>
	 * <p>
	 * The AVOID_UPDATE flag controls whether periodic resource change
	 * notifications should occur during the scope of this call. If this flag is
	 * specified, and no other threads modify the workspace concurrently, then
	 * all resource change notifications will be deferred until the end of this
	 * call. If this flag is not specified, the platform may decide to broadcast
	 * periodic resource change notifications during the scope of this call.
	 * </p>
	 * <p>
	 * Flags other than <code>AVOID_UPDATE</code> are ignored.
	 * </p>
	 * 
	 * @param action the action to perform
	 * @param rule the scheduling rule to use when running this operation, or
	 * <code>null</code> if there are no scheduling restrictions for this
	 * operation.
	 * @param flags bit-wise or of flag constants (only AVOID_UPDATE is relevant
	 * here)
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired. 
	 * @exception CoreException if the operation failed.
	 * @exception OperationCanceledException if the operation is canceled. If a 
	 * non-<code>null</code> scheduling rule is supplied, cancelation can occur
	 * even if no progress monitor is provided.
	 * 
	 * @see #AVOID_UPDATE
	 * @see IResourceRuleFactory
	 * @since 3.0
	 */
	public void run(IWorkspaceRunnable action, ISchedulingRule rule, int flags, IProgressMonitor monitor) throws CoreException;

	/**
	 * Runs the given action as an atomic workspace operation.
	 * <p>
	 * This is a convenience method, fully equivalent to:
	 * 
	 * <pre>
	 * workspace.run(action, workspace.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param action the action to perform
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @exception CoreException if the operation failed.
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 */
	public void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException;

	/**
	 * Saves this workspace's valuable state on disk. Consults with all
	 * registered plug-ins so that they can coordinate the saving of their
	 * persistent state as well.
	 * <p>
	 * The <code>full</code> parameter indicates whether a full save or a
	 * snapshot is being requested. Snapshots save the workspace information
	 * that is considered hard to be recomputed in the unlikely event of a
	 * crash. It includes parts of the workspace tree, workspace and projects
	 * descriptions, markers and sync information. Full saves are heavy weight
	 * operations which save the complete workspace state.
	 * </p>
	 * <p>
	 * To ensure that all outstanding changes to the workspace have been
	 * reported to interested parties prior to saving, a full save cannot be
	 * used within the dynamic scope of an <code>IWorkspace.run</code>
	 * invocation. Snapshots can be called any time and are interpreted by the
	 * workspace as a hint that a snapshot is required. The workspace will
	 * perform the snapshot when possible. Even as a hint, snapshots should only
	 * be called when necessary as they impact system performance. Although
	 * saving does not change the workspace per se, its execution is serialized
	 * like methods that write the workspace.
	 * </p>
	 * <p>
	 * The workspace is comprised of several different kinds of data with
	 * varying degrees of importance. The most important data, the resources
	 * themselves and their persistent properties, are written to disk
	 * immediately; other data are kept in volatile memory and only written to
	 * disk periodically; and other data are maintained in memory and never
	 * written out. The following table summarizes what gets saved when:
	 * <ul>
	 * <li>creating or deleting resource - immediately</li>
	 * <li>setting contents of file - immediately</li>
	 * <li>changes to project description - immediately</li>
	 * <li>session properties - never</li>
	 * <li>changes to persistent properties - immediately</li>
	 * <li>markers -<code>save</code></li>
	 * <li>synchronizer info -<code>save</code></li>
	 * <li>shape of the workspace resource tree -<code>save</code></li>
	 * <li>list of active plug-ins - never</li>
	 * </ul>
	 * Resource-based plug-in also have data with varying degrees of importance.
	 * Each plug-in gets to decide the policy for protecting its data, either
	 * immediately, never, or at <code>save</code> time. For the latter, the
	 * plug-in coordinates its actions with the workspace (see
	 * <code>ISaveParticipant</code> for details).
	 * </p>
	 * <p>
	 * If the platform is shutdown (or crashes) after saving the workspace, any
	 * information written to disk by the last successful workspace
	 * <code>save</code> will be restored the next time the workspace is
	 * reopened for the next session. Naturally, information that is written to
	 * disk immediately will be as of the last time it was changed.
	 * </p>
	 * <p>
	 * The workspace provides a general mechanism for keeping concerned parties
	 * apprised of any and all changes to resources in the workspace (
	 * <code>IResourceChangeListener</code>). It is even possible for a
	 * plug-in to find out about changes to resources that happen between
	 * workspace sessions (see <code>IWorkspace.addSaveParticipant</code>).
	 * </p>
	 * <p>
	 * At certain points during this method, the entire workspace resource tree
	 * must be locked to prevent resources from being changed (read access to
	 * resources is permitted).
	 * </p>
	 * <p>
	 * Implementation note: The execution sequence is as follows.
	 * <ul>
	 * <li>A long-term lock on the workspace is taken out to prevent further
	 * changes to workspace until the save is done.</li>
	 * <li>The list of saveable resource tree snapshots is initially empty.
	 * </li>
	 * <li>A different <code>ISaveContext</code> object is created for each
	 * registered workspace save participant plug-in, reflecting the kind of
	 * save (<code>ISaveContext.getKind</code>), the previous save number in
	 * which this plug-in actively participated, and the new save number (=
	 * previous save number plus 1).</li>
	 * <li>Each registered workspace save participant is sent
	 * <code>prepareToSave(context)</code>, passing in its own context
	 * object.
	 * <ul>
	 * <li>Plug-in suspends all activities until further notice.</li>
	 * </ul>
	 * If <code>prepareToSave</code> fails (throws an exception), the problem
	 * is logged and the participant is marked as unstable.</li>
	 * <li>In dependent-before-prerequisite order, each registered workspace
	 * save participant is sent <code>saving(context)</code>, passing in its
	 * own context object.
	 * <ul>
	 * <li>Plug-in decides whether it wants to actively participate in this
	 * save. The plug-in only needs to actively participate if some of its
	 * important state has changed since the last time it actively participated.
	 * If it does decide to actively participate, it writes its important state
	 * to a brand new file in its plug-in state area under a generated file name
	 * based on <code>context.getStateNumber()</code> and calls
	 * <code>context.needStateNumber()</code> to indicate that it has actively
	 * participated. If upon reactivation the plug-in will want a resource delta
	 * covering all changes between now and then, the plug-in should invoke
	 * <code>context.needDelta()</code> to request this now; otherwise, a
	 * resource delta for the intervening period will not be available on
	 * reactivation.</li>
	 * </ul>
	 * If <code>saving</code> fails (throws an exception), the problem is
	 * logged and the participant is marked as unstable.</li>
	 * <li>The plug-in save table contains an entry for each plug-in that has
	 * registered to participate in workspace saves at some time in the past
	 * (the list of plug-ins increases monotonically). Each entry records the
	 * save number of the last successful save in which that plug-in actively
	 * participated, and, optionally, a saved resource tree (conceptually, this
	 * is a complete tree; in practice, it is compressed into a special delta
	 * tree representation). A copy of the plug-in save table is made. Entries
	 * are created or modified for each registered plug-in to record the
	 * appropriate save number (either the previous save number, or the previous
	 * save number plus 1, depending on whether the participant was active and
	 * asked for a new number).</li>
	 * <li>The workspace tree, the modified copy of the plug-in save table, all
	 * markers, etc. and all saveable resource tree snapshots are written to
	 * disk as <b>one atomic operation </b>.</li>
	 * <li>The long-term lock on the workspace is released.</li>
	 * <li>If the atomic save succeeded:
	 * <ul>
	 * <li>The modified copy of the plug-in save table becomes the new plug-in
	 * save table.</li>
	 * <li>In prerequisite-before-dependent order, each registered workspace
	 * save participant is sent <code>doneSaving(context)</code>, passing in
	 * its own context object.
	 * <ul>
	 * <li>Plug-in may perform clean up by deleting obsolete state files in its
	 * plug-in state area.</li>
	 * <li>Plug-in resumes its normal activities.</li>
	 * </ul>
	 * If <code>doneSaving</code> fails (throws an exception), the problem is
	 * logged and the participant is marked as unstable. (The state number in
	 * the save table is not rolled back just because of this instability.)
	 * </li>
	 * <li>The workspace save operation returns.</li>
	 * </ul>
	 * <li>If it failed:
	 * <ul>
	 * <li>The workspace previous state is restored.</li>
	 * <li>In prerequisite-before-dependent order, each registered workspace
	 * save participant is sent <code>rollback(context)</code>, passing in
	 * its own context object.
	 * <ul>
	 * <li>Plug-in may perform clean up by deleting newly-created but obsolete
	 * state file in its plug-in state area.</li>
	 * <li>Plug-in resumes its normal activities.</li>
	 * </ul>
	 * If <code>rollback</code> fails (throws an exception), the problem is
	 * logged and the participant is marked as unstable. (The state number in
	 * the save table is rolled back anyway.)</li>
	 * <li>The workspace save operation fails.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </p>
	 * <p>
	 * After a full save, the platform can be shutdown. This will cause the
	 * Resources plug-in and all the other plug-ins to shutdown, without
	 * disturbing the saved workspace on disk.
	 * </p>
	 * <p>
	 * When the platform is later restarted, activating the Resources plug-in
	 * opens the saved workspace. This reads into memory the workspace's
	 * resource tree, plug-in save table, and saved resource tree snapshots
	 * (everything that was written to disk in the atomic operation above).
	 * Later, when a plug-in gets reactivated and registers to participate in
	 * workspace saves, it is handed back the info from its entry in the plug-in
	 * save table, if it has one. It gets back the number of the last save in
	 * which it actively participated and, possibly, a resource delta.
	 * </p>
	 * <p>
	 * The only source of long term garbage would come from a plug-in that never
	 * gets reactivated, or one that gets reactivated but fails to register for
	 * workspace saves. (There is no such problem with a plug-in that gets
	 * uninstalled; its easy enough to scrub its state areas and delete its
	 * entry in the plug-in save table.)
	 * </p>
	 * 
	 * @param full <code>true</code> if this is a full save, and
	 * <code>false</code> if this is only a snapshot for protecting against
	 * crashes
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting is not desired
	 * @return a status that may contain warnings, such as the failure of an
	 * individual participant
	 * @exception CoreException if this method fails to save the state of this
	 * workspace. Reasons include:
	 * <ul>
	 * <li>The operation cannot be batched with others.</li>
	 * </ul>
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * @see #addSaveParticipant(Plugin, ISaveParticipant)
	 */
	public IStatus save(boolean full, IProgressMonitor monitor) throws CoreException;

	/**
	 * Sets the workspace description. Its values are stored in the preference
	 * store.
	 * 
	 * @param description the new workspace description.
	 * @see #getDescription()
	 * @exception CoreException if the method fails. Reasons include:
	 * <ul>
	 * <li>There was a problem setting the workspace description.</li>
	 * </ul>
	 */
	public void setDescription(IWorkspaceDescription description) throws CoreException;

	/**
	 * Returns a copy of the given set of natures sorted in prerequisite order.
	 * For each nature, it is guaranteed that all of its prerequisites will
	 * precede it in the resulting array.
	 * 
	 * <p>
	 * Natures that are missing from the install or are involved in a
	 * prerequisite cycle are sorted arbitrarily. Duplicate nature IDs are
	 * removed, so the returned array may be smaller than the original.
	 * </p>
	 * 
	 * @param natureIds a valid set of nature extension identifiers
	 * @return the set of nature Ids sorted in prerequisite order
	 * @see #validateNatureSet(String[])
	 * @since 2.0
	 */
	public String[] sortNatureSet(String[] natureIds);

	/**
	 * Advises that the caller intends to modify the contents of the given files
	 * in the near future and asks whether modifying all these files would be
	 * reasonable. The files must all exist. This method is used to give the VCM
	 * component an opportunity to check out (or otherwise prepare) the files if
	 * required. (It is provided in this component rather than in the UI so that
	 * "core" (i.e., head-less) clients can use it. Similarly, it is located
	 * outside the VCM component for the convenience of clients that must also
	 * operate in configurations without VCM.)
	 * </p>
	 * <p>
	 * A client (such as an editor) should perform a <code>validateEdit</code>
	 * on a file whenever it finds itself in the following position: (a) the
	 * file is marked read-only, and (b) the client believes it likely (not
	 * necessarily certain) that it will modify the file's contents at some
	 * point. A case in point is an editor that has a buffer opened on a file.
	 * When the user starts to dirty the buffer, the editor should check to see
	 * whether the file is read-only. If it is, it should call
	 * <code>validateEdit</code>, and can reasonably expect this call, when
	 * successful, to cause the file to become read-write. An editor should also
	 * be sensitive to a file becoming read-only again even after a successful
	 * <code>validateEdit</code> (e.g., due to the user checking in the file
	 * in a different view); the editor should again call
	 * <code>validateEdit</code> if the file is read-only before attempting to
	 * save the contents of the file.
	 * </p>
	 * <p>
	 * By passing a UI context, the caller indicates that the VCM component may
	 * contact the user to help decide how best to proceed. If no UI context is
	 * provided, the VCM component will make its decision without additional
	 * interaction with the user. If OK is returned, the caller can safely
	 * assume that all of the given files haven been prepared for modification
	 * and that there is good reason to believe that
	 * <code>IFile.setContents</code> (or <code>appendContents</code>)
	 * would be successful on any of them. If the result is not OK, modifying
	 * the given files might not succeed for the reason(s) indicated.
	 * </p>
	 * <p>
	 * If a shell is passed in as the context, the VCM component may bring up a
	 * dialogs to query the user or report difficulties; the shell should be
	 * used to parent any such dialogs; the caller may safely assume that the
	 * reasons for failure will have been made clear to the user. If
	 * {@link IWorkspace#VALIDATE_PROMPT} is passed
	 * as the context, this indicates that the caller does not have access to
	 * a UI context but would still like the user to be prompted if required.
	 * If <code>null</code> is passed, the user should not be contacted; any
	 * failures should be reported via the result; the caller may chose to
	 * present these to the user however they see fit. The ideal implementation
	 * of this method is transactional; no files would be affected unless the
	 * go-ahead could be given. (In practice, there may be no feasible way to
	 * ensure such changes get done atomically.)
	 * </p>
	 * <p>
	 * The method calls <code>FileModificationValidator.validateEdit</code>
	 * for the file modification validator (if provided by the VCM plug-in).
	 * When there is no file modification validator, this method returns a
	 * status with an <code>IResourceStatus.READ_ONLY_LOCAL</code> code if one
	 * of the files is read-only, and a status with an <code>IStatus.OK</code>
	 * code otherwise.
	 * </p>
	 * <p>
	 * This method may be called from any thread. If the UI context is used, it
	 * is the responsibility of the implementor of
	 * <code>FileModificationValidator.validateEdit</code> to interact with
	 * the UI context in an appropriate thread.
	 * </p>
	 * 
	 * @param files the files that are to be modified; these files must all
	 * exist in the workspace
	 * @param context either {@link IWorkspace#VALIDATE_PROMPT},
	 * or the <code>org.eclipse.swt.widgets.Shell</code> that is
	 * to be used to parent any dialogs with the user, or <code>null</code> if
	 * there is no UI context (declared as an <code>Object</code> to avoid any
	 * direct references on the SWT component)
	 * @return a status object that is <code>OK</code> if things are fine, 
	 * otherwise a status describing reasons why modifying the given files is not 
	 * reasonable. A status with a severity of <code>CANCEL</code> is returned
	 * if the validation was canceled, indicating the edit should not proceed.
	 * @see IResourceRuleFactory#validateEditRule(IResource[])
	 * @since 2.0
	 */
	public IStatus validateEdit(IFile[] files, Object context);

	/**
	 * Validates that the given resource will not (or would not, if the resource
	 * doesn't exist in the workspace yet) be filtered out from the workspace by
	 * its parent resource filters.
	 * <p>
	 * Note that if the resource or its parent doesn't exist yet in the workspace, 
	 * it is possible that it will still be effectively filtered out once the resource
	 * and/or its parent is created, even though this method doesn't report it.
	 * 
	 * But if this method reports the resource as filtered, even though it, or its 
	 * parent, doesn't exist in the workspace yet, it means that the resource will
	 * be filtered out by its parent resource filters once it exists in the workspace.
	 * </p>
	 * <p>
	 * This method will return a status with severity <code>IStatus.ERROR</code>
	 * if the resource will be filtered out - removed - out of the workspace by 
	 * its parent resource filters. 
	 * </p>
	 * <p>
	 * Note: linked resources and virtual folders are never filtered out by their
	 * parent resource filters.
	 * 
	 * @param resource the resource to validate the location for
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * resource is not filtered by its parent resource filters, otherwise a status
	 * object with severity <code>IStatus.ERROR</code> indicating that it will
	 * @see IStatus#OK
	 * @since 3.6
	 */
	public IStatus validateFiltered(IResource resource);

	/**
	 * Validates the given path as the location of the given resource on disk.
	 * The path must be either an absolute file system path, or a relative path
	 * whose first segment is the name of a defined workspace path variable. In
	 * addition to the restrictions for paths in general (see <code>IPath.
	 * isValidPath</code>),
	 * a link location must also obey the following rules:
	 * <ul>
	 * <li>must not overlap with the platform's metadata directory</li>
	 * <li>must not be the same as or a parent of the root directory of the
	 * project the linked resource is contained in</li>
	 * </ul>
	 * <p>
	 * This method also checks that the given resource can legally become a
	 * linked resource. This includes the following restrictions:
	 * <ul>
	 * <li>must have a project as its immediate parent</li>
	 * <li>project natures and the team hook may disallow linked resources on
	 * projects they are associated with</li>
	 * <li>the global workspace preference to disable linking,
	 * <code>ResourcesPlugin.PREF_DISABLE_LINKING</code> must not be set to
	 * &quot;true&quot;</li>.
	 * </ul>
	 * </p>
	 * <p>
	 * This method will return a status with severity <code>IStatus.ERROR</code>
	 * if the location does not obey the above rules. Also, this method will
	 * return a status with severity <code>IStatus.WARNING</code> if the
	 * location overlaps the location of any existing resource in the workspace.
	 * </p>
	 * <p>
	 * Note: this method does not consider whether files or directories exist in
	 * the file system at the specified path.
	 * 
	 * @param resource the resource to validate the location for
	 * @param location the location of the linked resource contents on disk
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * location is valid as the linked resource location, otherwise a status
	 * object with severity <code>IStatus.WARNING</code> or
	 * <code>IStatus.ERROR</code> indicating what is wrong with the location
	 * @see IStatus#OK
	 * @see ResourcesPlugin#PREF_DISABLE_LINKING
	 * @since 2.1
	 */
	public IStatus validateLinkLocation(IResource resource, IPath location);

	/**
	 * Validates the given {@link URI} as the location of the given resource on disk.
	 * The location must be either an absolute URI, or a relative URI
	 * whose first segment is the name of a defined workspace path variable.
	 * A link location must obey the following rules:
	 * <ul>
	 * <li>must not overlap with the platform's metadata directory</li>
	 * <li>must not be the same as or a parent of the root directory of the
	 * project the linked resource is contained in</li>
	 * </ul>
	 * <p>
	 * This method also checks that the given resource can legally become a
	 * linked resource. This includes the following restrictions:
	 * <ul>
	 * <li>must have a project as its immediate parent</li>
	 * <li>project natures and the team hook may disallow linked resources on
	 * projects they are associated with</li>
	 * <li>the global workspace preference to disable linking,
	 * <code>ResourcesPlugin.PREF_DISABLE_LINKING</code> must not be set to
	 * &quot;true&quot;</li>.
	 * </ul>
	 * </p>
	 * <p>
	 * This method will return a status with severity <code>IStatus.ERROR</code>
	 * if the location does not obey the above rules. Also, this method will
	 * return a status with severity <code>IStatus.WARNING</code> if the
	 * location overlaps the location of any existing resource in the workspace.
	 * </p>
	 * <p>
	 * Note: this method does not consider whether files or directories exist in
	 * the file system at the specified location.
	 * 
	 * @param resource the resource to validate the location for
	 * @param location the location of the linked resource contents in some file system
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * location is valid as the linked resource location, otherwise a status
	 * object with severity <code>IStatus.WARNING</code> or
	 * <code>IStatus.ERROR</code> indicating what is wrong with the location
	 * @see IStatus#OK
	 * @see ResourcesPlugin#PREF_DISABLE_LINKING
	 * @since 3.2
	 */
	public IStatus validateLinkLocationURI(IResource resource, URI location);

	/**
	 * Validates the given string as the name of a resource valid for one of the
	 * given types.
	 * <p>
	 * In addition to the basic restrictions on paths in general (see
	 * {@link IPath#isValidSegment(String)}), a resource name must also not 
	 * contain any characters or substrings that are not valid on the file system 
	 * on which workspace root is located. In addition, the names "." and ".."
	 * are reserved due to their special meaning in file system paths.
	 * </p>
	 * <p>
	 * This validation check is done automatically as a resource is created (but
	 * not when the resource handle is constructed); this means that any
	 * resource that exists can be safely assumed to have a valid name and path.
	 * Note that the name of the workspace root resource is inherently invalid.
	 * </p>
	 * 
	 * @param segment the name segment to be checked
	 * @param typeMask bitwise-or of the resource type constants (
	 * <code>FILE</code>,<code>FOLDER</code>,<code>PROJECT</code> or
	 * <code>ROOT</code>) indicating expected resource type(s)
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * string is valid as a resource name, otherwise a status object indicating
	 * what is wrong with the string
	 * @see IResource#PROJECT
	 * @see IResource#FOLDER
	 * @see IResource#FILE
	 * @see IStatus#OK
	 */
	public IStatus validateName(String segment, int typeMask);

	/**
	 * Validates that each of the given natures exists, and that all nature
	 * constraints are satisfied within the given set.
	 * <p>
	 * The following conditions apply to validation of a set of natures:
	 * <ul>
	 * <li>all natures in the set exist in the plug-in registry
	 * <li>all prerequisites of each nature are present in the set
	 * <li>there are no cycles in the prerequisite graph of the set
	 * <li>there are no two natures in the set that specify one-of-nature
	 * inclusion in the same group.
	 * <li>there are no two natures in the set with the same id
	 * </ul>
	 * </p>
	 * <p>
	 * An empty nature set is always valid.
	 * </p>
	 * 
	 * @param natureIds an array of nature extension identifiers
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * set of natures is valid, otherwise a status object indicating what is
	 * wrong with the set
	 * @since 2.0
	 */
	public IStatus validateNatureSet(String[] natureIds);

	/**
	 * Validates the given string as a path for a resource of the given type(s).
	 * <p>
	 * In addition to the restrictions for paths in general (see
	 * <code>IPath.isValidPath</code>), a resource path should also obey the
	 * following rules:
	 * <ul>
	 * <li>a resource path should be an absolute path with no device id
	 * <li>its segments should be valid names according to
	 * <code>validateName</code>
	 * <li>a path for the workspace root must be the canonical root path
	 * <li>a path for a project should have exactly 1 segment
	 * <li>a path for a file or folder should have more than 1 segment
	 * <li>the first segment should be a valid project name
	 * <li>the second through penultimate segments should be valid folder names
	 * <li>the last segment should be a valid name of the given type
	 * </ul>
	 * </p>
	 * <p>
	 * Note: this method does not consider whether a resource at the specified
	 * path exists.
	 * </p>
	 * <p>
	 * This validation check is done automatically as a resource is created (but
	 * not when the resource handle is constructed); this means that any
	 * resource that exists can be safely assumed to have a valid name and path.
	 * </p>
	 * 
	 * @param path the path string to be checked
	 * @param typeMask bitwise-or of the resource type constants (
	 * <code>FILE</code>,<code>FOLDER</code>,<code>PROJECT</code>, or
	 * <code>ROOT</code>) indicating expected resource type(s)
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * path is valid as a resource path, otherwise a status object indicating
	 * what is wrong with the string
	 * @see IResource#PROJECT
	 * @see IResource#FOLDER
	 * @see IResource#FILE
	 * @see IStatus#OK
	 * @see IResourceStatus#getPath()
	 */
	public IStatus validatePath(String path, int typeMask);

	/**
	 * Validates the given path as the location of the given project on disk.
	 * The path must be either an absolute file system path, or a relative path
	 * whose first segment is the name of a defined workspace path variable. In
	 * addition to the restrictions for paths in general (see <code>IPath.
	 * isValidPath</code>),
	 * a location path should also obey the following rules:
	 * <ul>
	 * <li>must not be the same as another open or closed project</li>
	 * <li>must not occupy the default location for any project, whether existing or not</li>
	 * <li>must not be the same as or a parent of the platform's working directory</li>
	 * <li>must not be the same as or a child of the location of any existing
	 * linked resource in the given project</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Note: this method does not consider whether files or directories exist in
	 * the file system at the specified path.
	 * </p>
	 * 
	 * @param project the project to validate the location for, can be <code>null</code>
	 * if non default project location is validated
	 * @param location the location of the project contents on disk, or <code>null</code> 
	 * if the default project location is used
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * location is valid as the project content location, otherwise a status
	 * object indicating what is wrong with the location
	 * @see IProjectDescription#getLocationURI()
	 * @see IProjectDescription#setLocation(IPath)
	 * @see IStatus#OK
	 */
	public IStatus validateProjectLocation(IProject project, IPath location);

	/**
	 * Validates the given URI as the location of the given project.
	 * The location must be either an absolute URI, or a relative URI
	 * whose first segment is the name of a defined workspace path variable.
	 * A project location must obey the following rules:
	 * <ul>
	 * <li>must not be the same as another open or closed project</li>
	 * <li>must not occupy the default location for any project, whether existing or not</li>
	 * <li>must not be the same as or a parent of the platform's working directory</li>
	 * <li>must not be the same as or a child of the location of any existing
	 * linked resource in the given project</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Note: this method does not consider whether files or directories exist in
	 * the file system at the specified path.
	 * </p>
	 * 
	 * @param project the project to validate the location for, can be <code>null</code>
	 * if non default project location is validated
	 * @param location the location of the project contents on disk, or <code>null</code> 
	 * if the default project location is used
	 * @return a status object with code <code>IStatus.OK</code> if the given
	 * location is valid as the project content location, otherwise a status
	 * object indicating what is wrong with the location
	 * @see IProjectDescription#getLocationURI()
	 * @see IProjectDescription#setLocationURI(URI)
	 * @see IStatus#OK
	 * @since 3.2
	 */
	public IStatus validateProjectLocationURI(IProject project, URI location);

	/**
	 * Returns the path variable manager for this workspace.
	 * 
	 * @return the path variable manager
	 * @see IPathVariableManager
	 * @since 2.1
	 */
	public IPathVariableManager getPathVariableManager();
}
