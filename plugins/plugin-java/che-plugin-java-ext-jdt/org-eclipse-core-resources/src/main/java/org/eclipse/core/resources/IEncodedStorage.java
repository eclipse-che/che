/**
 * ***************************************************************************** Copyright (c) 2004,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.resources;

import org.eclipse.core.runtime.CoreException;

/**
 * A storage that knows how its contents are encoded.
 *
 * <p>The <code>IEncodedStorage</code> interface extends <code>IStorage</code> in order to provide
 * access to the charset to be used when decoding its contents.
 *
 * <p>Clients may implement this interface.
 *
 * @since 3.0
 */
public interface IEncodedStorage extends IStorage {
  /**
   * Returns the name of a charset encoding to be used when decoding this storage's contents into
   * characters. Returns <code>null</code> if a proper encoding cannot be determined.
   *
   * <p>Note that this method does not check whether the result is a supported charset name. Callers
   * should be prepared to handle <code>UnsupportedEncodingException</code> where this charset is
   * used.
   *
   * @return the name of a charset, or <code>null</code>
   * @exception CoreException if an error happens while determining the charset. See any refinements
   *     for more information.
   * @see IStorage#getContents()
   */
  public String getCharset() throws CoreException;
}
