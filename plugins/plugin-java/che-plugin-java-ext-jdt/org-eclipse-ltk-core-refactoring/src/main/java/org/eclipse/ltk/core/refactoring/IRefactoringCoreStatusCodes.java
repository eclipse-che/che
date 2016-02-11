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
package org.eclipse.ltk.core.refactoring;

/**
 * Status codes used by the refactoring core plug-in.
 * <p>
 * Note: this interface is not intended to be implemented by clients.
 * </p>
 *
 * @see org.eclipse.core.runtime.Status
 *
 * @since 3.0
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRefactoringCoreStatusCodes {

	/**
	 * Status code (value 10000) indicating an internal error.
	 */
	public static final int INTERNAL_ERROR= 10000;

	/**
	 * Status code (value 10001) indicating that a bad location exception has
	 * occurred during change execution.
	 *
	 * @see org.eclipse.jface.text.BadLocationException
	 */
	public static final int BAD_LOCATION= 10001;

	/**
	 * Status code (value 10002) indicating that an validateEdit call has
	 * changed the content of a file on disk.
	 */
	public static final int VALIDATE_EDIT_CHANGED_CONTENT= 10002;

	/**
	 * Status code (value 10003) indicating that a condition checker already
	 * exists in a shared condition checking context.
	 */
	public static final int CHECKER_ALREADY_EXISTS_IN_CONTEXT= 10003;

	/**
	 * Status code (value 10004) indicating that a refactoring history has been
	 * read which does not contain version information.
	 *
	 * @since 3.2
	 */
	public static final int MISSING_REFACTORING_HISTORY_VERSION= 10004;

	/**
	 * Status code (value 10005) indicating that a refactoring history with an
	 * unsupported version has been read.
	 *
	 * @since 3.2
	 */
	public static final int UNSUPPORTED_REFACTORING_HISTORY_VERSION= 10005;

	/**
	 * Status code (value 10006) indicating that a general error has occurred
	 * during I/O of a refactoring history.
	 *
	 * @since 3.2
	 */
	public static final int REFACTORING_HISTORY_IO_ERROR= 10006;

	/**
	 * Status code (value 10007) indicating that the format of a refactoring
	 * history contains errors.
	 *
	 * @since 3.2
	 */
	public static final int REFACTORING_HISTORY_FORMAT_ERROR= 10007;

	/**
	 *
	 * Status code (value 10008) indicating that participants are disabled
	 * because a refactoring threw an exception.
	 *
	 * @since 3.4
	 */
	public static final int REFACTORING_EXCEPTION_DISABLED_PARTICIPANTS= 10008;

	/**
	 *
	 * Status code (value 10009) indicating that a participant was disabled, either
	 * due to an exception or other reason logged elsewhere.
	 *
	 * @since 3.4
	 */
	public static final int PARTICIPANT_DISABLED= 10009;


}
