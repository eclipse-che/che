/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.source;

/**
 * Describes a range of lines.
 *
 * <p>Note that the number of lines is 1-based, e.g. <code>getStartLine() + getNumberOfLines()
 * </code> computes the first line <em>after</em> the range, and a range with <code>
 * getNumberOfLines() == 0</code> is empty.
 *
 * @since 3.0
 */
public interface ILineRange {

  /**
   * Returns the start line of this line range or <code>-1</code>.
   *
   * @return the start line of this line range or <code>-1</code> if this line range is invalid.
   */
  int getStartLine();

  /**
   * Returns the number of lines of this line range or <code>-1</code>.
   *
   * @return the number of lines in this line range or <code>-1</code> if this line range is
   *     invalid.
   */
  int getNumberOfLines();
}
