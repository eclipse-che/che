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

import org.eclipse.core.resources.IWorkspaceRunnable;

/**
 * Operation that, when run, checks the preconditions of the {@link Refactoring}
 * passed on creation.
 * <p>
 * The operation should be executed via the run method offered by
 * <code>IWorkspace</code> to achieve proper delta batching.
 * </p>
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkInitialConditions(IProgressMonitor)
 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkFinalConditions(IProgressMonitor)
 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkAllConditions(IProgressMonitor)
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CheckConditionsOperation implements IWorkspaceRunnable {

	private Refactoring fRefactoring;
	private int fStyle;
	private RefactoringStatus fStatus;

	/** Flag indicating that no conditions will be checked */
	public final static int NONE=				0;
	/** Flag indicating that only initial conditions will be checked*/
	public final static int INITIAL_CONDITONS=	1 << 1;
	/** Flag indicating that only final conditions will be checked */
	public final static int FINAL_CONDITIONS=	1 << 2;
	/** Flag indicating that all conditions will be checked */
	public final static int ALL_CONDITIONS=		INITIAL_CONDITONS | FINAL_CONDITIONS;

	private final static int LAST=          	1 << 3;

	/**
	 * Creates a new <code>CheckConditionsOperation</code>.
	 *
	 * @param refactoring the refactoring for which the preconditions are to
	 *  be checked.
	 * @param style style to define which conditions to check. Must be one of
	 *  <code>INITIAL_CONDITONS</code>, <code>FINAL_CONDITIONS</code> or
	 *  <code>ALL_CONDITIONS</code>
	 */
	public CheckConditionsOperation(Refactoring refactoring, int style) {
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
		fStyle= style;
		Assert.isTrue(checkStyle(fStyle));
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		try {
			fStatus= null;
			if ((fStyle & ALL_CONDITIONS) == ALL_CONDITIONS)
				fStatus= fRefactoring.checkAllConditions(pm);
			else if ((fStyle & INITIAL_CONDITONS) == INITIAL_CONDITONS)
				fStatus= fRefactoring.checkInitialConditions(pm);
			else if ((fStyle & FINAL_CONDITIONS) == FINAL_CONDITIONS)
				fStatus= fRefactoring.checkFinalConditions(pm);
		} finally {
			pm.done();
		}
	}

	/**
	 * Returns the outcome of the operation or <code>null</code> if an exception
	 * has occurred while performing the operation or if the operation hasn't
	 * been performed yet.
	 *
	 * @return the {@link RefactoringStatus} of the condition checking
	 */
	public RefactoringStatus getStatus() {
		return fStatus;
	}

	/**
	 * Returns the operation's refactoring
	 *
	 * @return the operation's refactoring
	 */
	public Refactoring getRefactoring() {
		return fRefactoring;
	}

	/**
	 * Returns the condition checking style.
	 *
	 * @return the condition checking style
	 */
	public int getStyle() {
		return fStyle;
	}

	private boolean checkStyle(int style) {
		return style > NONE && style < LAST;
	}

	/* package */ int getTicks(RefactoringTickProvider provider) {
		if ((fStyle & ALL_CONDITIONS) == ALL_CONDITIONS)
			return provider.getCheckAllConditionsTicks();
		else if ((fStyle & INITIAL_CONDITONS) == INITIAL_CONDITONS)
			return provider.getCheckInitialConditionsTicks();
		else if ((fStyle & FINAL_CONDITIONS) == FINAL_CONDITIONS)
			return provider.getCheckFinalConditionsTicks();
		return 0;
	}
}
