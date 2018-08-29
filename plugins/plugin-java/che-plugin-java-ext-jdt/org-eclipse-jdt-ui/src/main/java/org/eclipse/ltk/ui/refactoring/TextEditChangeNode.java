/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.ui.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.MultiStateTextFileChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.InternalTextEditChangeNode;
import org.eclipse.ltk.internal.ui.refactoring.PreviewNode;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.TextEditGroupNode;

/**
 * A special preview node to represent <code>TextEditBasedChange</code>s in the
 * preview tree.
 * <p>
 * This class should be subclassed by clients wishing to provide language
 * aware substructures for special <code>TextEditBasedChange<code>s. The preview
 * infrastructure accesses to preview node for a <code>TextEditBasedChange<code>
 * by asking the change for an adapter of type <code>TextEditChangeNode</code>.
 * If no adapter is returned, this default implementation will be used to present
 * <code>TextEditBasedChange<code> in the preview tree.
 * </p>
 * @since 3.2
 */
public class TextEditChangeNode extends InternalTextEditChangeNode {

  public abstract static class ChildNode extends PreviewNode {
    protected ChildNode(PreviewNode parent) {
      super(parent);
    }

    protected TextEditChangeNode getTextEditChangeNode() {
      return internalGetTextEditChangeNode(this);
    }
  }

  /**
   * Creates a new child node for the given parent and change group.
   *
   * @param parent the parent of the new child node
   * @param changeGroup the <code>TextEditBasedChangeGroup</code> this child node represents in the
   *     preview tree
   * @return the new child node
   */
  public static ChildNode createTextEditGroupNode(
      ChildNode parent, TextEditBasedChangeGroup changeGroup) {
    return new TextEditGroupNode(parent, changeGroup);
  }

  /**
   * Creates a new child node for the given parent and change group.
   *
   * @param parent the parent of the new child node
   * @param changeGroup the <code>TextEditBasedChangeGroup</code> this child node represents in the
   *     preview tree
   * @return the new child node
   */
  public static ChildNode createTextEditGroupNode(
      TextEditChangeNode parent, TextEditBasedChangeGroup changeGroup) {
    return new TextEditGroupNode(parent, changeGroup);
  }

  /**
   * Creates a new text edit change node for the given change.
   *
   * @param change the <code>TextEditBasedChange</code> this node represents in the preview tree
   */
  public TextEditChangeNode(TextEditBasedChange change) {
    // the parent will be set lazily via the initialize method
    super(null, change);
  }

  /**
   * Returns the <code>TextEditBasedChange</code> this node is
   * associated with.
   *
   * @return the <code>TextEditBasedChange<code>
   */
  public final TextEditBasedChange getTextEditBasedChange() {
    return super.getTextEditBasedChange();
  }

  /**
   * Returns the text used to render this node in the UI.
   *
   * @return a human readable representation of this node
   */
  public String getText() {
    Change change = getTextEditBasedChange();
    if (change instanceof TextFileChange) {
      IFile file = ((TextFileChange) change).getFile();
      return Messages.format(
          "{0} - {1}",
          new String[] {
            BasicElementLabels.getResourceName(file),
            BasicElementLabels.getPathLabel(file.getParent().getFullPath(), false)
          });
    }
    return super.getText();
  }

  /**
   * Returns the image descriptor used to render this node in the UI.
   *
   * @return the image descriptor representing this node
   */
  public ImageDescriptor getImageDescriptor() {
    return RefactoringPluginImages.DESC_OBJS_FILE_CHANGE;
  }

  protected ChildNode[] createChildNodes() {
    TextEditBasedChange change = getTextEditBasedChange();
    if (change instanceof MultiStateTextFileChange) {
      return new ChildNode
          [0]; // no edit preview & edit disabling possible in the MultiStateTextFileChange (edits
      // must be applied in sequence)
    }
    TextEditBasedChangeGroup[] groups = getSortedChangeGroups(change);
    ChildNode[] result = new ChildNode[groups.length];
    for (int i = 0; i < groups.length; i++) {
      result[i] = new TextEditGroupNode(this, groups[i]);
    }
    return result;
  }

  private static class OffsetComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      TextEditBasedChangeGroup c1 = (TextEditBasedChangeGroup) o1;
      TextEditBasedChangeGroup c2 = (TextEditBasedChangeGroup) o2;
      int p1 = getOffset(c1);
      int p2 = getOffset(c2);
      if (p1 < p2) return -1;
      if (p1 > p2) return 1;
      // same offset
      return 0;
    }

    private int getOffset(TextEditBasedChangeGroup edit) {
      return edit.getRegion().getOffset();
    }
  }

  private TextEditBasedChangeGroup[] getSortedChangeGroups(TextEditBasedChange change) {
    TextEditBasedChangeGroup[] groups = change.getChangeGroups();
    List result = new ArrayList(groups.length);
    for (int i = 0; i < groups.length; i++) {
      if (!groups[i].getTextEditGroup().isEmpty()) result.add(groups[i]);
    }
    Comparator comparator = new OffsetComparator();
    Collections.sort(result, comparator);
    return (TextEditBasedChangeGroup[]) result.toArray(new TextEditBasedChangeGroup[result.size()]);
  }
}
