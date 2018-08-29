/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.refactoring.actions;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;

/** Helper class for refactoring actions */
public class RefactoringActions {

  /**
   * Converts the given selection into a type using the following rules:
   *
   * <ul>
   *   <li>if the selection is enclosed by a type than that type is returned.
   *   <li>if the selection is inside a compilation unit or class file than the primary type is
   *       returned.
   *   <li>otherwise <code>null</code> is returned.
   * </ul>
   */
  public static IType getEnclosingOrPrimaryType(JavaTextSelection selection)
      throws JavaModelException {
    final IJavaElement element = selection.resolveEnclosingElement();
    if (element != null) return convertToEnclosingOrPrimaryType(element);
    return null;
  }
  //	public static IType getEnclosingOrPrimaryType(JavaEditor editor) throws JavaModelException {
  //		return convertToEnclosingOrPrimaryType(SelectionConverter.resolveEnclosingElement(
  //				editor, (ITextSelection)editor.getSelectionProvider().getSelection()));
  //	}

  private static IType convertToEnclosingOrPrimaryType(IJavaElement element)
      throws JavaModelException {
    if (element instanceof IType) return (IType) element;
    IType result = (IType) element.getAncestor(IJavaElement.TYPE);
    if (result != null) return result;
    if (element instanceof ICompilationUnit) return ((ICompilationUnit) element).findPrimaryType();
    if (element instanceof IClassFile) return ((IClassFile) element).getType();
    return null;
  }

  /**
   * Converts the given selection into a type using the following rules:
   *
   * <ul>
   *   <li>if the selection is enclosed by a type than that type is returned.
   *   <li>otherwise <code>null</code> is returned.
   * </ul>
   */
  public static IType getEnclosingType(JavaTextSelection selection) throws JavaModelException {
    return convertToEnclosingType(selection.resolveEnclosingElement());
  }
  //	public static IType getEnclosingType(JavaEditor editor) throws JavaModelException {
  //		return convertToEnclosingType(SelectionConverter.resolveEnclosingElement(
  //				editor, (ITextSelection)editor.getSelectionProvider().getSelection()));
  //	}

  private static IType convertToEnclosingType(IJavaElement element) {
    if (element == null) return null;
    if (!(element instanceof IType)) element = element.getAncestor(IJavaElement.TYPE);
    return (IType) element;
  }
}
