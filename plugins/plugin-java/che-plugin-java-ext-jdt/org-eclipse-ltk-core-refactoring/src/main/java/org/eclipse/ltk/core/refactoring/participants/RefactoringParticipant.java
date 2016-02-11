/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton) <francisu@ieee.org> -
 *          Fix for Bug 63149 [ltk] allow changes to be executed after the 'main' change during an undo [refactoring]
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.internal.core.refactoring.ParticipantDescriptor;

/**
 * A refactoring participant can participate in the condition checking and
 * change creation of a {@link RefactoringProcessor}.
 * <p>
 * If the severity of the condition checking result is {@link RefactoringStatus#FATAL}
 * then the whole refactoring will not be carried out.
 * </p>
 * <p>
 * The changes created by a participant <em>MUST</em> not conflict with any changes
 * provided by other participants or the refactoring itself. To ensure this, a participant
 * is only allowed to manipulate resources belonging to its domain. As of 3.1 this got
 * relaxed for textual resources. A participant can now change a textual resource already
 * manipulated by the processor as long as both are manipulating different regions in the
 * file (see {@link #createChange(IProgressMonitor)} and {@link #getTextChange(Object)}).
 * For example a rename type participant updating launch configuration is only allowed to
 * update launch configurations or shared textual resources. If a change conflicts with
 * another change during execution then the participant who created the change will be
 * disabled for the rest of the eclipse session.
 * </p>
 * <p>
 * A refactoring participant can not assume that all resources are saved before any
 * methods are called on it. Therefore a participant must be able to deal with unsaved
 * resources.
 * </p>
 * <p>
 * A refactoring participant can implement {@link ISharableParticipant} in order to be
 * shared for multiple elements to be refactored by the same processor.
 * </p>
 * <p>
 * This class should be subclassed by clients wishing to provide special refactoring
 * participants extension points.
 * </p>
 * <p>
 * Since 3.4, a refactoring participant can also override {@link #createPreChange(IProgressMonitor)}
 * to add changes that will be executed <em>before</em> the main refactoring changes
 * are executed.
 * </p>
 *
 * @see RefactoringProcessor
 * @see ISharableParticipant
 *
 * @since 3.0
 */
public abstract class RefactoringParticipant extends PlatformObject {

	private RefactoringProcessor fProcessor;

	private ParticipantDescriptor fDescriptor;

	/**
	 * Returns the processor that is associated with this participant.
	 *
	 * @return the processor that is associated with this participant
	 */
	public RefactoringProcessor getProcessor() {
		return fProcessor;
	}

	/**
	 * Initializes the participant. This method is called by the framework when a
	 * participant gets instantiated.
	 * <p>
	 * This method isn't intended to be extended or reimplemented by clients.
	 * </p>
	 * @param processor the processor this participant is associated with
	 * @param element the element to be refactored
	 * @param arguments the refactoring arguments
	 *
	 * @return <code>true</code> if the participant could be initialized;
	 *  otherwise <code>false</code> is returned. If <code>false</code> is
	 *  returned then the participant will not be added to the refactoring.
	 *
	 * @see #initialize(Object)
	 */
	public boolean initialize(RefactoringProcessor processor, Object element, RefactoringArguments arguments) {
		Assert.isNotNull(processor);
		Assert.isNotNull(arguments);
		fProcessor= processor;
		initialize(arguments);
		return initialize(element);
	}

	/**
	 * Initializes the participant with the element to be refactored.
	 * If this method returns <code>false</code> then the framework
	 * will consider the participant as not being initialized and the
	 * participant will be dropped by the framework.
	 *
	 * @param element the element to be refactored
	 *
	 * @return <code>true</code> if the participant could be initialized;
	 *  otherwise <code>false</code> is returned.
	 */
	protected abstract boolean initialize(Object element);

	/**
	 * Initializes the participant with the refactoring arguments
	 *
	 * @param arguments the refactoring arguments
	 */
	protected abstract void initialize(RefactoringArguments arguments);

	/**
	 * Returns a human readable name of this participant.
	 *
	 * @return a human readable name
	 */
	public abstract String getName();

	/**
	 * Checks the conditions of the refactoring participant.
	 * <p>
	 * The refactoring is considered as not being executable if the returned status
	 * has the severity of <code>RefactoringStatus#FATAL</code>. Note that this blocks
	 * the whole refactoring operation!
	 * </p>
	 * <p>
	 * Clients should use the passed {@link CheckConditionsContext} to validate the changes
	 * they generate. If the generated changes include workspace resource modifications,
	 * clients should call ...
	 *
	 * <pre> (ResourceChangeChecker) context.getChecker(ResourceChangeChecker.class);
	 * IResourceChangeDescriptionFactory deltaFactory= checker.getDeltaFactory();</pre>
	 *
	 * ... and use the delta factory to describe all resource modifications in advance.
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
	 * @throws OperationCanceledException if the condition checking got canceled
	 *
	 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkInitialConditions(IProgressMonitor)
	 * @see RefactoringStatus
	 */
	public abstract RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException;

