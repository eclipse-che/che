/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.util;

import org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/** @since 3.4 */
public class TextEditUtil {

  /**
   * Inserts the <code>edit</code> into <code>parent</code>.
   *
   * @param parent the target of the operation
   * @param edit the edit to insert into parent
   * @throws MalformedTreeException is edit can't be inserted int parent
   */
  public static void insert(TextEdit parent, TextEdit edit) {
    TextChangeCompatibility.insert(parent, edit);
  }

  /**
   * Returns true if the given <code>edit</code> is minimal.
   *
   * <p>That is if:
   *
   * <ul>
   *   <li><b>true</b> if <code>edit</code> is a leaf
   *   <li>if <code>edit</code> is a inner node then <b>true</b> if
   *       <ul>
   *         <li><code>edit</code> has same size as all its children
   *         <li><code>isPacked</code> is <b>true</b> for all children
   *       </ul>
   * </ul>
   *
   * @param edit the edit to verify
   * @return true if edit is minimal
   * @since 3.4
   */
  public static boolean isPacked(TextEdit edit) {
    if (!(edit instanceof MultiTextEdit)) return true;

    if (!edit.hasChildren()) return true;

    TextEdit[] children = edit.getChildren();
    if (edit.getOffset() != children[0].getOffset()) return false;

    if (edit.getExclusiveEnd() != children[children.length - 1].getExclusiveEnd()) return false;

    for (int i = 0; i < children.length; i++) {
      if (!isPacked(children[i])) return false;
    }

    return true;
  }

  /**
   * Degenerates the given edit tree into a list.<br>
   * All nodes of the result are leafs.<br>
   * <strong>The given edit is modified and can no longer be used.</strong>
   *
   * @param edit the edit tree to flatten
   * @return a list of edits
   * @since 3.4
   */
  public static MultiTextEdit flatten(TextEdit edit) {
    MultiTextEdit result = new MultiTextEdit();
    flatten(edit, result);
    return result;
  }

  private static void flatten(TextEdit edit, MultiTextEdit result) {
    if (!edit.hasChildren()) {
      result.addChild(edit);
    } else {
      TextEdit[] children = edit.getChildren();
      for (int i = 0; i < children.length; i++) {
        TextEdit child = children[i];
        child.getParent().removeChild(0);
        flatten(child, result);
      }
    }
  }

  /**
   * Does any node in <code>edit1</code> overlap with any other node in <code>edit2</code>.
   *
   * <p>If this returns true then the two edit trees can be merged into one.
   *
   * @param edit1 the edit to compare against edit2
   * @param edit2 the edit to compare against edit1
   * @return true of no node overlaps with any other node
   * @since 3.4
   */
  public static boolean overlaps(TextEdit edit1, TextEdit edit2) {
    if (edit1 instanceof MultiTextEdit && edit2 instanceof MultiTextEdit) {
      MultiTextEdit multiTextEdit1 = (MultiTextEdit) edit1;
      if (!multiTextEdit1.hasChildren()) return false;

      MultiTextEdit multiTextEdit2 = (MultiTextEdit) edit2;
      if (!multiTextEdit2.hasChildren()) return false;

      TextEdit[] children1 = multiTextEdit1.getChildren();
      TextEdit[] children2 = multiTextEdit2.getChildren();

      int i1 = 0;
      int i2 = 0;
      while (i1 < children1.length && i2 < children2.length) {
        while (children1[i1].getExclusiveEnd() < children2[i2].getOffset()) {
          i1++;
          if (i1 >= children1.length) return false;
        }
        while (children2[i2].getExclusiveEnd() < children1[i1].getOffset()) {
          i2++;
          if (i2 >= children2.length) return false;
        }

        if (children1[i1].getExclusiveEnd() < children2[i2].getOffset()) continue;

        if (overlaps(children1[i1], children2[i2])) return true;

        int mergeEnd = Math.max(children1[i1].getExclusiveEnd(), children2[i2].getExclusiveEnd());

        i1++;
        i2++;

        if (i1 < children1.length && children1[i1].getOffset() < mergeEnd) {
          return true;
        }
        if (i2 < children2.length && children2[i2].getOffset() < mergeEnd) {
          return true;
        }
      }

      return false;
    } else if (edit1 instanceof MultiTextEdit) {
      MultiTextEdit multiTextEdit1 = (MultiTextEdit) edit1;
      if (!multiTextEdit1.hasChildren()) return false;

      TextEdit[] children = multiTextEdit1.getChildren();

      int i = 0;
      while (children[i].getExclusiveEnd() < edit2.getOffset()) {
        i++;
        if (i >= children.length) return false;
      }

      if (overlaps(children[i], edit2)) return true;

      return false;
    } else if (edit2 instanceof MultiTextEdit) {
      MultiTextEdit multiTextEdit2 = (MultiTextEdit) edit2;
      if (!multiTextEdit2.hasChildren()) return false;

      TextEdit[] children = multiTextEdit2.getChildren();

      int i = 0;
      while (children[i].getExclusiveEnd() < edit1.getOffset()) {
        i++;
        if (i >= children.length) return false;
      }

      if (overlaps(children[i], edit1)) return true;

      return false;
    } else {
      int start1 = edit1.getOffset();
      int end1 = start1 + edit1.getLength();
      int start2 = edit2.getOffset();
      int end2 = start2 + edit2.getLength();

      if (start1 > end2) return false;

      if (start2 > end1) return false;

      return true;
    }
  }

