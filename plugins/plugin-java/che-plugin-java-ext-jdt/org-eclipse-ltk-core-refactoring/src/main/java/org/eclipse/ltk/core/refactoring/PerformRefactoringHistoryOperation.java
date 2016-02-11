/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin <eclipse.sprigogin@gmail.com> - [refactoring] Provide a way to implement refactorings that depend on resources that have to be explicitly released - https://bugs.eclipse.org/347599
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * Operation that, when run, executes a series of refactoring sequentially.
 * Refactorings are executed using {@link PerformRefactoringOperation}.
 * <p>
 * The operation should be executed via the run method offered by
 * <code>IWorkspace</code> to achieve proper delta batching.
 * </p>
 * <p>
 * Note: this class is not intended to be instantiated or extended outside of
 * the refactoring framework.
 * </p>
 *
 * @see org.eclipse.core.resources.IWorkspace
 * @see PerformRefactoringOperation
 * @see RefactoringHistory
 * @see RefactoringHistoryService
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PerformRefactoringHistoryOperation implements IWorkspaceRunnable {

	/** The status of the execution */
	private RefactoringStatus fExecutionStatus= new RefactoringStatus();

	/** The refactoring history */
	private final RefactoringHistory fRefactoringHistory;

	/**
	 * Creates a new perform refactoring history operation.
	 *
	 * @param history
	 *            the refactoring history
	 */
	public PerformRefactoringHistoryOperation(final RefactoringHistory history) {
		Assert.isNotNull(history);
		fRefactoringHistory= history;
	}

	/**
	 * Hook method which is called when the specified refactoring is going to be
	 * executed.
	 *
	 * @param refactoring
	 *            the refactoring about to be executed
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param monitor
	 *            the progress monitor to use
	 * @return a status describing the outcome of the initialization
	 */
	protected RefactoringStatus aboutToPerformRefactoring(final Refactoring refactoring, final RefactoringDescriptor descriptor, final IProgressMonitor monitor) {
		Assert.isNotNull(refactoring);
		Assert.isNotNull(descriptor);
		return new RefactoringStatus();
	}

	/**
	 * Method which is called to create a refactoring instance from a
	 * refactoring descriptor. The refactoring must be in an initialized state
	 * at the return of the method call. The default implementation delegates
	 * the task to the refactoring descriptor.
	 *
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param status
	 *            a refactoring status to describe the outcome of the
	 *            initialization
	 * @return the refactoring, or <code>null</code> if this refactoring
	 *         descriptor represents the unknown refactoring, or if no
	 *         refactoring contribution is available for this refactoring
	 *         descriptor
	 * @throws CoreException
	 *             if an error occurs while creating the refactoring instance
	 * @deprecated since 3.4. Override {@link #createRefactoring(RefactoringDescriptor, RefactoringStatus, IProgressMonitor)} instead
	 */
	protected Refactoring createRefactoring(final RefactoringDescriptor descriptor, final RefactoringStatus status) throws CoreException {
		Assert.isNotNull(descriptor);
		return descriptor.createRefactoring(status);
	}

	/**
	 * Method which is called to create a refactoring instance from a
	 * refactoring descriptor. The refactoring must be in an initialized state
	 * at the return of the method call. The default implementation delegates
	 * the task to the refactoring descriptor.
	 *
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param status
	 *            a refactoring status to describe the outcome of the
	 *            initialization
	 * @param monitor
	 *            the progress monitor to use
	 * @return the refactoring, or <code>null</code> if this refactoring
	 *         descriptor represents the unknown refactoring, or if no
	 *         refactoring contribution is available for this refactoring
	 *         descriptor
	 * @throws CoreException
	 *             if an error occurs while creating the refactoring instance
	 *
	 * @since 3.4
	 * @deprecated since 3.6. Override {@link #createRefactoringContext(RefactoringDescriptor, RefactoringStatus, IProgressMonitor)} instead
	 */
	protected Refactoring createRefactoring(final RefactoringDescriptor descriptor, final RefactoringStatus status, final IProgressMonitor monitor) throws CoreException {
		try {
			Assert.isNotNull(descriptor);
			return createRefactoring(descriptor, status); // call for backward compatibility
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Method which is called to create a refactoring context from a refactoring descriptor.
	 * The refactoring context must contain a refactoring in an initialized state at the return
	 * of the method call.
	 * <p>
	 * A caller of this method must ensure that {@link RefactoringContext#dispose()} is eventually called.
	 * </p>
	 * 
	 * <p>
	 * The default implementation delegates the task to the refactoring descriptor.
	 * </p>
	 *
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param status
	 *            a refactoring status to describe the outcome of the initialization
	 * @param monitor
	 *            the progress monitor to use
	 * @return the refactoring context, or <code>null</code> if this refactoring descriptor
	 *            represents the unknown refactoring, or if no refactoring contribution is
	 *            available for this refactoring descriptor
	 * @throws CoreException
	 *             if an error occurs while creating the refactoring context
	 * @since 3.6
	 */
	protected RefactoringContext createRefactoringContext(final RefactoringDescriptor descriptor,
			final RefactoringStatus status, final IProgressMonitor monitor) throws CoreException {
		try {
			Assert.isNotNull(descriptor);
			return descriptor.createRefactoringContext(status);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Returns the execution status. Guaranteed not to be <code>null</code>.
	 *
	 * @return the status of the session
	 */
	public final RefactoringStatus getExecutionStatus() {
		return fExecutionStatus;
	}

	/**
	 * Hook method which is called when the specified refactoring has been
	 * performed.
	 *
	 * @param refactoring
	 *            the refactoring which has been performed
	 * @param monitor
	 *            the progress monitor to use
	 */
	protected void refactoringPerformed(final Refactoring refactoring, final IProgressMonitor monitor) {
		Assert.isNotNull(refactoring);
		Assert.isNotNull(monitor);

		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(final IProgressMonitor monitor) throws CoreException {
		fExecutionStatus= new RefactoringStatus();
		final RefactoringDescriptorProxy[] proxies= fRefactoringHistory.getDescriptors();
		monitor.beginTask(RefactoringCoreMessages.PerformRefactoringHistoryOperation_perform_refactorings, 170 * proxies.length);
		final IRefactoringHistoryService service= RefactoringHistoryService.getInstance();
		try {
			service.connect();
			for (int index= 0; index < proxies.length; index++) {
				final RefactoringDescriptor descriptor= proxies[index].requestDescriptor(new SubProgressMonitor(monitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
				if (descriptor != null) {
					RefactoringContext context= null;
					RefactoringStatus status= new RefactoringStatus();
					try {
						try {
							context= createRefactoringContext(descriptor, status, new SubProgressMonitor(monitor, 30, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
						} catch (CoreException exception) {
							status.merge(RefactoringStatus.create(exception.getStatus()));
						}
						if (context != null && !status.hasFatalError()) {
							Refactoring refactoring= context.getRefactoring();
							final PerformRefactoringOperation operation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
							try {
								status.merge(aboutToPerformRefactoring(refactoring, descriptor, new SubProgressMonitor(monitor, 30, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
								if (!status.hasFatalError()) {
									ResourcesPlugin.getWorkspace().run(operation, new SubProgressMonitor(monitor, 90, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
									status.merge(operation.getConditionStatus());
									if (!status.hasFatalError())
										status.merge(operation.getValidationStatus());
								}
							} finally {
								refactoringPerformed(refactoring, new SubProgressMonitor(monitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
							}
						}
					} finally {
						fExecutionStatus.merge(status);
						if (context != null)
							context.dispose();
					}
				}
			}
		} finally {
			service.disconnect();
			monitor.done();
		}
	}
}
