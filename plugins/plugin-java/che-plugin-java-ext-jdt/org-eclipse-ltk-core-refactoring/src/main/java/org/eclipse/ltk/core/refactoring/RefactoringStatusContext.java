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
 * A <code>RefactoringStatusContext</code> can be used to annotate a
 * {@link RefactoringStatusEntry} with additional information
 * typically presented in the user interface.
 * <p>
 * To present a context in the user interface a corresponding context
 * viewer can be registered via the extension point <code>
 * org.eclipse.ltk.ui.refactoring.statusContextViewers</code>.
 * </p>
 * <p>
 * This class may be subclassed by clients.
 * </p>
 *
 * @since 3.0
 */
public abstract class RefactoringStatusContext {
	/**
	 * Returns the element that corresponds directly to this context,
	 * or <code>null</code> if there is no corresponding element.
	 * <p>
	 * For example, the corresponding element of a context for a problem
	 * detected in an <code>IResource</code> would be the resource itself.
	 * <p>
	 *
	 * @return the corresponding element
	 */
	public abstract Object getCorrespondingElement();

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		Object element= getCorrespondingElement();
		return element == null ? null : element.toString();
	}
}
