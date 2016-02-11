/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource.undostates;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;

/**
 * IFileContentDescription is a description of a file's content.
 *
 * This class is not intended to be instantiated or used by clients.
 *
 * @since 3.4
 *
 */
public interface IFileContentDescription {
	/**
	 * Returns an open input stream on the contents of the file described. The
	 * client is responsible for closing the stream when finished.
	 *
	 * @return an input stream containing the contents of the file
	 * @throws CoreException
	 *             any CoreException encountered retrieving the contents
	 */
	public InputStream getContents() throws CoreException;

	/**
	 * Returns whether this file content description still exists. If it does
	 * not exist, it will be unable to produce the contents.
	 *
	 * @return <code>true</code> if this description exists, and
	 *         <code>false</code> if it does not
	 */
	public boolean exists();

	/**
	 * Returns the name of a charset encoding to be used when decoding the
	 * contents into characters. Returns <code>null</code> if a charset
	 * has not been explicitly specified.
	 *
	 * @return the name of a charset, or <code>null</code>
	 * @throws CoreException
	 *             any CoreException encountered while determining the character
	 *             set
	 *
	 */
	public String getCharset() throws CoreException;
}
