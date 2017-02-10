/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.ide.ext.java.jdt.text.rules;


import org.eclipse.che.ide.api.editor.text.rules.Token;
import org.eclipse.che.ide.ext.java.jdt.text.Document;

/**
 * A token scanner scans a range of a document and reports about the token it finds.
 * A scanner has state. When asked, the scanner returns the offset and the length of the
 * last found token.
 *
 * @see Token
 */
public interface TokenScanner {
    /**
     * Configures the scanner by providing access to the document range that should
     * be scanned.
     *
     * @param document
     *         the document to scan
     * @param offset
     *         the offset of the document range to scan
     * @param length
     *         the length of the document range to scan
     */
    void setRange(Document document, int offset, int length);

    /**
     * Returns the next token in the document.
     *
     * @return the next token in the document
     */
    Token nextToken();

    /**
     * Returns the offset of the last token read by this scanner.
     *
     * @return the offset of the last token read by this scanner
     */
    int getTokenOffset();

    /**
     * Returns the length of the last token read by this scanner.
     *
     * @return the length of the last token read by this scanner
     */
    int getTokenLength();
}
