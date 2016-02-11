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
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Implementors of refactorings uses instances of <code>RefactoringTickProvider</code>
 * to specify the tick distribution during progress reporting when executing the
 * check conditions, create change and change initialization steps.
 *
 * @since 3.2
 */
public class RefactoringTickProvider {

	/**
	 * The default refactoring tick provider
	 */
	public final static RefactoringTickProvider DEFAULT= new RefactoringTickProvider(4, 40, 22, 11);

	private int[] fValues;

	private static final int CHECK_INITIAL_CONDITIONS= 0;
	private static final int CHECK_FINAL_CONDITIONS= 1;
	private static final int CREATE_CHANGE= 2;
	private static final int INITIALIZE_CHANGE= 3;

	/**
	 * Creates a new refactoring tick provider with the given values
	 *
	 * @param checkInitialConditionsTicks ticks used in the initial condition
	 *  check step
	 * @param checkFinalConditionsTicks ticks used in the final condition
	 *  check step
	 * @param createChangeTicks ticks used in the create change step
	 * @param initializeChangeTicks ticks used in the change validation steps
	 */
	public RefactoringTickProvider(
			int checkInitialConditionsTicks,
			int checkFinalConditionsTicks,
			int createChangeTicks,
			int initializeChangeTicks) {
		Assert.isTrue(checkInitialConditionsTicks >= 0 && checkFinalConditionsTicks >= 0 &&
			createChangeTicks >= 0 && initializeChangeTicks >= 0);

		fValues= new int[4];
		fValues[CHECK_INITIAL_CONDITIONS]= checkInitialConditionsTicks;
		fValues[CHECK_FINAL_CONDITIONS]= checkFinalConditionsTicks;
		fValues[CREATE_CHANGE]= createChangeTicks;
		fValues[INITIALIZE_CHANGE]= initializeChangeTicks;
	}

	/**
	 * Sum of <code>getCheckConditionsTicks</code>, <code>getCreateChangeTicks</code>
	 * and <code>getInitializeChangeTicks</code>.
	 *
	 * @return the number of ticks, >= 0
	 */
	public int getAllTicks() {
		return getCheckAllConditionsTicks() + fValues[CREATE_CHANGE] + fValues[INITIALIZE_CHANGE];
	}

	/**
	 * Sum of <code>getCheckInitialConditionsTicks()</code> and
	 * <code>getCheckFinalConditionsTicks</code>
	 *
	 * @return the number of ticks, >= 0
	 */
	public int getCheckAllConditionsTicks() {
		return fValues[CHECK_INITIAL_CONDITIONS] + fValues[CHECK_FINAL_CONDITIONS];
	}

	/**
	 * Number of ticks reserved in the parent progress monitor of the progress monitor
	 * passed to <code>Refactoring#checkInitialConditions()</code>.
	 *
	 * @return the number of ticks, >= 0
	 */
	public int getCheckInitialConditionsTicks() {
		return fValues[CHECK_INITIAL_CONDITIONS];
	}

	/**
	 * Number of ticks reserved in the parent progress monitor of the progress monitor
	 * passed to <code>Refactoring#checkFinalConditions()</code>.
	 *
	 * @return the number of ticks, >= 0
	 */
	public int getCheckFinalConditionsTicks() {
		return fValues[CHECK_FINAL_CONDITIONS];
	}

	/**
	 * Number of ticks reserved in the parent progress monitor of the progress monitor
	 * passed to <code>Refactoring#createChange()</code>.
	 *
	 * @return the number of ticks, >= 0
	 */
	public int getCreateChangeTicks() {
		return fValues[CREATE_CHANGE];
	}

	/**
	 * Number of ticks reserved in the parent progress monitor for the progress monitor
	 * passed to <code>{@link Change#initializeValidationData(IProgressMonitor)}</code>
	 * which is executed on the object returned by <code>Refactoring#createChange()</code>.
	 *
	 * @return the number of ticks, >= 0
	 */
	public int getInitializeChangeTicks() {
		return fValues[INITIALIZE_CHANGE];
	}
}