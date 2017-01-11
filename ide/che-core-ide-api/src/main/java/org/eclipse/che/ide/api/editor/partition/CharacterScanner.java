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
package org.eclipse.che.ide.api.editor.partition;

import java.util.List;


/**
 * Defines the interface of a character scanner used by rules.
 * Rules may request the next character or ask the character
 * scanner to unread the last read character.
 */
public interface CharacterScanner {

    /** The value returned when this scanner has read EOF. */
    public static final int EOF = -1;

    /**
     * Provides rules access to the legal line delimiters. The returned
     * object may not be modified by clients.
     *
     * @return the legal line delimiters
     */
    List<String> getLegalLineDelimiters();

    /**
     * Returns the column of the character scanner.
     *
     * @return the column of the character scanner
     */
    int getColumn();

    /**
     * Returns the next character or EOF if end of file has been reached
     *
     * @return the next character or EOF
     */
    int read();

    /** Rewinds the scanner before the last read character. */
    void unread();
}
