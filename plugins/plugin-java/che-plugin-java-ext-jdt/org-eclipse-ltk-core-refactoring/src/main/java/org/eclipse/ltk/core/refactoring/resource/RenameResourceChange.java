/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.resource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * {@link Change} that renames a resource.
 *
 * @since 3.4
 */
public class RenameResourceChange extends ResourceChange {

	private final String fNewName;
	private final IPath fResourcePath;
	private final long fStampToRestore;

	private ChangeDescriptor fDescriptor;

	/**
	 * Creates the change.
	 *
	 * @param resourcePath the path of the resource to rename
	 * @param newName the new name. Must not be empty.
	 */
	public RenameResourceChange(IPath resourcePath, String newName) {
		this(resourcePath, newName, IResource.NULL_STAMP);
	}

	/**
	 * Creates the change with a time stamp to restore.
	 *
	 * @param resourcePath  the path of the resource to rename
	 * @param newName the new name. Must not be empty.
	 * @param stampToRestore the time stamp to restore or {@link IResource#NULL_STAMP} to not restore the
	 * time stamp.
	 */
	protected RenameResourceChange(IPath resourcePath, String newName, long stampToRestore) {
		if (resourcePath == null || newName == null || newName.length() == 0) {
			throw new IllegalArgumentException();
		}

		fResourcePath= resourcePath;
		fNewName= newName;
		fStampToRestore= stampToRestore;
		fDescriptor= null;
		setValidationMethod(VALIDATE_NOT_DIRTY);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getDescriptor()
	 */
	public ChangeDescriptor getDescriptor() {
		return fDescriptor;
	}

	/**
	 * Sets the change descriptor to be returned by {@link Change#getDescriptor()}.
	 *
	 * @param descriptor the change descriptor
	 */
	public void setDescriptor(ChangeDescriptor descriptor) {
		fDescriptor= descriptor;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.resource.ResourceChange#getModifiedResource()
	 */
	protected IResource getModifiedResource() {
		return getResource();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return Messages.format(RefactoringCoreMessages.RenameResourceChange_name, new String[] { BasicElementLabels.getPathLabel(fResourcePath, false), BasicElementLabels.getResourceName(fNewName) });
	}

	/**
	 * Returns the new name.
	 *
	 * @return return the new name
	 */
	public String getNewName() {
		return fNewName;
	}

	private IResource getResource() {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(fResourcePath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		try {
			pm.beginTask(RefactoringCoreMessages.RenameResourceChange_progress_description, 1);

			IResource resource= getResource();
			long currentStamp= resource.getModificationStamp();
			IPath newPath= renamedResourcePath(fResourcePath, fNewName);
			resource.move(newPath, IResource.SHALLOW, pm);
			if (fStampToRestore != IResource.NULL_STAMP) {
				IResource newResource= ResourcesPlugin.getWorkspace().getRoot().findMember(newPath);
				newResource.revertModificationStamp(fStampToRestore);
			}
			String oldName= fResourcePath.lastSegment();
			return new RenameResourceChange(newPath, oldName, currentStamp);
		} finally {
			pm.done();
		}
	}

	private static IPath renamedResourcePath(IPath path, String newName) {
		return path.removeLastSegments(1).append(newName);
	}

}
