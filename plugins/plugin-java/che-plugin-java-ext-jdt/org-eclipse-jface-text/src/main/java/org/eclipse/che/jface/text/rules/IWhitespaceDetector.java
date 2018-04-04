/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2005 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.rules;

/**
 * Defines the interface by which <code>WhitespaceRule</code> determines whether a given character
 * is to be considered whitespace in the current context.
 */
public interface IWhitespaceDetector {

  /**
   * Returns whether the specified character is whitespace.
   *
   * @param c the character to be checked
   * @return <code>true</code> if the specified character is a whitespace char
   */
  boolean isWhitespace(char c);
}
