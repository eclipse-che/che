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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * {@link Change} that moves a resource.
 *
 * @since 3.4
 */
public class MoveResourceChange extends ResourceChange {

	private final IResource fSource;
	private final IContainer fTarget;
	private final long fStampToRestore;
	private final Change fRestoreSourceChange;

	private ChangeDescriptor fDescriptor;

	/**
	 * Creates the change.
	 *
	 * @param source the resource to move
	 * @param target the container the resource is moved to. An existing resource at the destination will be
	 * replaced.
	 */
	public MoveResourceChange(IResource source, IContainer target) {
		this(source, target, IResource.NULL_STAMP, null);
	}

	/**
	 * Creates the change.
	 *
	 * @param source the resource to move
	 * @param target the container the resource is moved to. An existing resource at the destination will be
	 * replaced.
	 * @param stampToRestore the stamp to restore on the moved resource
	 * 	@param restoreSourceChange the change to restore a resource at the source or <code>null</code> if no resource
	 * needs to be resourced.
	 */
	protected MoveResourceChange(IResource source, IContainer target, long stampToRestore, Change restoreSourceChange) {
		fSource= source;
		fTarget= target;
		fStampToRestore= stampToRestore;
		fRestoreSourceChange= restoreSourceChange;

		// We already present a dialog to the user if he
		// moves read-only resources. Since moving a resource
		// doesn't do a validate edit (it actually doesn't
		// change the content we can't check for READ only
		// here.
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
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final Change perform(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {
			if (monitor == null)
				monitor= new NullProgressMonitor();

			monitor.beginTask(getName(), 4);

			Change deleteUndo= null;

			// delete destination if required
			IResource resourceAtDestination= fTarget.findMember(fSource.getName());
			if (resourceAtDestination != null && resourceAtDestination.exists()) {
				deleteUndo= performDestinationDelete(resourceAtDestination, new SubProgressMonitor(monitor, 1));
			} else {
				monitor.worked(1);
			}

			// move resource
			long currentStamp= fSource.getModificationStamp();
			IPath destinationPath= fTarget.getFullPath().append(fSource.getName());
			fSource.move(destinationPath, IResource.KEEP_HISTORY | IResource.SHALLOW, new SubProgressMonitor(monitor, 2));
			resourceAtDestination= ResourcesPlugin.getWorkspace().getRoot().findMember(destinationPath);

			// restore timestamp at destination
			if (fStampToRestore != IResource.NULL_STAMP) {
				resourceAtDestination.revertModificationStamp(fStampToRestore);
			}

			// restore file at source
			if (fRestoreSourceChange != null) {
				performSourceRestore(new SubProgressMonitor(monitor, 1));
			} else {
				monitor.worked(1);
			}
			return new MoveResourceChange(resourceAtDestination, fSource.getParent(), currentStamp, deleteUndo);
		} finally {
			monitor.done();
		}
	}

	private Change performDestinationDelete(IResource newResource, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(RefactoringCoreMessages.MoveResourceChange_progress_delete_destination, 3);
		try {
			DeleteResourceChange deleteChange= new DeleteResourceChange(newResource.getFullPath(), true);
			deleteChange.initializeValidationData(new SubProgressMonitor(monitor, 1));
			RefactoringStatus deleteStatus= deleteChange.isValid(new SubProgressMonitor(monitor, 1));
			if (!deleteStatus.hasFatalError()) {
				return deleteChange.perform(new SubProgressMonitor(monitor, 1));
			}
			return null;
		} finally {
			monitor.done();
		}
	}

	private void performSourceRestore(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(RefactoringCoreMessages.MoveResourceChange_progress_restore_source, 3);
		try {
			fRestoreSourceChange.initializeValidationData(new SubProgressMonitor(monitor, 1));
			RefactoringStatus restoreStatus= fRestoreSourceChange.isValid(new SubProgressMonitor(monitor, 1));
			if (!restoreStatus.hasFatalError()) {
				fRestoreSourceChange.perform(new SubProgressMonitor(monitor, 1));
			}
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.resource.ResourceChange#getModifiedResource()
	 */
	protected IResource getModifiedResource() {
		return fSource;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return Messages.format(RefactoringCoreMessages.MoveResourceChange_name, new String[] { BasicElementLabels.getPathLabel(fSource.getFullPath(), false), BasicElementLabels.getResourceName(fTarget) });
	}
}