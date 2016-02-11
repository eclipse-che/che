/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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

/**
 * Copy arguments describe the data that a processor
 * provides to its copy participants.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.1
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CopyArguments extends RefactoringArguments {

	private Object fDestination;
	private final ReorgExecutionLog fLog;

	/**
	 * Creates new copy arguments.
	 *
	 * @param destination the destination of the copy
	 * @param log the log for the execution of the reorg refactoring
	 */
	public CopyArguments(Object destination, ReorgExecutionLog log) {
		Assert.isNotNull(destination);
		Assert.isNotNull(log);
		fDestination= destination;
		fLog= log;
	}

	/**
	 * Returns the destination of the move
	 *
	 * @return the move's destination
	 */
	public Object getDestination() {
		return fDestination;
	}

	/**
	 * Returns the resource execution log.
	 *
	 * @return the resource execution log
	 */
	public ReorgExecutionLog getExecutionLog() {
		return fLog;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.2
	 */
	public String toString() {
		return "copy to " + fDestination.toString(); //$NON-NLS-1$
	}
}
