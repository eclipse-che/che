/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Abstract super class for all refactorings. Refactorings are used to perform
 * behavior-preserving workspace transformations. A refactoring offers two
 * different kind of methods:
 * <ol>
 *   <li>methods to check conditions to determine if the refactoring can be carried out
 *       in general and if transformation will be behavior-preserving.
 *       </li>
 *   <li>a method to create a {@link org.eclipse.ltk.core.refactoring.Change} object
 *       that represents the actual work space modifications.
 *       </li>
 * </ol>
 * The life cycle of a refactoring is as follows:
 * <ol>
 *   <li>the refactoring gets created</li>
 *   <li>the refactoring is initialized with the elements to be refactored. It is
 *       up to a concrete refactoring implementation to provide corresponding API.</li>
 *   <li>{@link #checkInitialConditions(IProgressMonitor)} is called. The method
 *       can be called more than once.</li>
 *   <li>additional arguments are provided to perform the refactoring (for example
 *       the new name of a element in the case of a rename refactoring). It is up
 *       to a concrete implementation to provide corresponding API.</li>
 *   <li>{@link #checkFinalConditions(IProgressMonitor)} is called. The method
 *       can be called more than once. The method must not be called if
 *       {@link #checkInitialConditions(IProgressMonitor)} returns a refactoring
 *       status of severity {@link RefactoringStatus#FATAL}.</li>
 *   <li>{@link #createChange(IProgressMonitor)} is called. The method must only be
 *       called once after each call to {@link #checkFinalConditions(IProgressMonitor)}
 *       and should not be called if one of the condition checking methods
 *       returns a refactoring status of severity {@link RefactoringStatus#FATAL}.
 *   </li>
 *   <li>steps 4 to 6 can be executed repeatedly (for example when the user goes
 *       back from the preview page).
 *   </li>
 * </ol>
 *
 * <p>
 * A refactoring can not assume that all resources are saved before any methods
 * are called on it. Therefore a refactoring must be able to deal with unsaved
 * resources.
 * </p>
 * <p>
 * The class should be subclassed by clients wishing to implement new refactorings.
 * </p>
 * 
 * @see RefactoringContext
 *
 * @since 3.0
 */
public abstract class Refactoring extends PlatformObject {

	private Object fValidationContext;

	/**
	 * Sets the validation context used when calling
	 * {@link org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)}.
	 *
	 * @param context the <code>org.eclipse.swt.widgets.Shell</code> that is
	 * to be used to parent any dialogs with the user, or <code>null</code> if
	 * there is no UI context (declared as an <code>Object</code> to avoid any
	 * direct references on the SWT component)
	 */
	public final void setValidationContext(Object context) {
		fValidationContext= context;
	}

	/**
	 * Returns the validation context
	 *
	 * @return the validation context or <code>null</code> if no validation
	 *  context has been set.
	 */
	public final Object getValidationContext() {
		return fValidationContext;
	}

	/**
	 * Returns the refactoring's name.
	 *
	 * @return the refactoring's human readable name. Must not be
	 *  <code>null</code>
	 */
	public abstract String getName();

	//---- Conditions ------------------------------------------------------------

	/**
	 * Returns the tick provider used for progress reporting for this
	 * refactoring.
	 *
	 * @return the refactoring tick provider used for progress reporting
	 *
	 * @since 3.2
	 */
	public final RefactoringTickProvider getRefactoringTickProvider() {
		RefactoringTickProvider result= doGetRefactoringTickProvider();
		if (result == null) {
			result= RefactoringTickProvider.DEFAULT;
		}
		return result;
	}

	/**
	 * Hook method to provide the tick provider used for progress reporting.
	 * <p>
	 * Subclasses may override this method
	 * </p>
	 * @return the refactoring tick provider used for progress reporting
	 *
	 * @since 3.2
	 */
	protected RefactoringTickProvider doGetRefactoringTickProvider() {
		return RefactoringTickProvider.DEFAULT;
	}

	/**
	 * Checks all conditions. This implementation calls <code>checkInitialConditions</code>
	 * and <code>checkFinalConditions</code>.
	 * <p>
	 * Subclasses may extend this method to provide additional condition checks.
	 * </p>
	 *
	 * @param pm a progress monitor to report progress
	 *
	 * @return a refactoring status. If the status is <code>RefactoringStatus#FATAL</code>
	 *  the refactoring has to be considered as not being executable.
	 *
	 * @throws CoreException if an exception occurred during condition checking.
	 *  If this happens then the condition checking has to be interpreted as failed
	 *
	 * @throws OperationCanceledException if the condition checking got canceled
	 *
	 * @see #checkInitialConditions(IProgressMonitor)
	 * @see #checkFinalConditions(IProgressMonitor)
	 */
	public RefactoringStatus checkAllConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		RefactoringTickProvider refactoringTickProvider= getRefactoringTickProvider();
		pm.beginTask("", refactoringTickProvider.getCheckAllConditionsTicks()); //$NON-NLS-1$
		RefactoringStatus result= new RefactoringStatus();
		result.merge(checkInitialConditions(new SubProgressMonitor(pm, refactoringTickProvider.getCheckInitialConditionsTicks())));
		if (!result.hasFatalError()) {
			if (pm.isCanceled())
				throw new OperationCanceledException();
			result.merge(checkFinalConditions(new SubProgressMonitor(pm, refactoringTickProvider.getCheckFinalConditionsTicks())));
		}
		pm.done();
		return result;
	}

	/**
	 * Checks some initial conditions based on the element to be refactored. The
	 * method is typically called by the UI to perform an initial checks after an
	 * action has been executed.
	 * <p>
	 * The refactoring has to be considered as not being executable if the returned status
	 * has the severity of <code>RefactoringStatus#FATAL</code>.
	 * </p>
	 * <p>
	 * This method can be called more than once.
	 * </p>
	 *
	 * @param pm a progress monitor to report progress. Although initial checks
	 *  are supposed to execute fast, there can be certain situations where progress
	 *  reporting is necessary. For example rebuilding a corrupted index may report
	 *  progress.
	 *
	 * @return a refactoring status. If the status is <code>RefactoringStatus#FATAL</code>
	 *  the refactoring has to be considered as not being executable.
	 *
	 * @throws CoreException if an exception occurred during initial condition checking.
	 *  If this happens then the initial condition checking has to be interpreted as failed
	 *
	 * @throws OperationCanceledException if the condition checking got canceled
	 *
	 * @see #checkFinalConditions(IProgressMonitor)
	 * @see RefactoringStatus#FATAL
	 */
	public abstract RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException;

	/**
	 * After <code>checkInitialConditions</code> has been performed and the user has
	 * provided all input necessary to perform the refactoring this method is called
	 * to check the remaining preconditions.
	 * <p>
	 * The refactoring has to be considered as not being executable if the returned status
	 * has the severity of <code>RefactoringStatus#FATAL</code>.
	 * </p>
	 * <p>
	 * This method can be called more than once.
	 * </p>
	 *
	 * @param pm a progress monitor to report progress
	 *
	 * @return a refactoring status. If the status is <code>RefactoringStatus#FATAL</code>
	 *  the refactoring is considered as not being executable.
	 *
	 * @throws CoreException if an exception occurred during final condition checking
	 *  If this happens then the final condition checking is interpreted as failed
	 *
	 * @throws OperationCanceledException if the condition checking got canceled
	 *
	 * @see #checkInitialConditions(IProgressMonitor)
	 * @see RefactoringStatus#FATAL
	 */
	public abstract RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException;

	//---- change creation ------------------------------------------------------

	/**
	 * Creates a {@link Change} object that performs the actual workspace
	 * transformation.
	 *
	 * @param pm a progress monitor to report progress
	 *
	 * @return the change representing the workspace modifications of the
	 *  refactoring
	 *
	 * @throws CoreException if an error occurred while creating the change
	 *
	 * @throws OperationCanceledException if the condition checking got canceled
	 */
	public abstract Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

	/**
	 * {@inheritDoc}
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this))
			return this;
//		return super.getAdapter(adapter);
		return null;
	}

	/* (non-Javadoc)
	 * for debugging only
	 */
	public String toString() {
		return getName();
	}
}
