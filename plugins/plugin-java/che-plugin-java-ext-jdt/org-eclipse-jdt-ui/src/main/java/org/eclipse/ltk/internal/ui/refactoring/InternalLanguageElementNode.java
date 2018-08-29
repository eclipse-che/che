/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode.ChildNode;

public abstract class InternalLanguageElementNode extends TextEditChangeNode.ChildNode {

  private List /*<ChildNode>*/ fChildren;
  private GroupCategorySet fGroupCategories;

  protected InternalLanguageElementNode(PreviewNode parent) {
    super(parent);
  }

  public ChangePreviewViewerDescriptor getChangePreviewViewerDescriptor() throws CoreException {
    InternalTextEditChangeNode element = getTextEditChangeNode();
    if (element == null) return null;
    return element.getChangePreviewViewerDescriptor();
  }

  public ChangePreview feedInput(IChangePreviewViewer viewer, List categories)
      throws CoreException {
    InternalTextEditChangeNode element = getTextEditChangeNode();
    if (element != null) {
      Change change = element.getChange();
      if (change instanceof TextEditBasedChange) {
        List groups = collectTextEditBasedChangeGroups(categories);
        return viewer.setInput(
            TextEditChangePreviewViewer.createInput(
                change,
                (TextEditBasedChangeGroup[])
                    groups.toArray(new TextEditBasedChangeGroup[groups.size()]),
                getTextRange()));
      }
    } else {
      return viewer.setInput(null);
    }
    return null;
  }

  public void setEnabled(boolean enabled) {
    for (Iterator iter = fChildren.iterator(); iter.hasNext(); ) {
      PreviewNode element = (PreviewNode) iter.next();
      element.setEnabled(enabled);
    }
  }

  public void setEnabledShallow(boolean enabled) {
    // do nothing. We don't manage an own enablement state.
  }

  public int getActive() {
    Assert.isTrue(fChildren.size() > 0);
    int result = ((PreviewNode) fChildren.get(0)).getActive();
    for (int i = 1; i < fChildren.size(); i++) {
      PreviewNode element = (PreviewNode) fChildren.get(i);
      result = PreviewNode.ACTIVATION_TABLE[element.getActive()][result];
      if (result == PreviewNode.PARTLY_ACTIVE) break;
    }
    return result;
  }

  public PreviewNode[] getChildren() {
    if (fChildren == null) return PreviewNode.EMPTY_CHILDREN;
    return (PreviewNode[]) fChildren.toArray(new PreviewNode[fChildren.size()]);
  }

  boolean hasOneGroupCategory(List categories) {
    if (fChildren == null) return false;
    return getGroupCategorySet().containsOneCategory(categories);
  }

  public boolean hasDerived() {
    if (fChildren == null) return false;
    for (Iterator iter = fChildren.iterator(); iter.hasNext(); ) {
      PreviewNode node = (PreviewNode) iter.next();
      if (node.hasDerived()) return true;
    }
    return false;
  }

  private GroupCategorySet getGroupCategorySet() {
    if (fGroupCategories == null) {
      fGroupCategories = GroupCategorySet.NONE;
      for (Iterator iter = fChildren.iterator(); iter.hasNext(); ) {
        PreviewNode node = (PreviewNode) iter.next();
        GroupCategorySet other = null;
        if (node instanceof TextEditGroupNode) {
          other = ((TextEditGroupNode) node).getGroupCategorySet();
        } else if (node instanceof InternalLanguageElementNode) {
          other = ((InternalLanguageElementNode) node).getGroupCategorySet();
        } else {
          Assert.isTrue(false, "Shouldn't happen"); // $NON-NLS-1$
        }
        fGroupCategories = GroupCategorySet.union(fGroupCategories, other);
      }
    }
    return fGroupCategories;
  }

  protected void internalAddChild(ChildNode child) {
    if (fChildren == null) fChildren = new ArrayList(2);
    fChildren.add(child);
  }

  private List collectTextEditBasedChangeGroups(List categories) {
    List result = new ArrayList(10);
    PreviewNode[] children = getChildren();
    for (int i = 0; i < children.length; i++) {
      PreviewNode child = children[i];
      if (child instanceof TextEditGroupNode) {
        TextEditBasedChangeGroup changeGroup = ((TextEditGroupNode) child).getChangeGroup();
        if (categories == null || changeGroup.getGroupCategorySet().containsOneCategory(categories))
          result.add(changeGroup);
      } else if (child instanceof InternalLanguageElementNode) {
        result.addAll(
            ((InternalLanguageElementNode) child).collectTextEditBasedChangeGroups(categories));
      }
    }
    return result;
  }

  /**
   * Returns the text region the of this language element node.
   *
   * @return the text region of this language element node
   * @throws CoreException if the source region can't be obtained
   */
  public abstract IRegion getTextRange() throws CoreException;
}