  /**
   * Create an edit which contains <code>edit1</code> and <code>edit2</code>
   *
   * <p>If <code>edit1</code> overlaps <code>edit2</code> this method fails with a {@link
   * MalformedTreeException}
   *
   * <p><strong>The given edits are modified and they can no longer be used.</strong>
   *
   * @param edit1 the edit to merge with edit2
   * @param edit2 the edit to merge with edit1
   * @return the merged tree
   * @throws MalformedTreeException if {@link #overlaps(TextEdit, TextEdit)} returns <b>true</b>
   * @see #overlaps(TextEdit, TextEdit)
   * @since 3.4
   */
  public static TextEdit merge(TextEdit edit1, TextEdit edit2) {
    if (edit1 instanceof MultiTextEdit && !edit1.hasChildren()) {
      return edit2;
    }

    if (edit2 instanceof MultiTextEdit && !edit2.hasChildren()) {
      return edit1;
    }

    MultiTextEdit result = new MultiTextEdit();
    merge(edit1, edit2, result);
    return result;
  }

  private static void merge(TextEdit edit1, TextEdit edit2, MultiTextEdit result) {
    if (edit1 instanceof MultiTextEdit && edit2 instanceof MultiTextEdit) {
      MultiTextEdit multiTextEdit1 = (MultiTextEdit) edit1;
      if (!multiTextEdit1.hasChildren()) {
        result.addChild(edit2);
        return;
      }

      MultiTextEdit multiTextEdit2 = (MultiTextEdit) edit2;
      if (!multiTextEdit2.hasChildren()) {
        result.addChild(edit1);
        return;
      }

      TextEdit[] children1 = multiTextEdit1.getChildren();
      TextEdit[] children2 = multiTextEdit2.getChildren();

      int i1 = 0;
      int i2 = 0;
      while (i1 < children1.length && i2 < children2.length) {

        while (i1 < children1.length
            && children1[i1].getExclusiveEnd() < children2[i2].getOffset()) {
          edit1.removeChild(0);
          result.addChild(children1[i1]);
          i1++;
        }
        if (i1 >= children1.length) break;

        while (i2 < children2.length
            && children2[i2].getExclusiveEnd() < children1[i1].getOffset()) {
          edit2.removeChild(0);
          result.addChild(children2[i2]);
          i2++;
        }
        if (i2 >= children2.length) break;

        if (children1[i1].getExclusiveEnd() < children2[i2].getOffset()) continue;

        edit1.removeChild(0);
        edit2.removeChild(0);
        merge(children1[i1], children2[i2], result);

        i1++;
        i2++;
      }

      while (i1 < children1.length) {
        edit1.removeChild(0);
        result.addChild(children1[i1]);
        i1++;
      }

      while (i2 < children2.length) {
        edit2.removeChild(0);
        result.addChild(children2[i2]);
        i2++;
      }
    } else if (edit1 instanceof MultiTextEdit) {
      TextEdit[] children = edit1.getChildren();

      int i = 0;
      while (children[i].getExclusiveEnd() < edit2.getOffset()) {
        edit1.removeChild(0);
        result.addChild(children[i]);
        i++;
        if (i >= children.length) {
          result.addChild(edit2);
          return;
        }
      }
      edit1.removeChild(0);
      merge(children[i], edit2, result);
      i++;
      while (i < children.length) {
        edit1.removeChild(0);
        result.addChild(children[i]);
        i++;
      }
    } else if (edit2 instanceof MultiTextEdit) {
      TextEdit[] children = edit2.getChildren();

      int i = 0;
      while (children[i].getExclusiveEnd() < edit1.getOffset()) {
        edit2.removeChild(0);
        result.addChild(children[i]);
        i++;
        if (i >= children.length) {
          result.addChild(edit1);
          return;
        }
      }
      edit2.removeChild(0);
      merge(edit1, children[i], result);
      i++;
      while (i < children.length) {
        edit2.removeChild(0);
        result.addChild(children[i]);
        i++;
      }
    } else {
      if (edit1.getExclusiveEnd() < edit2.getOffset()) {
        result.addChild(edit1);
        result.addChild(edit2);
      } else {
        result.addChild(edit2);
        result.addChild(edit1);
      }
    }
  }
}
