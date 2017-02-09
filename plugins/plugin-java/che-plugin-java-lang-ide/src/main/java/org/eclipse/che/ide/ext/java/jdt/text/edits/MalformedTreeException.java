/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.jdt.text.edits;

/**
 * Thrown to indicate that an edit got added to a parent edit but the child edit somehow conflicts with the parent or one of it
 * siblings.
 * <p>
 * This class is not intended to be serialized.
 * </p>
 *
 * @see TextEdit#addChild(TextEdit)
 * @see TextEdit#addChildren(TextEdit[])
 */
public class MalformedTreeException extends RuntimeException {

    // Not intended to be serialized
    private static final long serialVersionUID = 1L;

    private TextEdit fParent;

    private TextEdit fChild;

    /**
     * Constructs a new malformed tree exception.
     *
     * @param parent
     *         the parent edit
     * @param child
     *         the child edit
     * @param message
     *         the detail message
     */
    public MalformedTreeException(TextEdit parent, TextEdit child, String message) {
        super(message);
        fParent = parent;
        fChild = child;
    }

    /**
     * Returns the parent edit that caused the exception.
     *
     * @return the parent edit
     */
    public TextEdit getParent() {
        return fParent;
    }

    /**
     * Returns the child edit that caused the exception.
     *
     * @return the child edit
     */
    public TextEdit getChild() {
        return fChild;
    }

    void setParent(TextEdit parent) {
        fParent = parent;
    }
}
