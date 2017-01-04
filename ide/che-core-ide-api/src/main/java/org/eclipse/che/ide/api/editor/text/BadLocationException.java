/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.text;

/**
 * Indicates the attempt to access a non-existing position. The attempt has been performed on a text store such as a document or
 * string.
 * <p>
 * This class is not intended to be serialized.
 * </p>
 */
public class BadLocationException extends Exception {

    /**
     * Serial version UID for this class.
     * <p>
     * Note: This class is not intended to be serialized.
     * </p>
     */
    private static final long serialVersionUID = 3257281452776370224L;

    /** Creates a new bad location exception. */
    public BadLocationException() {
        super();
    }

    /**
     * Creates a new bad location exception.
     *
     * @param message
     *         the exception message
     */
    public BadLocationException(String message) {
        super(message);
    }
}
