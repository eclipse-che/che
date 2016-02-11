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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;

/**
 * An undo manager keeps track of performed changes. Use the method <code>addUndo</code>
 * to add change objects to the undo stack and <code>performUndo</code> and <code>
 * performRedo</code> to undo or redo changes.
 * <p>
 * This interface is not intended to be implemented by clients. Clients should use the
 * method {@link RefactoringCore#getUndoManager()} to access the refactoring undo manager.
 * </p>
 * <p>
 * As of 3.1 the implementation of the refactoring undo manager is based on the
 * {@link org.eclipse.core.commands.operations.IOperationHistory} provided by the
 * <code>org.eclipse.core.commands</code> plug-in.
 * </p>
 * <p>
 * As of 3.2 clients which need to examine refactorings which have been performed, undone or redone should use
 * {@link IRefactoringHistoryService} for enhanced functionality.
 * </p>
 *
 * @see org.eclipse.core.commands.operations.IOperationHistory
 *
 * @since 3.0
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IUndoManager {

	/**
	 * Adds a listener to the undo manager. Does nothing if the listener
	 * is already present.
	 *
	 * @param listener the listener to be added to the undo manager
	 */
	public void addListener(IUndoManagerListener listener);

	/**
	 * Removes the given listener from this undo manager. Does nothing if
	 * the listener isn't registered with this undo manager
	 *
	 * @param listener the listener to be removed
	 */
	public void removeListener(IUndoManagerListener listener);

	/**
	 * The infrastructure is going to perform the given change. If a
	 * client calls this method it must make sure that the corresponding
	 * method {@link #changePerformed(Change)} is called after the
	 * change got executed. A typically code snippet looks as follows:
	 * <pre>
	 *   Change change= ...;
	 *   try {
	 *     undoManager.aboutToPerformChange(change);
	 *     // execute change
	 *   } finally {
	 *     undoManager.changePerformed(change);
	 *   }
	 * </pre>
	 *
	 * @param change the change to be performed.
	 */
	public void aboutToPerformChange(Change change);

	/**
	 * The infrastructure has performed the given change.
	 *
	 * @param change the change that was performed
	 *
	 * @deprecated use #changePerformed(Change, boolean) instead
	 */
	public void changePerformed(Change change);

	/**
	 * The infrastructure has performed the given change.
	 *
	 * @param change the change that was performed
	 * @param successful <code>true</code> if the change got executed
	 *  successful; <code>false</code> otherwise.
	 *
	 * @since 3.1
	 */
	public void changePerformed(Change change, boolean successful);

	/**
	 * Adds a new undo change to this undo manager.
	 *
	 * @param name the name presented on the undo stack for the provided
	 *  undo change. The name must be human readable.
	 * @param change the undo change
	 */
	public void addUndo(String name, Change change);

	/**
	 * Returns <code>true</code> if there is anything to undo, otherwise
	 * <code>false</code>.
	 *
	 * @return <code>true</code> if there is anything to undo, otherwise
	 *  <code>false</code>
	 */
	public boolean anythingToUndo();

	/**
	 * Returns the name of the top most undo.
	 *
	 * @return the top most undo name. Returns <code>null</code> if there
	 * aren't any changes to undo.
	 */
	public String peekUndoName();

	/**
	 * Undo the top most undo change.
	 *
	 * @param query a proceed query to decide how to proceed if the validation
	 *  checking of the undo change to perform returns a non OK status and the
	 *  status isn't a fatal error. If <code>null</code> is passed in the
	 *  undo proceeds if the status is not a fatal error.
	 * @param pm a progress monitor to report progress during performing
	 *  the undo change
	 *
	 * @throws CoreException if performing the undo caused an exception
	 */
	public void performUndo(IValidationCheckResultQuery query, IProgressMonitor pm) throws CoreException;

	/**
	 * Returns <code>true</code> if there is anything to redo, otherwise
	 * <code>false</code>.
	 *
	 * @return <code>true</code> if there is anything to redo, otherwise
	 *  <code>false</code>
	 */
	public boolean anythingToRedo();

	/**
	 * Returns the name of the top most redo.
	 *
	 * @return the top most redo name. Returns <code>null</code> if there
	 * are no any changes to redo.
	 */
	public String peekRedoName();

	/**
	 * Redo the top most redo change.
	 *
	 * @param query a proceed query to decide how to proceed if the validation
	 *  checking of the redo change to perform returns a non OK status. If
	 *  <code>null</code> is passed in the undo proceeds if the status
	 *  is not a fatal error.
	 * @param pm a progress monitor to report progress during performing
	 *  the redo change
	 *
	 * @throws CoreException if performing the redo caused an exception
	 */
	public void performRedo(IValidationCheckResultQuery query, IProgressMonitor pm) throws CoreException;

	/**
	 * Flushes the undo manager's undo and redo stacks.
	 */
	public void flush();

	/**
	 * Shut down the undo manager.
	 */
	public void shutdown();
}
