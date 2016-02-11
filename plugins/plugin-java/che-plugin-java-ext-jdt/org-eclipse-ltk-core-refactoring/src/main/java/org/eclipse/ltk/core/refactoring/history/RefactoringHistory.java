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

import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

/**
 * Object which represents a sequence of executed refactorings with optional time
 * information.
 * <p>
 * Refactoring histories are exposed by the refactoring history service as
 * result of queries and contain only lightweight proxy objects. The refactoring
 * history service may hand out any number of refactoring histories and
 * associated refactoring descriptor proxies for any given query.
 * </p>
 * <p>
 * Note: this class may be extended to provide different implementations
 * </p>
 *
 * @see IRefactoringHistoryService
 * @see RefactoringDescriptorProxy
 *
 * @since 3.2
 */
public abstract class RefactoringHistory extends PlatformObject {

	/**
	 * Returns the refactoring descriptors of this history, in descending order
	 * of their time stamps.
	 *
	 * @return the refactoring descriptors, or an empty array
	 */
	public abstract RefactoringDescriptorProxy[] getDescriptors();

	/**
	 * Is the refactoring history empty?
	 *
	 * @return <code>true</code> if the history is empty, <code>false</code>
	 *         otherwise
	 */
	public abstract boolean isEmpty();

	/**
	 * Returns this refactoring history with all entries from the other history
	 * removed.
	 * <p>
	 * The current refactoring history remains unchanged.
	 * </p>
	 *
	 * @param history
	 *            the refactoring history
	 * @return the resulting refactoring history
	 */
	public abstract RefactoringHistory removeAll(RefactoringHistory history);
}
