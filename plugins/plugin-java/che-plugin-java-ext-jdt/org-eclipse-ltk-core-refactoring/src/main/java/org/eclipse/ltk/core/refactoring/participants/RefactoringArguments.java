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
 * A generic super class of all refactoring specific argument
 * classes.
 * <p>
 * The main purpose of this class is to not use <code>Object</code>
 * in signatures. This helps to distinguish between the element to
 * be refactored and the arguments needed to carry out the refactoring.
 * </p>
 * <p>
 * This class should be subclassed by clients wishing to provide special
 * refactoring arguments for special participants.
 * </p>
 *
 * @since 3.0
 */
public abstract class RefactoringArguments {

	/**
	 * Creates new refactoring arguments.
	 */
	protected RefactoringArguments() {
	}

	/**
	 * Returns a string representation of these arguments suitable for debugging
	 * purposes only.
	 * <p>
	 * Subclasses should reimplement this method.
	 * </p>
	 *
	 * @return a debug string
	 * @since 3.2
	 */
	public String toString() {
		return super.toString();
	}
}
