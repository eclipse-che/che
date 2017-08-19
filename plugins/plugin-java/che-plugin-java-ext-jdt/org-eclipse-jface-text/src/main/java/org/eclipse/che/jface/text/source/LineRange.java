/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2005 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.source;

/**
 * Default implementation of {@link ILineRange}.
 *
 * @since 3.0
 */
public final class LineRange implements ILineRange {

  private int fStartLine;
  private int fNumberOfLines;

  /**
   * Creates a new line range with the given specification.
   *
   * @param startLine the start line
   * @param numberOfLines the number of lines
   */
  public LineRange(int startLine, int numberOfLines) {
    fStartLine = startLine;
    fNumberOfLines = numberOfLines;
  }

  /*
   * @see org.eclipse.jface.text.source.ILineRange#getStartLine()
   */
  public int getStartLine() {
    return fStartLine;
  }

  /*
   * @see org.eclipse.jface.text.source.ILineRange#getNumberOfLines()
   */
  public int getNumberOfLines() {
    return fNumberOfLines;
  }
}
