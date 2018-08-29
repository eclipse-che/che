/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text;

/**
 * This interface represents a textual selection. A text selection is a range of characters.
 * Although a text selection is a snapshot taken at a particular point in time, it must not copy the
 * line information and the selected text from the selection provider.
 *
 * <p>If, for example, the selection provider is a text viewer ( {@link ITextViewer}), and a text
 * selection is created for the range [5, 10], the line formation for the 5th character must not be
 * determined and remembered at the point of creation. It can rather be determined at the point,
 * when <code>getStartLine</code> is called. If the source viewer range [0, 15] has been changed in
 * the meantime between the creation of the text selection object and the invocation of <code>
 * getStartLine</code>, the returned line number may differ from the line number of the 5th
 * character at the point of creation of the text selection object.
 *
 * <p>The contract of this interface is that weak in order to allow for efficient implementations.
 *
 * <p>Clients may implement this interface or use the default implementation provided by {@link
 * TextSelection}.
 *
 * @see TextSelection
 */
public interface ITextSelection /* extends ISelection */ {

  /**
   * Returns the offset of the selected text.
   *
   * @return the offset of the selected text or -1 if there is no valid text information
   */
  int getOffset();

  /**
   * Returns the length of the selected text.
   *
   * @return the length of the selected text or -1 if there is no valid text information
   */
  int getLength();

  /**
   * Returns number of the line containing the offset of the selected text. If the underlying text
   * has been changed between the creation of this selection object and the call of this method, the
   * value returned might differ from what it would have been at the point of creation.
   *
   * @return the start line of this selection or -1 if there is no valid line information
   */
  int getStartLine();

  /**
   * Returns the number of the line containing the last character of the selected text. If the
   * underlying text has been changed between the creation of this selection object and the call of
   * this method, the value returned might differ from what it would have been at the point of
   * creation.
   *
   * @return the end line of this selection or -1 if there is no valid line information
   */
  int getEndLine();

  /**
   * Returns the selected text. If the underlying text has been changed between the creation of this
   * selection object and the call of this method, the value returned might differ from what it
   * would have been at the point of creation.
   *
   * @return the selected text or <code>null</code> if there is no valid text information
   */
  String getText();
}
