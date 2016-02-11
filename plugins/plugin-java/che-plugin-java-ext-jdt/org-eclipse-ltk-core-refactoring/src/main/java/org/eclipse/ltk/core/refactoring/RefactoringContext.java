/*******************************************************************************
 * Copyright (c) 2011, 2013 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sergey Prigogin <eclipse.sprigogin@gmail.com> - [refactoring] Provide a way to implement refactorings that depend on resources that have to be explicitly released - https://bugs.eclipse.org/347599
 *     IBM Corporation - bug fixes
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

/**
 * <p>
 * Refactoring context is a disposable object that can be used by a refactoring to hold resources
 * that have to be explicitly released. The refactoring context is guaranteed to receive
 * a {@link #dispose()} call after the associated refactoring has finished or produced an error.
 * At this point, the refactoring context must release all resources and detach all listeners.
 * A refactoring context can only be disposed once; it cannot be reused.
 * </p>
 * <p>
 * This class is intended to be subclassed by clients wishing to implement new refactorings that
 * depend on resources that have to be explicitly released.
 * </p>
 * @since 3.6
 */
public class RefactoringContext {
	private Refactoring fRefactoring;

	/**
	 * Creates a context for the given refactoring.
	 * 
	 * @param refactoring The refactoring associated with the context. Cannot be <code>null</code>.
	 * @throws NullPointerException if refactoring is <code>null</code>.
	 */
	public RefactoringContext(Refactoring refactoring) {
		if (refactoring == null)
			throw new NullPointerException();
		fRefactoring= refactoring;
	}

	/**
	 * Returns the refactoring associated with the context.
	 * <p>
	 * The returned refactoring must be in an initialized state, i.e. ready to
	 * be executed via {@link PerformRefactoringOperation}.
	 * </p>
	 * @return The refactoring associated with the context.
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 */
	public Refactoring getRefactoring() {
		return fRefactoring;
	}

	/**
	 * Disposes of the context. This method will be called exactly once during the life cycle
	 * of the context after the associated refactoring has finished or produced an error.
	 * <p>
	 * Subclasses may extend this method (must call super).
	 * </p>
	 */
	public void dispose() {
		if (fRefactoring == null)
			throw new IllegalStateException("dispose() called more than once."); //$NON-NLS-1$
		fRefactoring= null;
	}
}
