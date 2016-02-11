/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import java.io.InputStream;
import org.eclipse.core.runtime.*;

/**
 * A storage object represents a set of bytes which can be accessed.
 * These may be in the form of an <code>IFile</code> or <code>IFileState</code>
 * or any other object supplied by user code.  The main role of an <code>IStorage</code>
 * is to provide a uniform API for access to, and presentation of, its content.
 * <p>
 * Storage objects implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * <p>
 * Clients may implement this interface.
 * </p>
 * </p>
 */
public interface IStorage extends IAdaptable {
	/**
	 * Returns an open input stream on the contents of this storage.
	 * The caller is responsible for closing the stream when finished.
	 *
	 * @return an input stream containing the contents of this storage
	 * @exception CoreException if the contents of this storage could 
	 *		not be accessed.   See any refinements for more information.
	 */
	public InputStream getContents() throws CoreException;

	/**
	 * Returns the full path of this storage.  The returned value
	 * depends on the implementor/extender.  A storage need not
	 * have a path.
	 *
	 * @return the path related to the data represented by this storage or 
	 *		<code>null</code> if none.
	 */
	public IPath getFullPath();

	/**
	 * Returns the name of this storage. 
	 * The name of a storage is synonymous with the last segment
	 * of its full path though if the storage does not have a path,
	 * it may still have a name.
	 *
	 * @return the name of the data represented by this storage,
	 *		or <code>null</code> if this storage has no name
	 * @see #getFullPath()
	 */
	public String getName();

	/**
	 * Returns whether this storage is read-only.
	 *
	 * @return <code>true</code> if this storage is read-only
	 */
	public boolean isReadOnly();
}
