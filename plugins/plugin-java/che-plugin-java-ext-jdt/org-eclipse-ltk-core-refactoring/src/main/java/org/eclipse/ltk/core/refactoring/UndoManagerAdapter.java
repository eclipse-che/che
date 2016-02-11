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

/**
 * This adapter class provides default implementations for the
 * methods defined by the {@link IUndoManagerListener} interface.
 * <p>
 * This class may be subclassed by clients.
 * </p>
 * @since 3.0
 */
public class UndoManagerAdapter implements IUndoManagerListener {

	/**
	 * {@inheritDoc}
	 */
	public void undoStackChanged(IUndoManager manager) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void redoStackChanged(IUndoManager manager) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void aboutToPerformChange(IUndoManager manager, Change change) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void changePerformed(IUndoManager manager, Change change) {
	}
}