	/**
	 * Creates a {@link Change} object that contains the workspace modifications
	 * of this participant to be executed <em>before</em> the
	 * changes from the refactoring are executed. Note that this implies that the
	 * undo change of the returned Change object will be executed <em>after</em>
	 * the undo changes from the refactoring have been executed.
	 * <p>
	 * The changes provided
	 * by a participant <em>must</em> not conflict with any change provided by other
	 * participants or by the refactoring itself.
	 * <p>
	 * If the change conflicts with any change provided by other participants or
	 * by the refactoring itself, then change execution will fail and the
	 * participant will be disabled for the rest of the eclipse session.
	 * </p>
	 * <p>
	 * If an exception occurs while creating the change, the refactoring can not
	 * be carried out, and the participant will be disabled for the rest of the
	 * eclipse session.
	 * </p>
	 * <p>
	 * A participant can manipulate text resource already manipulated by
	 * the processor as long as the textual manipulations don't conflict (e.g.
	 * the participant manipulates a different region of the text resource).
	 * The method must not return those changes in its change tree since the change
	 * is already part of another change tree. If the participant only manipulates
	 * shared changes then it can return <code>null</code> to indicate that it didn't
	 * create own changes. A shared text change can be accessed via the method
	 * {@link #getTextChange(Object)}.
	 * </p>
	 * <p>
	 * The default implementation returns <code>null</code>. Subclasses can extend or override.
	 * </p>
	 * <p>
	 * Note that most refactorings will implement {@link #createChange(IProgressMonitor)}
	 * rather than this method.
	 * </p>
	 *
	 * @param pm a progress monitor to report progress
	 *
	 * @return the change representing the workspace modifications to be executed
	 *  before the refactoring change or <code>null</code> if no changes are made
	 *
	 * @throws CoreException if an error occurred while creating the change
	 *
	 * @throws OperationCanceledException if the change creation got canceled
	 *
	 * @see #createChange(IProgressMonitor)
	 *
	 * @since 3.4
	 */
	public Change createPreChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return null;
	}

	/**
	 * Creates a {@link Change} object that contains the workspace modifications
	 * of this participant to be executed <em>after</em> the
	 * changes from the refactoring are executed. Note that this implies that the
	 * undo change of the returned Change object will be executed <em>before</em>
	 * the undo changes from the refactoring have been executed.
	 * <p>
	 * The changes provided by a participant <em>must</em>
	 * not conflict with any change provided by other participants or by the
	 * refactoring itself.
	 * <p>
	 * If the change conflicts with any change provided by other participants or
	 * by the refactoring itself, then change execution will fail and the
	 * participant will be disabled for the rest of the eclipse session.
	 * </p>
	 * <p>
	 * If an exception occurs while creating the change, the refactoring can not
	 * be carried out, and the participant will be disabled for the rest of the
	 * eclipse session.
	 * </p>
	 * <p>
	 * As of 3.1, a participant can manipulate text resources already manipulated by
	 * the processor as long as the textual manipulations don't conflict (i.e.
	 * the participant manipulates a different region of the text resource).
	 * The method must not return those changes in its change tree since the change
	 * is already part of another change tree. If the participant only manipulates
	 * shared changes, then it can return <code>null</code> to indicate that it didn't
	 * create own changes. A shared text change can be accessed via the method
	 * {@link #getTextChange(Object)}.
	 * </p>
	 *
	 * @param pm a progress monitor to report progress
	 *
	 * @return the change representing the workspace modifications to be executed
	 *  after the refactoring change or <code>null</code> if no changes are made
	 *
	 * @throws CoreException if an error occurred while creating the change
	 *
	 * @throws OperationCanceledException if the change creation got canceled
	 *
	 * @see #createPreChange(IProgressMonitor)
	 */
	public abstract Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

	/**
	 * Returns the text change for the given element or <code>null</code>
	 * if a text change doesn't exist. This method only returns a valid
	 * result during change creation. Outside of change creation always
	 * <code>null</code> is returned.
	 *
	 * @param element the element to be modified for which a text change
	 *  is requested
	 *
	 * @return the text change or <code>null</code> if no text change exists
	 *  for the element
	 *
	 *  @since 3.1
	 */
	public TextChange getTextChange(Object element) {
		return getProcessor().getRefactoring().getTextChange(element);
	}

	//---- helper method ----------------------------------------------------

	/* package */ void setDescriptor(ParticipantDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		fDescriptor= descriptor;
	}

	/* package */ ParticipantDescriptor getDescriptor() {
		return fDescriptor;
	}
}
