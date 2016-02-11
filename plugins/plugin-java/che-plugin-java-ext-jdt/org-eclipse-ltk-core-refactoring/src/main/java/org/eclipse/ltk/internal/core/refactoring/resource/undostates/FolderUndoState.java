/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ltk.internal.core.refactoring.resource.undostates;

import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * {@link FolderUndoState} is a lightweight description that describes a folder to be
 * created.
 *
 * This class is not intended to be instantiated or used by clients.
 *
 * @since 3.4
 *
 */
public class FolderUndoState extends ContainerUndoState {

	/**
	 * Create a {@link FolderUndoState} from the specified folder handle. Typically
	 * used when the folder handle represents a resource that actually exists,
	 * although it will not fail if the resource is non-existent.
	 *
	 * @param folder
	 *            the folder to be described
	 */
	public FolderUndoState(IFolder folder) {
		super(folder);
	}

	/**
	 * Create a {@link FolderUndoState} from the specified folder handle. If the
	 * folder to be created should be linked to a different location, specify
	 * the location.
	 *
	 * @param folder
	 *            the folder to be described
	 * @param linkLocation
	 *            the location to which the folder is linked, or
	 *            <code>null</code> if it is not linked
	 */
	public FolderUndoState(IFolder folder, URI linkLocation) {
		super(folder);
		this.name = folder.getName();
		this.location = linkLocation;
	}

	public IResource createResourceHandle() {
		IWorkspaceRoot workspaceRoot = getWorkspace().getRoot();
		IPath folderPath = parent.getFullPath().append(name);
		return workspaceRoot.getFolder(folderPath);
	}

	public void createExistentResourceFromHandle(IResource resource,
			IProgressMonitor monitor) throws CoreException {

		Assert.isLegal(resource instanceof IFolder);
		if (resource.exists()) {
			return;
		}
		IFolder folderHandle = (IFolder) resource;
		try {
			monitor.beginTask("", 200); //$NON-NLS-1$
			monitor.setTaskName(RefactoringCoreMessages.FolderDescription_NewFolderProgress);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (location != null) {
				folderHandle.createLink(location,
						IResource.ALLOW_MISSING_LOCAL, new SubProgressMonitor(
								monitor, 100));
			} else {
				folderHandle.create(false, true, new SubProgressMonitor(
						monitor, 100));
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			createChildResources(folderHandle, monitor, 100);

		} finally {
			monitor.done();
		}
	}
}
