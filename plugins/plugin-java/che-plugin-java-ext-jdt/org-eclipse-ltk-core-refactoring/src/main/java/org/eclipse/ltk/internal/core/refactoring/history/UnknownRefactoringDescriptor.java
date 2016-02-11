/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * Refactoring descriptor to denote the unknown refactoring.
 *
 * @since 3.2
 */
public final class UnknownRefactoringDescriptor extends RefactoringDescriptor {

	/**
	 * Creates a new unknown refactoring descriptor.
	 *
	 * @param name
	 *            the name of the change, must not be <code>null</code> or empty
	 */
	public UnknownRefactoringDescriptor(final String name) {
		super(ID_UNKNOWN, null, name, null, RefactoringDescriptor.NONE);
	}

	/**
	 * {@inheritDoc}
	 */
	public Refactoring createRefactoring(final RefactoringStatus status) throws CoreException {
		status.merge(RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.UnknownRefactoringDescriptor_cannot_create_refactoring));
		return null;
	}
}
