/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
 * A participant to participate in refactorings that copy elements to a shared
 * clipboard. A copy participant can't assume that its associated refactoring
 * processor is a copy processor. A copy operation might be a side effect of another
 * refactoring operation.
 * <p>
 * Copy participants are registered via the extension point <code>
 * org.eclipse.ltk.core.refactoring.copyParticipants</code>.
 * Extensions to this extension point must therefore extend this abstract class.
 * </p>
 *
 * @since 3.1
 */
public abstract class CopyParticipant extends RefactoringParticipant {

	private CopyArguments fArguments;

	/**
	 * {@inheritDoc}
	 */
	protected final void initialize(RefactoringArguments arguments) {
		fArguments= (CopyArguments)arguments;
	}

	/**
	 * Returns the copy arguments.
	 *
	 * @return the copy arguments
	 */
	public CopyArguments getArguments() {
		return fArguments;
	}
}
