/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

/**
 * A workspace description represents the workspace preferences. It can be
 * used to query the current preferences and set new ones. The workspace
 * preference values are stored in the preference store and are also accessible
 * via the preference mechanism. Constants for the preference keys are found
 * on the <code>ResourcesPlugin</code> class.
 *
 * @see IWorkspace#getDescription()
 * @see IWorkspace#setDescription(IWorkspaceDescription)
 * @see IWorkspace#newProjectDescription(String)
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IWorkspaceDescription {
	/**
	 * Returns the order in which projects in the workspace should be built.
	 * The returned value is <code>null</code> if the workspace's default build
	 * order is being used.
	 *
	 * @return the names of projects in the order they will be built, 
	 *   or <code>null</code> if the default build order should be used
	 * @see #setBuildOrder(String[])
	 * @see ResourcesPlugin#PREF_BUILD_ORDER
	 */
	public String[] getBuildOrder();

	/**
	 * Returns the maximum length of time, in milliseconds, a file state should be 
	 * kept in the local history. This setting is ignored by the workspace when
	 * <code>isApplyFileStatePolicy()</code> returns <code>false</code>.
	 * 
	 * @return the maximum time a file state should be kept in the local history
	 *   represented in milliseconds
	 * @see #setFileStateLongevity(long)
	 * @see #isApplyFileStatePolicy()
	 * @see ResourcesPlugin#PREF_FILE_STATE_LONGEVITY
	 */
	public long getFileStateLongevity();

	/**
	 * Returns the maximum number of times that the workspace should rebuild when
	 * builders affect projects that have already been built.
	 * 
	 * @return the maximum number of times that the workspace should rebuild when
	 * builders affect projects that have already been built.
	 * @see #setMaxBuildIterations(int)
	 * @see ResourcesPlugin#PREF_MAX_BUILD_ITERATIONS
	 * @since 2.1
	 */
	public int getMaxBuildIterations();

	/**
	 * Returns the maximum number of states per file that can be stored in the local history.
	 * This setting is ignored by the workspace when <code>isApplyFileStatePolicy()</code>
	 * returns <code>false</code>.
	 *
	 * @return the maximum number of states per file that can be stored in the local history
	 * @see #setMaxFileStates(int)
	 * @see #isApplyFileStatePolicy()
	 * @see ResourcesPlugin#PREF_MAX_FILE_STATES
	 */
	public int getMaxFileStates();

	/**
	 * Returns the maximum permitted size of a file, in bytes, to be stored in the
	 * local history. This setting is ignored by the workspace when 
	 * <code>isApplyFileStatePolicy()</code> returns <code>false</code>.
	 *
	 * @return the maximum permitted size of a file to be stored in the local history
	 * @see #setMaxFileStateSize(long)
	 * @see #isApplyFileStatePolicy()
	 * @see ResourcesPlugin#PREF_MAX_FILE_STATE_SIZE
	 */
	public long getMaxFileStateSize();

	/**
	 * Returns whether file states are discarded according to the policy specified by
	 * <code>setFileStateLongevity(long)</code>, <code>setMaxFileStates(int)</code>
	 * and <code>setMaxFileStateSize(long)</code> methods. 
	 * 
	 * @return <code>true</code> if file states are removed due to the policy, 
	 * 		<code>false</code> otherwise
	 * @see #setApplyFileStatePolicy(boolean)
	 * @see #setFileStateLongevity(long)
	 * @see #setMaxFileStates(int)
	 * @see #setMaxFileStateSize(long)
	 * @see ResourcesPlugin#PREF_APPLY_FILE_STATE_POLICY
	 * @since 3.6
	 */
	public boolean isApplyFileStatePolicy();

	/**
	 * Returns the interval between automatic workspace snapshots.
	 *
	 * @return the amount of time in milliseconds between automatic workspace snapshots
	 * @see #setSnapshotInterval(long)
	 * @see ResourcesPlugin#PREF_SNAPSHOT_INTERVAL
	 * @since 2.0
	 */
	public long getSnapshotInterval();

	/**
	 * Returns whether this workspace performs autobuilds.
	 *
	 * @return <code>true</code> if autobuilding is on, otherwise
	 *		<code>false</code>
	 * @see #setAutoBuilding(boolean)
	 * @see ResourcesPlugin#PREF_AUTO_BUILDING
	 */
	public boolean isAutoBuilding();

	/**
	 * Records whether this workspace performs autobuilds.
	 * <p>
	 * When autobuild is on, any changes made to a project and its
	 * resources automatically triggers an incremental build of the workspace.
	 * </p>
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param value <code>true</code> to turn on autobuilding,
	 *  and <code>false</code> to turn it off
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #isAutoBuilding()
	 * @see ResourcesPlugin#PREF_AUTO_BUILDING
	 */
	public void setAutoBuilding(boolean value);

	/**
	 * Sets the order in which projects in the workspace should be built.
	 * Projects not named in this list are built in a default order defined
	 * by the workspace.  Set this value to <code>null</code> to use the
	 * default ordering for all projects.  Projects not named in the list are 
	 * built in unspecified order after all ordered projects.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param value the names of projects in the order in which they are built,
	 *   or <code>null</code> to use the workspace's default order for all projects
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getBuildOrder()
	 * @see ResourcesPlugin#PREF_BUILD_ORDER
	 */
	public void setBuildOrder(String[] value);

	/**
	 * Sets the maximum time, in milliseconds, a file state should be kept in the
	 * local history. This setting is ignored by the workspace when <code>setApplyFileStatePolicy(boolean)
	 * </code> is set to false.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param time the maximum number of milliseconds a file state should be 
	 * 		kept in the local history
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getFileStateLongevity()
	 * @see #setApplyFileStatePolicy(boolean)
	 * @see ResourcesPlugin#PREF_FILE_STATE_LONGEVITY
	 */
	public void setFileStateLongevity(long time);

	/**
	 * Sets the maximum number of times that the workspace should rebuild when
	 * builders affect projects that have already been built.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param number the maximum number of times that the workspace should rebuild
	 * when builders affect projects that have already been built.
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getMaxBuildIterations()
	 * @see ResourcesPlugin#PREF_MAX_BUILD_ITERATIONS
	 * @since 2.1
	 */
	public void setMaxBuildIterations(int number);

	/**
	 * Sets the maximum number of states per file that can be stored in the local history.
	 * If the maximum number is reached, older states are removed in favor of
	 * new ones. This setting is ignored by the workspace when <code>setApplyFileStatePolicy(boolean)
	 * </code> is set to <code>false</code>.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param number the maximum number of states per file that can be stored in the local history
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getMaxFileStates()
	 * @see #setApplyFileStatePolicy(boolean)
	 * @see ResourcesPlugin#PREF_MAX_FILE_STATES
	 */
	public void setMaxFileStates(int number);

	/**
	 * Sets the maximum permitted size of a file, in bytes, to be stored in the
	 * local history. This setting is ignored by the workspace when <code>setApplyFileStatePolicy(boolean)
	 * </code> is set to <code>false</code>.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param size the maximum permitted size of a file to be stored in the local history
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getMaxFileStateSize()
	 * @see #setApplyFileStatePolicy(boolean)
	 * @see ResourcesPlugin#PREF_MAX_FILE_STATE_SIZE
	 */
	public void setMaxFileStateSize(long size);

	/**
	 * Sets whether file states are discarded according to the policy specified by
	 * <code>setFileStateLongevity(long)</code>, <code>setMaxFileStates(int)</code>
	 * and <code>setMaxFileStateSize(long)</code> methods.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param apply <code>true</code> if file states are removed due to the policy, 
	 * 		<code>false</code> otherwise
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #setFileStateLongevity(long)
	 * @see #setMaxFileStates(int)
	 * @see #setMaxFileStateSize(long)
	 * @see #isApplyFileStatePolicy()
	 * @see ResourcesPlugin#PREF_APPLY_FILE_STATE_POLICY
	 * @since 3.6
	 */
	public void setApplyFileStatePolicy(boolean apply);

	/**
	 * Sets the interval between automatic workspace snapshots.  The new interval
	 * will only take effect after the next snapshot.
	 * <p>
	 * Users must call <code>IWorkspace.setDescription</code> before changes 
	 * made to this description take effect.
	 * </p>
	 *
	 * @param delay the amount of time in milliseconds between automatic workspace snapshots
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 * @see #getSnapshotInterval()
	 * @see ResourcesPlugin#PREF_SNAPSHOT_INTERVAL
	 * @since 2.0
	 */
	public void setSnapshotInterval(long delay);
}
