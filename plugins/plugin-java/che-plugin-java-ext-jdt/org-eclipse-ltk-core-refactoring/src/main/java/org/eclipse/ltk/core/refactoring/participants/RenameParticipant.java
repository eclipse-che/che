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
package org.eclipse.ltk.core.refactoring.participants;

/**
 * A participant to participate in refactorings that rename elements. A rename
 * participant can't assume that its associated refactoring processor is a
 * rename processor. A rename operation might be a side effect of another
 * refactoring operation.
 * <p>
 * Rename participants are registered via the extension point <code>
 * org.eclipse.ltk.core.refactoring.renameParticipants</code>.
 * Extensions to this extension point must therefore extend this abstract class.
 * </p>
 *
 * @since 3.0
 */
public abstract class RenameParticipant extends RefactoringParticipant {

	private RenameArguments fArguments;

	/**
	 * {@inheritDoc}
	 */
	protected final void initialize(RefactoringArguments arguments) {
		fArguments= (RenameArguments)arguments;
	}

	/**
	 * Returns the rename arguments.
	 *
	 * @return the rename arguments
	 */
	public RenameArguments getArguments() {
		return fArguments;
	}
}
