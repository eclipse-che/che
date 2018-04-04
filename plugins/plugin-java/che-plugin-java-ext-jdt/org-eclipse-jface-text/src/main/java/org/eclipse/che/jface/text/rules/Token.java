/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2007 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.rules;

import org.eclipse.core.runtime.Assert;

/** Standard implementation of <code>IToken</code>. */
public class Token implements IToken {

  /** Internal token type: Undefined */
  private static final int T_UNDEFINED = 0;
  /** Internal token type: EOF */
  private static final int T_EOF = 1;
  /** Internal token type: Whitespace */
  private static final int T_WHITESPACE = 2;
  /** Internal token type: Others */
  private static final int T_OTHER = 3;

  /** Standard token: Undefined. */
  public static final IToken UNDEFINED = new Token(T_UNDEFINED);
  /** Standard token: End Of File. */
  public static final IToken EOF = new Token(T_EOF);
  /** Standard token: Whitespace. */
  public static final IToken WHITESPACE = new Token(T_WHITESPACE);

  /**
   * Standard token: Neither {@link #UNDEFINED}, {@link #WHITESPACE}, nor {@link #EOF}.
   *
   * @deprecated will be removed
   */
  public static final IToken OTHER = new Token(T_OTHER);

  /** The type of this token */
  private int fType;
  /** The data associated with this token */
  private Object fData;

  /**
   * Creates a new token according to the given specification which does not have any data attached
   * to it.
   *
   * @param type the type of the token
   * @since 2.0
   */
  private Token(int type) {
    fType = type;
    fData = null;
  }

  /**
   * Creates a new token which represents neither undefined, whitespace, nor EOF. The newly created
   * token has the given data attached to it.
   *
   * @param data the data attached to the newly created token
   */
  public Token(Object data) {
    fType = T_OTHER;
    fData = data;
  }

  /**
   * Re-initializes the data of this token. The token may not represent undefined, whitespace, or
   * EOF.
   *
   * @param data to be attached to the token
   * @since 2.0
   */
  public void setData(Object data) {
    Assert.isTrue(isOther());
    fData = data;
  }

  /*
   * @see IToken#getData()
   */
  public Object getData() {
    return fData;
  }

  /*
   * @see IToken#isOther()
   */
  public boolean isOther() {
    return (fType == T_OTHER);
  }

  /*
   * @see IToken#isEOF()
   */
  public boolean isEOF() {
    return (fType == T_EOF);
  }

  /*
   * @see IToken#isWhitespace()
   */
  public boolean isWhitespace() {
    return (fType == T_WHITESPACE);
  }

  /*
   * @see IToken#isUndefined()
   */
  public boolean isUndefined() {
    return (fType == T_UNDEFINED);
  }
}
