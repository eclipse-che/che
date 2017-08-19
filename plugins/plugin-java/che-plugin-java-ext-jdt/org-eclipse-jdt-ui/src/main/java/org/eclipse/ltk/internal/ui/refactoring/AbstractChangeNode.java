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

import java.util.List;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;

public abstract class AbstractChangeNode extends PreviewNode {

  private final Change fChange;
  private PreviewNode[] fChildren;

  public static PreviewNode createNode(
      PreviewNode parent, RefactoringPreviewChangeFilter filter, Change change) {
    if (change instanceof CompositeChange) {
      return new CompositeChangeNode(parent, filter, (CompositeChange) change);
    } else if (change instanceof TextEditBasedChange) {
      InternalTextEditChangeNode result =
          (TextEditChangeNode) change.getAdapter(TextEditChangeNode.class);
      if (result == null) {
        result = new TextEditChangeNode((TextEditBasedChange) change);
      }
      result.initialize(parent);
      return result;
    }
    return new DefaultChangeNode(parent, change);
  }

  public static PreviewNode createNode(PreviewNode parent, Change change) {
    return createNode(parent, null, change);
  }

  /**
   * Creates a new <code>AbstractChangeNode</code> for the given change.
   *
   * @param parent the change nodes's parent or <code>null
   * 	</code> if the change node doesn't have a parent
   * @param change the actual change. Argument must not be <code>null</code>
   */
  AbstractChangeNode(PreviewNode parent, Change change) {
    super(parent);
    Assert.isNotNull(change);
    fChange = change;
  }

  /**
   * Returns the underlying <code>Change</code> object.
   *
   * @return the underlying change
   */
  Change getChange() {
    return fChange;
  }

  public PreviewNode[] getChildren() {
    if (fChildren == null) {
      fChildren = doCreateChildren();
    }
    return fChildren;
  }

  abstract PreviewNode[] doCreateChildren();

  public String getText() {
    return fChange.getName();
  }

  public ImageDescriptor getImageDescriptor() {
    return RefactoringPluginImages.DESC_OBJS_DEFAULT_CHANGE;
  }

  public ChangePreviewViewerDescriptor getChangePreviewViewerDescriptor() throws CoreException {
    return ChangePreviewViewerDescriptor.get(fChange);
  }

  public ChangePreview feedInput(IChangePreviewViewer viewer, List categories)
      throws CoreException {
    return viewer.setInput(new ChangePreviewViewerInput(fChange));
  }

  public void setEnabled(boolean enabled) {
    fChange.setEnabled(enabled);
  }

  public void setEnabledShallow(boolean enabled) {
    fChange.setEnabledShallow(enabled);
  }

  boolean hasOneGroupCategory(List categories) {
    PreviewNode[] children = getChildren();
    for (int i = 0; i < children.length; i++) {
      if (children[i].hasOneGroupCategory(categories)) return true;
    }
    return false;
  }

  public boolean hasDerived() {
    if (hasDerivedResourceChange(fChange)) return true;
    PreviewNode[] children = getChildren();
    for (int i = 0; i < children.length; i++) {
      if (children[i].hasDerived()) return true;
    }
    return false;
  }

  int getDefaultChangeActive() {
    int result = fChange.isEnabled() ? ACTIVE : INACTIVE;
    if (fChildren != null) {
      for (int i = 0; i < fChildren.length; i++) {
        result = ACTIVATION_TABLE[fChildren[i].getActive()][result];
        if (result == PARTLY_ACTIVE) break;
      }
    }
    return result;
  }

  int getCompositeChangeActive() {
    if (fChildren != null && fChildren.length > 0) {
      int result = fChildren[0].getActive();
      for (int i = 1; i < fChildren.length; i++) {
        result = ACTIVATION_TABLE[fChildren[i].getActive()][result];
        if (result == PARTLY_ACTIVE) break;
      }
      return result;
    } else {
      return fChange.isEnabled() ? ACTIVE : INACTIVE;
    }
  }

  /**
   * Returns <code>true</code> iff the change node contains a derived resource.
   *
   * @param change the change
   * @return whether the change contains a derived resource
   */
  static boolean hasDerivedResourceChange(Change change) {
    Object modifiedElement = change.getModifiedElement();
    if (modifiedElement instanceof IResource) {
      return ((IResource) modifiedElement).isDerived(IResource.CHECK_ANCESTORS);
    } else if (modifiedElement instanceof IAdaptable) {
      IAdaptable adaptable = (IAdaptable) modifiedElement;
      IResource resource = (IResource) adaptable.getAdapter(IResource.class);
      if (resource != null) {
        return resource.isDerived(IResource.CHECK_ANCESTORS);
      }
    }
    return false;
  }
}
