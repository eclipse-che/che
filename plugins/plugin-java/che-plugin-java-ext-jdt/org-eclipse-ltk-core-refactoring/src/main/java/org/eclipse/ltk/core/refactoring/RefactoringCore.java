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

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringContributionManager;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * Central access point to access resources managed by the refactoring
 * core plug-in.
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefactoringCore {

	/**
	 * The id of the Refactoring plug-in (value <code>"org.eclipse.ltk.core.refactoring"</code>).
	 *
	 * @since 3.2
	 */
	public static final String ID_PLUGIN= "org.eclipse.ltk.core.refactoring"; //$NON-NLS-1$

	private static IValidationCheckResultQueryFactory fQueryFactory= new DefaultQueryFactory();

	private static class NullQuery implements IValidationCheckResultQuery {
		public boolean proceed(RefactoringStatus status) {
			return true;
		}
		public void stopped(RefactoringStatus status) {
			// do nothing
		}
	}

	private static class DefaultQueryFactory implements IValidationCheckResultQueryFactory {
		public IValidationCheckResultQuery create(IAdaptable context) {
			return new NullQuery();
		}
	}

	private RefactoringCore() {
		// no instance
	}

	/**
	 * Returns the singleton undo manager for the refactoring undo stack.
	 *
	 * @return the refactoring undo manager.
	 */
	public static IUndoManager getUndoManager() {
		return RefactoringCorePlugin.getUndoManager();
	}

	/**
	 * Returns the singleton refactoring history service.
	 *
	 * @return the refactoring history service
	 *
	 * @since 3.2
	 */
	public static IRefactoringHistoryService getHistoryService() {
		return RefactoringHistoryService.getInstance();
	}

	/**
	 * Returns the refactoring contribution with the specified unique id.
	 *
	 * @param id
	 *            the unique id of the contribution
	 * @return the refactoring contribution, or <code>null</code> if in
	 *         contribution is registered with for this id
	 *
	 * @since 3.2
	 */
	public static RefactoringContribution getRefactoringContribution(String id) {
		return RefactoringContributionManager.getInstance().getRefactoringContribution(id);
	}

	/**
	 * When condition checking is performed for a refactoring then the
	 * condition check is interpreted as failed if the refactoring status
	 * severity return from the condition checking operation is equal
	 * or greater than the value returned by this method.
	 *
	 * @return the condition checking failed severity
	 */
	public static int getConditionCheckingFailedSeverity() {
		return RefactoringStatus.WARNING;
	}

	/**
	 * Returns the query factory.
	 *
	 * @return the query factory
	 *
	 * @since 3.1
	 */
	public static IValidationCheckResultQueryFactory getQueryFactory() {
		return fQueryFactory;
	}

	/**
	 * An internal method to set the query factory.
	 * <p>
	 * This method is NOT official API. It is a special method for the refactoring UI
	 * plug-in to set a dialog based query factory.
	 * </p>
	 * @param factory the factory to set or <code>null</code>
	 *
	 * @since 3.1
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void internalSetQueryFactory(IValidationCheckResultQueryFactory factory) {
		if (factory == null) {
			fQueryFactory= new DefaultQueryFactory();
		} else {
			fQueryFactory= factory;
		}
	}
}
