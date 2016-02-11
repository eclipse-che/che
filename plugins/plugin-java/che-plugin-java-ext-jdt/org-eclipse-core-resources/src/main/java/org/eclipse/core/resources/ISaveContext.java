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

import org.eclipse.core.runtime.IPath;

/**
 * A context for workspace <code>save</code> operations.
 * <p>
 * Note that <code>IWorkspace.save</code> uses a
 * different save context for each registered participant,
 * allowing each to declare whether they have actively
 * participated and decide whether to receive a resource 
 * delta on reactivation.
 * </p>
 *
 * @see IWorkspace#save(boolean, org.eclipse.core.runtime.IProgressMonitor)
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISaveContext {

	/*====================================================================
	 * Constants related to save kind
	 *====================================================================*/

	/** 
	 * Type constant which identifies a full save.
	 *
	 * @see ISaveContext#getKind()
	 */
	public static final int FULL_SAVE = 1;

	/** 
	 * Type constant which identifies a snapshot.
	 *
	 * @see ISaveContext#getKind()
	 */
	public static final int SNAPSHOT = 2;

	/** 
	 * Type constant which identifies a project save.
	 *
	 * @see ISaveContext#getKind()
	 */
	public static final int PROJECT_SAVE = 3;

	/**
	 * Returns current files mapped with the <code>ISaveContext.map</code>
	 * facility or an empty array if there are no mapped files.
	 *
	 * @return the files currently mapped by the participant
	 *
	 * @see #map(IPath, IPath)
	 */
	public IPath[] getFiles();

	/**
	 * Returns the type of this save. The types can be:
	 * <ul>
	 * <li> <code>ISaveContext.FULL_SAVE</code></li>
	 * <li> <code>ISaveContext.SNAPSHOT</code></li>
	 * <li> <code>ISaveContext.PROJECT_SAVE</code></li>
	 * </ul>
	 *
	 * @return the type of the current save
	 */
	public int getKind();

	/**
	 * Returns the number for the previous save in
	 * which the plug-in actively participated, or <code>0</code>
	 * if the plug-in has never actively participated in a save before.
	 * <p>
	 * In the event of an unsuccessful save, this is the value to 
	 * <code>rollback</code> to.
	 * </p>
	 *
	 * @return the previous save number if positive, or <code>0</code> 
	 *		if never saved before
	 * @see ISaveParticipant#rollback(ISaveContext)
	 */
	public int getPreviousSaveNumber();

	/**
	 * If the current save is a project save, this method returns the project
	 * being saved.
	 *
	 * @return the project being saved or <code>null</code> if this is not
	 *   project save
	 *
	 * @see #getKind()
	 */
	public IProject getProject();

	/**
	 * Returns the number for this save. This number is
	 * guaranteed to be <code>1</code> more than the 
	 * previous save number.
	 * <p>
	 * This is the value to use when, for example, creating files
	 * in which a participant will save its data.
	 * </p>
	 *
	 * @return the save number
	 * @see ISaveParticipant#saving(ISaveContext)
	 */
	public int getSaveNumber();

	/**
	 * Returns the current location for the given file or 
	 * <code>null</code> if none.
	 *
	 * @return the location of a given file or <code>null</code>
	 * @see #map(IPath, IPath)
	 * @see ISavedState#lookup(IPath)
	 */
	public IPath lookup(IPath file);

	/**
	 * Maps the given plug-in file to its real location. This method is intended to be used
	 * with <code>ISaveContext.getSaveNumber()</code> to map plug-in configuration
	 * file names to real locations.
	 * <p>
	 * For example, assume a plug-in has a configuration file named "config.properties".
	 * The map facility can be used to map that logical name onto a real
	 * name which is specific to a particular save (e.g., 10.config.properties,
	 * where 10 is the current save number).  The paths specified here should
	 * always be relative to the plug-in state location for the plug-in saving the state.
	 * </p>
	 * <p>
	 * Each save participant must manage the deletion of its old state files.  Old state files
	 * can be discovered using <code>getPreviousSaveNumber</code> or by using
	 * <code>getFiles</code> to discover the current files and comparing that to the 
	 * list of files on disk.
	 * </p>
	 * @param file the logical name of the participant's data file
	 * @param location the real (i.e., filesystem) name by which the file should be known 
	 *		for this save, or <code>null</code> to remove the entry
	 * @see #lookup(IPath)
	 * @see #getSaveNumber()
	 * @see #needSaveNumber()
	 * @see ISavedState#lookup(IPath)
	 */
	public void map(IPath file, IPath location);

	/**
	 * Indicates that the saved workspace tree should be remembered so that a delta
	 * will be available in a subsequent session when the plug-in re-registers
	 * to participate in saves. If this method is not called, no resource delta will 
	 * be made available. This facility is not available for marker deltas.
	 * Plug-ins must assume that all markers may have changed when they are activated.
	 * <p>
	 * Note that this is orthogonal to <code>needSaveNumber</code>. That is,
	 * one can ask for a delta regardless of whether or not one is an active participant.
	 * </p>
	 * <p>
	 * Note that deltas are not guaranteed to be saved even if saving is requested.
	 * Deltas cannot be supplied where the previous state is too old or has become invalid.
	 * </p>
	 * <p>
	 * This method is only valid for full saves. It is ignored during snapshots
	 * or project saves.
	 * </p>
	 *
	 * @see IWorkspace#addSaveParticipant(org.eclipse.core.runtime.Plugin, ISaveParticipant)
	 * @see ISavedState#processResourceChangeEvents(IResourceChangeListener)
	 */
	public void needDelta();

	/**
	 * Indicates that this participant has actively participated in this save.
	 * If the save is successful, the current save number will be remembered;
	 * this save number will be the previous save number for subsequent saves
	 * until the participant again actively participates.
	 * <p>
	 * If this method is not called, the plug-in is not deemed to be an active
	 * participant in this save.
	 * </p>
	 * <p>
	 * Note that this is orthogonal to <code>needDelta</code>. That is,
	 * one can be an active participant whether or not one asks for a delta.
	 * </p>
	 *
	 * @see IWorkspace#addSaveParticipant(org.eclipse.core.runtime.Plugin, ISaveParticipant)
	 * @see ISavedState#getSaveNumber()
	 */
	public void needSaveNumber();
}
