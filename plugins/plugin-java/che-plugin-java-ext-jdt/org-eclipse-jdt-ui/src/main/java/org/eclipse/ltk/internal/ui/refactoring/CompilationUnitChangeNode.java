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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.MultiStateTextFileChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.ui.refactoring.LanguageElementNode;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.text.edits.TextEdit;

public class CompilationUnitChangeNode extends TextEditChangeNode {

  static final ChildNode[] EMPTY_CHILDREN = new ChildNode[0];

  private static class JavaLanguageNode extends LanguageElementNode {

    private IJavaElement fJavaElement;
    private static JavaElementImageProvider fgImageProvider = new JavaElementImageProvider();

    public JavaLanguageNode(TextEditChangeNode parent, IJavaElement element) {
      super(parent);
      fJavaElement = element;
      Assert.isNotNull(fJavaElement);
    }

    public JavaLanguageNode(ChildNode parent, IJavaElement element) {
      super(parent);
      fJavaElement = element;
      Assert.isNotNull(fJavaElement);
    }

    @Override
    public String getText() {
      return JavaElementLabels.getElementLabel(fJavaElement, JavaElementLabels.ALL_DEFAULT);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
      return fgImageProvider.getJavaImageDescriptor(
          fJavaElement,
          JavaElementImageProvider.OVERLAY_ICONS | JavaElementImageProvider.SMALL_ICONS);
    }

    @Override
    public IRegion getTextRange() throws CoreException {
      ISourceRange range = ((ISourceReference) fJavaElement).getSourceRange();
      return new Region(range.getOffset(), range.getLength());
    }
  }

  public CompilationUnitChangeNode(TextEditBasedChange change) {
    super(change);
  }

  @Override
  protected ChildNode[] createChildNodes() {
    TextEditBasedChange change = getTextEditBasedChange();
    if (change instanceof MultiStateTextFileChange) {
      return new ChildNode
          [0]; // no edit preview & edit disabling possible in the MultiStateTextFileChange (edits
      // must be applied
      // in sequence)
    }

    ICompilationUnit cunit = (ICompilationUnit) change.getAdapter(ICompilationUnit.class);
    if (cunit != null) {
      List<ChildNode> children = new ArrayList<ChildNode>(5);
      Map<IJavaElement, JavaLanguageNode> map = new HashMap<IJavaElement, JavaLanguageNode>(20);
      TextEditBasedChangeGroup[] changes = getSortedChangeGroups(change);
      for (int i = 0; i < changes.length; i++) {
        TextEditBasedChangeGroup tec = changes[i];
        try {
          IJavaElement element = getModifiedJavaElement(tec, cunit);
          if (element.equals(cunit)) {
            children.add(createTextEditGroupNode(this, tec));
          } else {
            JavaLanguageNode pjce = getChangeElement(map, element, children, this);
            pjce.addChild(createTextEditGroupNode(pjce, tec));
          }
        } catch (JavaModelException e) {
          children.add(createTextEditGroupNode(this, tec));
        }
      }
      return children.toArray(new ChildNode[children.size()]);
    } else {
      return EMPTY_CHILDREN;
    }
  }

  private static class OffsetComparator implements Comparator<TextEditBasedChangeGroup> {
    public int compare(TextEditBasedChangeGroup c1, TextEditBasedChangeGroup c2) {
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
    TextEditBasedChangeGroup[] edits = change.getChangeGroups();
    List<TextEditBasedChangeGroup> result = new ArrayList<TextEditBasedChangeGroup>(edits.length);
    for (int i = 0; i < edits.length; i++) {
      if (!edits[i].getTextEditGroup().isEmpty()) result.add(edits[i]);
    }
    Comparator<TextEditBasedChangeGroup> comparator = new OffsetComparator();
    Collections.sort(result, comparator);
    return result.toArray(new TextEditBasedChangeGroup[result.size()]);
  }

  private IJavaElement getModifiedJavaElement(TextEditBasedChangeGroup edit, ICompilationUnit cunit)
      throws JavaModelException {
    IRegion range = edit.getRegion();
    if (range.getOffset() == 0 && range.getLength() == 0) return cunit;
    IJavaElement result = cunit.getElementAt(range.getOffset());
    if (result == null) return cunit;

    try {
      while (true) {
        ISourceReference ref = (ISourceReference) result;
        IRegion sRange =
            new Region(ref.getSourceRange().getOffset(), ref.getSourceRange().getLength());
        if (result.getElementType() == IJavaElement.COMPILATION_UNIT
            || result.getParent() == null
            || coveredBy(edit, sRange)) break;
        result = result.getParent();
      }
    } catch (JavaModelException e) {
      // Do nothing, use old value.
    } catch (ClassCastException e) {
      // Do nothing, use old value.
    }
    return result;
  }

  private JavaLanguageNode getChangeElement(
      Map<IJavaElement, JavaLanguageNode> map,
      IJavaElement element,
      List<ChildNode> children,
      TextEditChangeNode cunitChange) {
    JavaLanguageNode result = map.get(element);
    if (result != null) return result;
    IJavaElement parent = element.getParent();
    if (parent instanceof ICompilationUnit) {
      result = new JavaLanguageNode(cunitChange, element);
      children.add(result);
      map.put(element, result);
    } else {
      JavaLanguageNode parentChange = getChangeElement(map, parent, children, cunitChange);
      result = new JavaLanguageNode(parentChange, element);
      parentChange.addChild(result);
      map.put(element, result);
    }
    return result;
  }

  private boolean coveredBy(TextEditBasedChangeGroup group, IRegion sourceRegion) {
    int sLength = sourceRegion.getLength();
    if (sLength == 0) return false;
    int sOffset = sourceRegion.getOffset();
    int sEnd = sOffset + sLength - 1;
    TextEdit[] edits = group.getTextEdits();
    for (int i = 0; i < edits.length; i++) {
      TextEdit edit = edits[i];
      if (edit.isDeleted()) return false;
      int rOffset = edit.getOffset();
      int rLength = edit.getLength();
      int rEnd = rOffset + rLength - 1;
      if (rLength == 0) {
        if (!(sOffset < rOffset && rOffset <= sEnd)) return false;
      } else {
        if (!(sOffset <= rOffset && rEnd <= sEnd)) return false;
      }
    }
    return true;
  }
}
