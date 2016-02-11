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
package org.eclipse.ltk.core.refactoring.participants;

/**
 * A tagging interface to share a concrete {@link RefactoringParticipant} instance across
 * multiple elements to be refactored. Consider the example of moving
 * more than one file: if a corresponding move participant is not
 * tagged as a <code>ISharableParticipant</code> then a separate instance
 * of a participant is created for every file to be moved. If the
 * participant is marked as shared then only one instance is created
 * and the participant is responsible to handle all files to be moved.
 * <p>
 * The first element to be refactored will be added to the participant
 * via the participant specific <code>initialize(Object element)</code>
 * method. All subsequent elements will be added via the generic <code>
 * addElement(Object, RefactoringArguments)</code> method. Implementors
 * of this interface can assume that the refactoring arguments passed
 * to the <code>addElement</code> method conform to the participant. For
 * example the arguments are of type <code>MoveArguments</code> if this
 * interface is mixed into a move participant.
 * </p>
 * <p>
 * Clients may implement this interface to tag participants as shared.
 * </p>
 *
 * @see RefactoringParticipant
 * @since 3.0
 */
public interface ISharableParticipant {

	/**
	 * Adds the given element and argument to the refactoring participant.
	 *
	 * @param element the element to add
	 * @param arguments the corresponding arguments
	 */
	public void addElement(Object element, RefactoringArguments arguments);
}
