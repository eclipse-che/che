/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.text.java.correction;


/**
 * Correction proposals can implement this interface to be invokable by a command (which can be
 * bound to a keyboard shortcut).
 * 
 * @since 3.8
 */
public interface ICommandAccess {

	/**
	 * Correction commands starting with this prefix will be handled by the Java editor.
	 */
	public static final String COMMAND_ID_PREFIX= "org.eclipse.jdt.ui.correction."; //$NON-NLS-1$

	/**
	 * Commands for quick assists must have this suffix.
	 */
	public static final String ASSIST_SUFFIX= ".assist"; //$NON-NLS-1$

	/**
	 * Returns the id of the command that should invoke this correction proposal.
	 * 
	 * @return the id of the command or <code>null</code> if this proposal does not have a command.
	 *         This id must start with {@link #COMMAND_ID_PREFIX} to be recognized as a correction
	 *         command. In addition, the id must end with {@link #ASSIST_SUFFIX} to be recognized as
	 *         a quick assist command.
	 */
	String getCommandId();

}
