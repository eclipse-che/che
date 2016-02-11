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
package org.eclipse.ltk.core.refactoring;

/**
 * A {@link RefactoringChangeDescriptor} describes changes created by a
 * refactoring. Changes created by a refactoring should provide an appropriate
 * refactoring change descriptor, which allows to completely reconstruct the
 * particular refactoring instance from the encapsulated refactoring descriptor.
 * <p>
 * Note: this class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefactoringChangeDescriptor extends ChangeDescriptor {

	/** The refactoring descriptor */
	private final RefactoringDescriptor fRefactoringDescriptor;

	/**
	 * Creates the <code>RefactoringChangeDescriptor</code> with the
	 * {@link RefactoringDescriptor} that originated the change.
	 *
	 * @param descriptor
	 *            the {@link RefactoringDescriptor} that originated the change.
	 */
	public RefactoringChangeDescriptor(final RefactoringDescriptor descriptor) {
		fRefactoringDescriptor= descriptor;
	}

	/**
	 * Returns the {@link RefactoringDescriptor} that originated the change.
	 *
	 * @return the {@link RefactoringDescriptor} that originated the change.
	 */
	public RefactoringDescriptor getRefactoringDescriptor() {
		return fRefactoringDescriptor;
	}
}
