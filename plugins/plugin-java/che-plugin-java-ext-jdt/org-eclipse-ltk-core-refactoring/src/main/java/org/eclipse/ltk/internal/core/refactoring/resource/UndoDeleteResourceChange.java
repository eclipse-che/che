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
package org.eclipse.ltk.internal.core.refactoring.resource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.resource.undostates.ResourceUndoState;

/**
 * Undo a resource delete change.  This uses the {@link ResourceUndoState}
 * to reverse the change.
 *
 * @since 3.4
 */
public class UndoDeleteResourceChange extends Change {

	private final ResourceUndoState fResourceState;

	public UndoDeleteResourceChange(ResourceUndoState resourceDescription) {
		fResourceState= resourceDescription;
	}

	public void initializeValidationData(IProgressMonitor pm) {
	}

	public Object getModifiedElement() {
		return null;
	}

	public String getName() {
		return Messages.format(RefactoringCoreMessages.UndoDeleteResourceChange_change_name, BasicElementLabels.getResourceName(fResourceState.getName()));
	}

	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (!fResourceState.isValid()) {
			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.UndoDeleteResourceChange_cannot_restore, BasicElementLabels.getResourceName(fResourceState.getName())));
		}
		return new RefactoringStatus();
	}

	public Change perform(IProgressMonitor pm) throws CoreException {
		if (fResourceState.verifyExistence(true)) {
			String message= Messages.format(RefactoringCoreMessages.UndoDeleteResourceChange_already_exists, BasicElementLabels.getResourceName(fResourceState.getName()));
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), message));
		}

		IResource created= fResourceState.createResource(pm);
		created.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(pm, 1));
		DeleteResourceChange change= new DeleteResourceChange(created.getFullPath(), true, false);
		change.setValidationMethod(ResourceChange.VALIDATE_NOT_READ_ONLY | ResourceChange.VALIDATE_NOT_DIRTY);
		return change;
	}

	public String toString() {
		return Messages.format(RefactoringCoreMessages.UndoDeleteResourceChange_revert_resource, fResourceState.getName());
	}
}
