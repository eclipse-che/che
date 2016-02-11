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
package org.eclipse.ltk.core.refactoring.history;

import org.eclipse.ltk.core.refactoring.RefactoringCore;

/**
 * Interface for refactoring execution listeners. Clients may register a
 * refactoring execution listener with the {@link IRefactoringHistoryService}
 * obtained by calling {@link RefactoringCore#getHistoryService()} in order to
 * get informed about refactoring execution events.
 * <p>
 * Note: this interface is intended to be implemented by clients.
 * </p>
 *
 * @see IRefactoringHistoryService
 * @see RefactoringExecutionEvent
 *
 * @since 3.2
 */
public interface IRefactoringExecutionListener {

	/**
	 * Gets called if a refactoring execution event happened.
	 * <p>
	 * Implementors of this method should not rely on a fixed set of event
	 * types.
	 * </p>
	 * <p>
	 * The event object is valid only for the duration of this method.
	 * </p>
	 *
	 * @param event
	 *            the refactoring execution event
	 */
	public void executionNotification(RefactoringExecutionEvent event);
}
