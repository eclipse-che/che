/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * An abstract base class defining the protocol between a refactoring and
 * its associated processor. The API is very similar to the one of a
 * {@link org.eclipse.ltk.core.refactoring.Refactoring}. Implementors of
 * this class should therefore study the interface of the refactoring class
 * as well.
 * <p>
 * A refactoring processor is responsible for:
 * <ul>
 *   <li>refactoring the actual element. For example if a rename Java method
 *       refactoring is executed its associated processor provides the
 *       precondition checking for renaming a method and creates the change
 *       object describing the workspace modifications. This change object
 *       contains elementary changes to rename the Java method and
 *       to update all call sides of this method as well.</li>
 *   <li>loading all participants that want to participate in the refactoring.
 *       For example a Java method rename processor is responsible to load
 *       all participants that want to participate in a Java method rename.</li>
 * </ul>
 * </p>
 * <p>
 * A refactoring processor can not assume that all resources are saved before
 * any methods are called on it. Therefore a processor must be able to deal with
 * unsaved resources.
 * </p>
 * <p>
 * This class should be subclassed by clients wishing to provide special refactoring
 * processors.
 * </p>
 *
 * @since 3.0
 */
public abstract class RefactoringProcessor extends PlatformObject {

	private ProcessorBasedRefactoring fRefactoring;

	/**
	 * Set the owning refactoring.
	 *
	 * @param refactoring the refactoring
	 *
	 * @since 3.1
	 */
	/* package */ void setRefactoring(ProcessorBasedRefactoring refactoring) {
		Assert.isTrue(fRefactoring == null, "The refactoring can only be set once"); //$NON-NLS-1$
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
	}

	/**
	 * Returns the associated refactoring. Returns <code>null</code> if the
	 * processor isn't associated with a refactoring yet.
	 *
	 * @return the associated refactoring
	 *
	 * @since 3.1
	 */
	public ProcessorBasedRefactoring getRefactoring() {
		return fRefactoring;
	}

	/**
	 * Returns an array containing the elements to be refactored. The concrete
	 * type of the elements depend on the concrete refactoring processor. For
	 * example a processor responsible for renaming Java methods returns the
	 * method to be renamed via this call.
	 *
	 * @return an array containing the elements to be refactored
	 */
	public abstract Object[] getElements();

	/**
	 * Returns the unique identifier of the refactoring processor. The
	 * identifier must not be <code>null</code>.
	 *
	 * @return a unique identifier.
	 */
	public abstract String getIdentifier();

	/**
	 * Returns a human readable name. The name will be displayed to users. The
	 * name must not be <code>null</code>.
	 *
	 * @return a human readable name
	 */
	public abstract String getProcessorName();

	/**
	 * Checks whether the processor is applicable to the elements to be
	 * refactored or not. If <code> false</code> is returned the processor is
	 * interpreted to be unusable.
	 *
	 * @return <code>true</code> if the processor is applicable to the
	 *         elements; otherwise <code>false</code> is returned.
	 * @throws CoreException is the test fails. The processor is treated as
	 *  unusable if this method throws an exception
	 */
	public abstract boolean isApplicable() throws CoreException;

	/**
	 * Checks some initial conditions based on the element to be refactored.
	 * <p>
	 * The refactoring using this processor is considered as not being
	 * executable if the returned status has the severity of
	 * <code>RefactoringStatus#FATAL</code>.
	 * </p>
	 * <p>
	 * This method can be called more than once.
	 * </p>
	 *
	 * @param pm a progress monitor to report progress. Although availability
	 *        checks are supposed to execute fast, there can be certain
	 *        situations where progress reporting is necessary. For example
	 *        rebuilding a corrupted index may report progress.
	 *
	 * @return a refactoring status. If the status is <code>RefactoringStatus#FATAL</code>
	 *  the refactoring is considered as not being executable.
	 *
	 * @throws CoreException if an exception occurred during initial condition
	 *         checking. If this happens, the initial condition checking is
	 *         interpreted as failed.
	 *
	 * @throws OperationCanceledException if the condition checking got canceled
	 *
	 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkInitialConditions(IProgressMonitor)
	 * @see RefactoringStatus#FATAL
	 */
	public abstract RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException;

