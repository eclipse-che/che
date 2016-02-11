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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.ltk.internal.core.refactoring.NotCancelableProgressMonitor;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.history.UnknownRefactoringDescriptor;

/**
 * Operation that, when performed, creates a {@link Change} object for a given
 * refactoring. If created with a refactoring object directly, no precondition
 * checking is performed. If created with a {@link CheckConditionsOperation} the
 * requested precondition checking is performed before creating the change.
 * <p>
 * If the precondition checking returns a fatal error or the status's severity
 * exceeds a certain threshold then no change will be created.
 * </p>
 * <p>
 * If a change has been created the operation calls {@link Change#initializeValidationData(IProgressMonitor)}
 * to initialize the change's validation data.
 * </p>
 * <p>
 * The operation should be executed via the run method offered by
 * <code>IWorkspace</code> to achieve proper delta batching.
 * </p>
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CreateChangeOperation implements IWorkspaceRunnable {

	private Refactoring fRefactoring;

	private CheckConditionsOperation fCheckConditionOperation;
	private int fConditionCheckingFailedSeverity;

	private Change fChange;

	/**
	 * Creates a new operation with the given refactoring. No condition checking
	 * is performed before creating the change object. It is assumed that the
	 * condition checking has already been performed outside of this operation.
	 * The operation might fail if the precondition checking has not been performed
	 * yet.
	 *
	 * @param refactoring the refactoring for which the change is to be created
	 */
	public CreateChangeOperation(Refactoring refactoring) {
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
	}

	/**
	 * Creates a new operation with the given {@link CheckConditionsOperation}. When
	 * performed the operation first checks the conditions as specified by the <code>
	 * CheckConditionsOperation</code>. Depending on the result of the condition
	 * checking a change object is created or not.
	 *
	 * @param operation the condition checking operation
	 * @param checkFailedSeverity the severity from which on the condition checking is
	 *  interpreted as failed. The passed value must be greater than {@link RefactoringStatus#OK}
	 *  and less than or equal {@link RefactoringStatus#FATAL}.
	 *  The standard value from which on a condition check should is to be interpreted as
	 *  failed can be accessed via {@link RefactoringCore#getConditionCheckingFailedSeverity()}.
	 *
	 */
	public CreateChangeOperation(CheckConditionsOperation operation, int checkFailedSeverity) {
		Assert.isNotNull(operation);
		fCheckConditionOperation= operation;
		fRefactoring= operation.getRefactoring();
		Assert.isTrue (checkFailedSeverity > RefactoringStatus.OK && checkFailedSeverity <= RefactoringStatus.FATAL);
		fConditionCheckingFailedSeverity= checkFailedSeverity;
	}

	/**
	 * Returns the condition checking failed severity used by this operation.
	 *
	 * @return the condition checking failed severity
	 *
	 * @see RefactoringStatus
	 */
	public int getConditionCheckingFailedSeverity() {
		return fConditionCheckingFailedSeverity;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		fChange= null;
		try {
			fChange= null;
			RefactoringTickProvider rtp= fRefactoring.getRefactoringTickProvider();
			if (fCheckConditionOperation != null) {
				int conditionTicks= fCheckConditionOperation.getTicks(rtp);
				int allTicks= conditionTicks + rtp.getCreateChangeTicks() + rtp.getInitializeChangeTicks();
				pm.beginTask("", allTicks); //$NON-NLS-1$
				pm.subTask(""); //$NON-NLS-1$
				fCheckConditionOperation.run(new SubProgressMonitor(pm, conditionTicks));
				RefactoringStatus status= fCheckConditionOperation.getStatus();
				if (status != null && status.getSeverity() < fConditionCheckingFailedSeverity) {
					fChange= fRefactoring.createChange(new SubProgressMonitor(pm, rtp.getCreateChangeTicks()));
					fChange.initializeValidationData(new NotCancelableProgressMonitor(
							new SubProgressMonitor(pm, rtp.getInitializeChangeTicks())));
				} else {
					pm.worked(rtp.getCreateChangeTicks() + rtp.getInitializeChangeTicks());
				}
			} else {
				pm.beginTask("", rtp.getCreateChangeTicks() + rtp.getInitializeChangeTicks()); //$NON-NLS-1$
				fChange= fRefactoring.createChange(new SubProgressMonitor(pm, rtp.getCreateChangeTicks()));
				fChange.initializeValidationData(new NotCancelableProgressMonitor(
					new SubProgressMonitor(pm, rtp.getInitializeChangeTicks())));
			}
		} finally {
			pm.done();
		}
	}

	/**
	 * Returns the outcome of the operation or <code>null</code> if an exception
	 * occurred when performing the operation or the operation hasn't been
	 * performed yet.
	 *
	 * @return the created change or <code>null</code>
	 */
	public Change getChange() {
		if (fChange != null) {
			final ChangeDescriptor descriptor= fChange.getDescriptor();
			if (descriptor == null) {
				final CompositeChange composite= new CompositeChange(fChange.getName()) {

					public final ChangeDescriptor getDescriptor() {
						String name= fChange.getName();
						if (name.length() == 0)
							name= RefactoringCoreMessages.CreateChangeOperation_unknown_Refactoring;
						UnknownRefactoringDescriptor unknownDescriptor= new UnknownRefactoringDescriptor(name);
						return new RefactoringChangeDescriptor(unknownDescriptor);
					}
				};
				composite.markAsSynthetic();
				composite.add(fChange);
				fChange= composite;
			}
		}
		return fChange;
	}

	/**
	 * Returns the status of the condition checking. Returns <code>null</code> if
	 * no condition checking has been requested.
	 *
	 * @return the status of the condition checking
	 */
	public RefactoringStatus getConditionCheckingStatus() {
		if (fCheckConditionOperation != null)
			return fCheckConditionOperation.getStatus();
		return null;
	}

	/**
	 * Returns the condition checking style as set to the {@link CheckConditionsOperation}.
	 * If no condition checking operation is provided (e.g. the change is created directly
	 * by calling {@link Refactoring#createChange(IProgressMonitor)} then {@link
	 * CheckConditionsOperation#NONE} is returned.
	 *
	 * @return the condition checking style
	 */
	public int getConditionCheckingStyle() {
		if (fCheckConditionOperation != null)
			return fCheckConditionOperation.getStyle();
		return CheckConditionsOperation.NONE;
	}
}
