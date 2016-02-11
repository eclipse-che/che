/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ltk.internal.core.refactoring.resource.undostates;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;


/**
 * {@link ResourceUndoState} is a lightweight state object that describes the common
 * attributes of a resource to be created.
 *
 * This class is not intended to be extended by clients.
 *
 * @since 3.4
 *
 */
public abstract class ResourceUndoState {

	/**
	 * Create a {@link ResourceUndoState} object given the specified resource. The resource
	 * is assumed to exist.
	 *
	 * @param resource
	 *            the resource from which a state should be created
	 * @return the resource state
	 */
	public static ResourceUndoState fromResource(IResource resource) {
		if (resource.getType() == IResource.PROJECT) {
			return new ProjectUndoState((IProject) resource);
		} else if (resource.getType() == IResource.FOLDER) {
			return new FolderUndoState((IFolder) resource);
		} else if (resource.getType() == IResource.FILE) {
			return new FileUndoState((IFile) resource);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Create a resource handle that can be used to create a resource from this
	 * resource state. This handle can be used to create the actual
	 * resource, or to describe the creation to a resource delta factory.
	 *
	 * @return the resource handle that can be used to create a resource from
	 *         this state object
	 */
	public abstract IResource createResourceHandle();

	/**
	 * Get the name of this resource.
	 *
	 * @return the name of the Resource
	 */
	public abstract String getName();

	/**
	 * Create an existent resource from this resource state.
	 *
	 * @param monitor
	 *            the progress monitor to use
	 * @return a resource that has the attributes of this resource state
	 * @throws CoreException if creation failed
	 */
	public abstract IResource createResource(IProgressMonitor monitor) throws CoreException;

	/**
	 * Given a resource handle, create an actual resource with the attributes of
	 * the receiver resource state.
	 *
	 * @param resource
	 *            the resource handle
	 * @param monitor
	 *            the progress monitor to be used when creating the resource
	 * @throws CoreException if creation failed
	 */
	public abstract void createExistentResourceFromHandle(IResource resource, IProgressMonitor monitor) throws CoreException;

	/**
	 * Return a boolean indicating whether this resource state has enough
	 * information to create a resource.
	 *
	 * @return <code>true</code> if the resource can be created, and
	 *         <code>false</code> if it does not have enough information
	 */
	public abstract boolean isValid();

	/**
	 * Record the appropriate state of this resource state using
	 * any available resource history.
	 *
	 * @param resource
	 *            the resource whose state is to be recorded.
	 * @param monitor
	 *            the progress monitor to be used
	 * @throws CoreException if history could not be read
	 */
	public abstract void recordStateFromHistory(IResource resource, IProgressMonitor monitor) throws CoreException;

	/**
	 * Return a boolean indicating whether this state represents an
	 * existent resource.
	 *
	 * @param checkMembers
	 *            Use <code>true</code> if members should also exist in order
	 *            for this state to be considered existent. A value of
	 *            <code>false</code> indicates that the existence of members
	 *            does not matter.
	 *
	 * @return a boolean indicating whether this state represents an
	 *         existent resource.
	 */
	public abstract boolean verifyExistence(boolean checkMembers);
}
