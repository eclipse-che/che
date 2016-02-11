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
package org.eclipse.ltk.core.refactoring.participants;

/**
 * Delete arguments describes the data that a processor provides
 * to its delete participants.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DeleteArguments extends RefactoringArguments {

	private boolean fDeleteProjectContents;

	/**
	 * Creates a new delete arguments object (deleteProjectContents is <code>false</code>). 
	 */
	public DeleteArguments() {
		this(false);
	}
	
	/**
	 * Creates a new delete arguments object.
	 * 
	 * @param deleteProjectContents <code>true</code> if project contents will be deleted
	 * @since 3.6
	 */
	public DeleteArguments(boolean deleteProjectContents) {
		fDeleteProjectContents= deleteProjectContents;
	}
	
	/**
	 * Returns whether project contents will be deleted as well. This method is not applicable for
	 * file and folder deletions.
	 * 
	 * @return <code>true</code> if the refactoring will delete the project contents,
	 *         <code>false</code> if it only removes the reference from the workspace
	 * @since 3.6
	 */
	public boolean getDeleteProjectContents() {
		return fDeleteProjectContents;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.2
	 */
    public String toString() {
    	return "delete"; //$NON-NLS-1$
    }
}
