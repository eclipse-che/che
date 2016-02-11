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
 * A data structure returned by {@link IWorkspace#addSaveParticipant(org.eclipse.core.runtime.Plugin, ISaveParticipant)}
 * containing a save number and an optional resource delta.
 *
 * @see IWorkspace#addSaveParticipant(org.eclipse.core.runtime.Plugin, ISaveParticipant)
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISavedState {
	/**
	 * Returns the files mapped with the {@link ISaveContext#map(IPath, IPath)}
	 * facility. Returns an empty array if there are no mapped files.
	 *
	 * @return the files currently mapped by the participant
	 *
	 * @see #lookup(IPath)
	 * @see ISaveContext#map(IPath, IPath)
	 */
	public IPath[] getFiles();

	/**
	 * Returns the save number for the save participant.
	 * This is the save number of the last successful save in which the plug-in
	 * <b>actively</b> participated, or <code>0</code> if the plug-in has
	 * never actively participated in a successful save.
	 *
	 * @return the save number
	 */
	public int getSaveNumber();

	/**
	 * Returns the mapped location associated with the given path 
	 * or <code>null</code> if none.
	 *
	 * @return the mapped location of a given path
	 * @see #getFiles()
	 * @see ISaveContext#map(IPath, IPath)
	 */
	public IPath lookup(IPath file);

	/**
	 * Used to receive notification of changes that might have happened
	 * while this plug-in was not active. The listener receives notifications of changes to 
	 * the workspace resource tree since the time this state was saved.  After this 
	 * method is run, the delta is forgotten. Subsequent calls to this method
	 * will have no effect.
	 * <p>
	 * No notification is received in the following cases:
	 * <ul>
	 * <li>if a saved state was never recorded ({@link ISaveContext#needDelta()}</code> 
	 * 		was not called) </li>
	 * <li>a saved state has since been forgotten (using {@link IWorkspace#forgetSavedTree(String)}) </li>
	 * <li>a saved state has been deemed too old or has become invalid</li>
	 * </ul>
	 * <p>
	 * All clients should have a contingency plan in place in case 
	 * a changes are not available (the case should be very similar
	 * to the first time a plug-in is activated, and only has the
	 * current state of the workspace to work from).
	 * </p>
	 * <p>
	 * The supplied event is of type {@link IResourceChangeEvent#POST_BUILD}
	 * and contains the delta detailing changes since this plug-in last participated
	 * in a save. This event object (and the resource delta within it) is valid only
	 * for the duration of the invocation of this method.
	 * </p>
	 *
	 * @param listener the listener
	 * @see ISaveContext#needDelta()
	 * @see IResourceChangeListener
	 */
	public void processResourceChangeEvents(IResourceChangeListener listener);
}
