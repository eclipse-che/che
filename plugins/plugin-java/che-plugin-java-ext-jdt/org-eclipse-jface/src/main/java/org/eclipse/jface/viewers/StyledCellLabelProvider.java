/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Michael Krkoska - initial API
 * and implementation (bug 188333) Pawel Piech - Bug 291245 - [Viewers]
 * StyledCellLabelProvider.paint(...) does not respect column alignment
 * *****************************************************************************
 */
package org.eclipse.jface.viewers;

/**
 * A {@link StyledCellLabelProvider} supports styled labels by using owner draw. Besides the styles
 * in labels, the label provider preserves native viewer behavior:
 *
 * <ul>
 *   <li>similar image and label positioning
 *   <li>native drawing of focus and selection
 * </ul>
 *
 * <p>For providing the label's styles, create a subclass and overwrite {@link
 * StyledCellLabelProvider#update(ViewerCell)} to return set all information needed to render a
 * element. Use {@link ViewerCell#setStyleRanges(StyleRange[])} to set style ranges on the label.
 *
 * @since 3.4
 */
public abstract class StyledCellLabelProvider {

  /**
   * Applies decoration styles to the decorated string and adds the styles of the previously
   * undecorated string.
   *
   * <p>If the <code>decoratedString</code> contains the <code>styledString</code>, then the result
   * keeps the styles of the <code>styledString</code> and styles the decorations with the <code>
   * decorationStyler</code>. Otherwise, the decorated string is returned without any styles.
   *
   * @param decoratedString the decorated string
   * @param decorationStyler the styler to use for the decoration or <code>null</code> for no styles
   * @param styledString the original styled string
   * @return the styled decorated string (can be the given <code>styledString</code>)
   * @since 3.5
   */
  public static StyledString styleDecoratedString(
      String decoratedString, StyledString.Styler decorationStyler, StyledString styledString) {
    String label = styledString.getString();
    int originalStart = decoratedString.indexOf(label);
    if (originalStart == -1) {
      return new StyledString(decoratedString); // the decorator did something wild
    }

    if (decoratedString.length() == label.length()) return styledString;

    if (originalStart > 0) {
      StyledString newString =
          new StyledString(decoratedString.substring(0, originalStart), decorationStyler);
      newString.append(styledString);
      styledString = newString;
    }
    if (decoratedString.length() > originalStart + label.length()) { // decorator appended something
      return styledString.append(
          decoratedString.substring(originalStart + label.length()), decorationStyler);
    }
    return styledString; // no change
  }
}
