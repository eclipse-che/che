/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;

import org.eclipse.text.edits.MalformedTreeException;

import org.eclipse.jface.text.BadLocationException;

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class Changes {

	public static RefactoringStatus validateModifiesFiles(IFile[] filesToModify) {
		RefactoringStatus result= new RefactoringStatus();
		IStatus status= Resources.checkInSync(filesToModify);
		if (!status.isOK())
			result.merge(RefactoringStatus.create(status));
		status= Resources.makeCommittable(filesToModify, null);
		if (!status.isOK()) {
			result.merge(RefactoringStatus.create(status));
			if (!result.hasFatalError()) {
				result.addFatalError(RefactoringCoreMessages.Changes_validateEdit);
			}
		}
		return result;
	}

	public static RefactoringStatus checkInSync(IFile[] filesToModify) {
		RefactoringStatus result= new RefactoringStatus();
		IStatus status= Resources.checkInSync(filesToModify);
		if (!status.isOK())
			result.merge(RefactoringStatus.create(status));
		return result;
	}

	public static CoreException asCoreException(BadLocationException e) {
		String message= e.getMessage();
		if (message == null)
			message= "BadLocationException"; //$NON-NLS-1$
		return new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IRefactoringCoreStatusCodes.BAD_LOCATION, message, e));
	}

	public static CoreException asCoreException(MalformedTreeException e) {
		String message= e.getMessage();
		if (message == null)
			message= "MalformedTreeException"; //$NON-NLS-1$
		return new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IRefactoringCoreStatusCodes.BAD_LOCATION, message, e));
	}
}
