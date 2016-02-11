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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A condition checker can be used to share condition checks
 * across the main processor and all its associated participants.
 * <p>
 * This interface should be implemented by clients wishing to provide a
 * special refactoring processor with special shared condition checks.
 * </p>
 *
 * @see org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext
 *
 * @since 3.0
 */
public interface IConditionChecker {

	/**
	 * Performs the actual condition checking.
	 *
	 * @param monitor a progress monitor to report progress
	 * @return the outcome of the condition check
	 *
	 * @throws CoreException if an error occurred during condition
	 *  checking. The check is interpreted as failed if this happens
	 */
	public RefactoringStatus check(IProgressMonitor monitor) throws CoreException;
}