	/**
	 * Checks the final conditions based on the element to be refactored.
	 * <p>
	 * The refactoring using this processor is considered as not being
	 * executable if the returned status has the severity of
	 * <code>RefactoringStatus#FATAL</code>.
	 * </p>
	 * <p>
	 * This method can be called more than once.
	 * </p>
	 *
	 * @param pm a progress monitor to report progress
	 * @param context a condition checking context to collect shared condition checks
	 *
	 * @return a refactoring status. If the status is <code>RefactoringStatus#FATAL</code>
	 *  the refactoring is considered as not being executable.
	 *
	 * @throws CoreException if an exception occurred during final condition
	 *  checking. If this happens, the final condition checking is interpreted as failed.
	 *
	 * @throws OperationCanceledException if the condition checking got canceled
	 *
	 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkFinalConditions(IProgressMonitor)
	 * @see RefactoringStatus#FATAL
	 */
	public abstract RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
		throws CoreException, OperationCanceledException;

	/**
	 * Creates a {@link Change} object describing the workspace modifications
	 * the processor contributes to the overall refactoring.
	 *
	 * @param pm a progress monitor to report progress
	 *
	 * @return the change representing the workspace modifications of the
	 *  processor
	 *
	 * @throws CoreException if an error occurred while creating the change
	 *
	 * @throws OperationCanceledException if the condition checking got canceled
	 *
	 * @see org.eclipse.ltk.core.refactoring.Refactoring#createChange(IProgressMonitor)
	 */
	public abstract Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

	/**
	 * Additional hook allowing processors to add changes to the set of workspace
	 * modifications after all participant changes have been created.
	 *
	 * @param participantChanges an array containing the changes created by the
	 *  participants
	 * @param pm a progress monitor to report progress
	 *
	 * @return change representing additional workspace modifications, or <code>null</code>
	 *
	 * @throws CoreException if an error occurred while creating the post change
	 *
	 * @throws OperationCanceledException if the condition checking got canceled
	 *
	 * @see #createChange(IProgressMonitor)
	 */
	public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return null;
	}

	/**
	 * Returns the array of participants. It is up to the implementor of a
	 * concrete processor to define which participants are loaded. In general,
	 * three different kinds of participants can be distinguished:
	 * <ul>
	 *   <li>participants listening to the processed refactoring itself. For
	 *       example if a Java field gets renamed all participants listening
	 *       to Java field renames should be added via this hook.</li>
	 *   <li>participants listening to changes of derived elements. For example
	 *       if a Java field gets renamed corresponding setter and getters methods
	 *       are renamed as well. The setter and getter methods are considered as
	 *       derived elements and the corresponding participants should be added via
	 *       this hook.</li>
	 *   <li>participants listening to changes of a domain model different than
	 *       the one that gets manipulated, but changed as a "side effect" of the
	 *       refactoring. For example, renaming a package moves all its files to a
	 *       different folder. If the package contains a HTML file then the rename
	 *       package processor is supposed to load all move HTML file participants
	 *       via this hook.</li>
	 * </ul>
	 * <p>
	 * Implementors are responsible to initialize the created participants with
	 * the right arguments. The method is called after
	 * {@link #checkFinalConditions(IProgressMonitor, CheckConditionsContext)}has
	 * been called on the processor itself.
	 * </p>
	 * @param status a refactoring status to report status if problems occur while
	 *  loading the participants
	 * @param sharedParticipants a list of sharable participants. Implementors of
	 *  this method can simply pass this instance to the corresponding participant
	 *  loading methods defined in {@link ParticipantManager}.
	 *
	 * @return an array of participants or <code>null</code> or an empty array
	 *  if no participants are loaded
	 *
	 * @throws CoreException if creating or loading of the participants failed
	 *
	 * @see ISharableParticipant
	 */
	public abstract RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants) throws CoreException;
}
