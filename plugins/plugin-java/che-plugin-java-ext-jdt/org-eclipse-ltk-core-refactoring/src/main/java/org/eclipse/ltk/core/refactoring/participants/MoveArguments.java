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

import org.eclipse.core.runtime.Assert;

/**
 * Move arguments describe the data that a processor
 * provides to its move participants.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MoveArguments extends RefactoringArguments {

	private Object fDestination;
	private boolean fUpdateReferences;

	/**
	 * Creates new move arguments.
	 *
	 * @param destination the destination of the move
	 * @param updateReferences <code>true</code> if reference
	 *  updating is requested; <code>false</code> otherwise
	 */
	public MoveArguments(Object destination, boolean updateReferences) {
		Assert.isNotNull(destination);
		fDestination= destination;
		fUpdateReferences= updateReferences;
	}

	/**
	 * Returns the destination of the move
	 *
	 * @return the move's destination
	 */
	public Object getDestination() {
		return fDestination;
	}

	/**
	 * Returns whether reference updating is requested or not.
	 *
	 * @return returns <code>true</code> if reference
	 *  updating is requested; <code>false</code> otherwise
	 */
	public boolean getUpdateReferences() {
		return fUpdateReferences;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.2
	 */
	public String toString() {
		return "move to " //$NON-NLS-1$
				+ (fUpdateReferences ? "(update references) " : "(don't update references) ")  //$NON-NLS-1$//$NON-NLS-2$
				+ fDestination.toString();
	}
}
