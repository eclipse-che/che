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

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A query interface to decide if a undo change whose validation check
 * returned a status unequal {@link org.eclipse.ltk.core.refactoring.RefactoringStatus#OK}
 * should be executed or not.
 * <p>
 * Clients should be aware that the methods defined by this interface can be
 * called from a non UI thread.
 * </p>
 * <p>
 * The interface may be implemented by clients.
 * </p>
 *
 * @since 3.0
 */
public interface IValidationCheckResultQuery {

	/**
	 * Returns whether the undo proceeds or not. This method is called if the
	 * validation check returned a status greater than <code>OK</code> and less
	 * than <code>FATAL</code>.
	 *
	 * @param status the refactoring status returned from {@link Change#isValid(IProgressMonitor)}
	 *
	 * @return <code>true</code> if the undo should proceed; <code>false</code>
	 *  otherwise
	 */
	public boolean proceed(RefactoringStatus status);

	/**
	 * Called when the validation check returned a fatal error. In this case the
	 * undo can't proceed. The hook can be used to present a corresponding dialog
	 * to the user.
	 *
	 * @param status the refactoring status returned from {@link Change#isValid(IProgressMonitor)}
	 */
	public void stopped(RefactoringStatus status);
}
