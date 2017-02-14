/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.ide.api.editor.text.rules;


import org.eclipse.che.ide.runtime.Assert;

/** Standard implementation of <code>IToken</code>. */
public class TokenImpl implements Token {

    /** Internal token type: Undefined */
    private static final int   T_UNDEFINED  = 0;
    /** Internal token type: EOF */
    private static final int   T_EOF        = 1;
    /** Internal token type: Whitespace */
    private static final int   T_WHITESPACE = 2;
    /** Internal token type: Others */
    private static final int   T_OTHER      = 3;
    /** Standard token: Undefined. */
    public static final  Token UNDEFINED    = new TokenImpl(T_UNDEFINED);
    /** Standard token: End Of File. */
    public static final  Token EOF          = new TokenImpl(T_EOF);
    /** Standard token: Whitespace. */
    public static final  Token WHITESPACE   = new TokenImpl(T_WHITESPACE);
    /** The type of this token */
    private int    fType;
    /** The data associated with this token */
    private Object fData;

    /**
     * Creates a new token according to the given specification which does not
     * have any data attached to it.
     *
     * @param type
     *         the type of the token
     */
    private TokenImpl(int type) {
        fType = type;
        fData = null;
    }

    /**
     * Creates a new token which represents neither undefined, whitespace, nor EOF.
     * The newly created token has the given data attached to it.
     *
     * @param data
     *         the data attached to the newly created token
     */
    public TokenImpl(Object data) {
        fType = T_OTHER;
        fData = data;
    }

    /*
     * @see IToken#getData()
     */
    @Override
    public Object getData() {
        return fData;
    }

    /**
     * Re-initializes the data of this token. The token may not represent
     * undefined, whitespace, or EOF.
     *
     * @param data
     *         to be attached to the token
     */
    public void setData(Object data) {
        Assert.isTrue(isOther());
        fData = data;
    }

    /*
     * @see IToken#isOther()
     */
    @Override
    public boolean isOther() {
        return (fType == T_OTHER);
    }

    /*
     * @see IToken#isEOF()
     */
    @Override
    public boolean isEOF() {
        return (fType == T_EOF);
    }

    /*
     * @see IToken#isWhitespace()
     */
    @Override
    public boolean isWhitespace() {
        return (fType == T_WHITESPACE);
    }

    /*
     * @see IToken#isUndefined()
     */
    @Override
    public boolean isUndefined() {
        return (fType == T_UNDEFINED);
    }
}
