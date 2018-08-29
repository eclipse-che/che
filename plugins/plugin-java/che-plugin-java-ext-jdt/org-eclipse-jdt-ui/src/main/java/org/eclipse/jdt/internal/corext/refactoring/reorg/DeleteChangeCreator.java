/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceManipulation;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.changes.ClasspathChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.DeletePackageFragmentRootChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.DeleteSourceManipulationChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationStateChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility;
import org.eclipse.jdt.internal.corext.refactoring.changes.UndoablePackageDeleteChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringFileBuffers;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.text.edits.TextEdit;

class DeleteChangeCreator {
  private DeleteChangeCreator() {
    // private
  }

  /**
   * @param manager the text change manager
   * @param resources the resources to delete
   * @param javaElements the Java elements to delete
   * @param changeName the name of the change
   * @param packageDeletes a list of {@link IResource}s that will be deleted by the delete operation
   *     of the {@link IPackageFragment}s in <code>javaElements</code>, or <code>null</code> iff
   *     <code>javaElements</code> does not contain package fragments
   * @return the created change
   * @throws CoreException
   */
  static Change createDeleteChange(
      TextChangeManager manager,
      IResource[] resources,
      IJavaElement[] javaElements,
      String changeName,
      List<IResource> packageDeletes)
      throws CoreException {
    /*
     * Problem: deleting a package and subpackages can result in
     * multiple package fragments in fJavaElements but only
     * one folder in packageDeletes. The way to handle this is to make the undo
     * change of individual package delete changes an empty change, and
     * add take care of the undo in UndoablePackageDeleteChange.
     */
    DynamicValidationStateChange result;
    if (packageDeletes.size() > 0) {
      result = new UndoablePackageDeleteChange(changeName, packageDeletes);
    } else {
      result = new DynamicValidationStateChange(changeName);
    }

    for (int i = 0; i < javaElements.length; i++) {
      IJavaElement element = javaElements[i];
      if (!ReorgUtils.isInsideCompilationUnit(element)) result.add(createDeleteChange(element));
    }

    for (int i = 0; i < resources.length; i++) {
      result.add(createDeleteChange(resources[i]));
    }

    Map<ICompilationUnit, List<IJavaElement>> grouped =
        ReorgUtils.groupByCompilationUnit(getElementsSmallerThanCu(javaElements));
    if (grouped.size() != 0) {
      Assert.isNotNull(manager);
      for (Iterator<ICompilationUnit> iter = grouped.keySet().iterator(); iter.hasNext(); ) {
        ICompilationUnit cu = iter.next();
        result.add(createDeleteChange(cu, grouped.get(cu), manager));
      }
    }

    return result;
  }

  private static Change createDeleteChange(IResource resource) {
    Assert.isTrue(!(resource instanceof IWorkspaceRoot)); // cannot be done
    Assert.isTrue(!(resource instanceof IProject)); // project deletion is handled by the workbench
    return new DeleteResourceChange(resource.getFullPath(), true);
  }

  /*
   * List<IJavaElement> javaElements
   */
  private static Change createDeleteChange(
      ICompilationUnit cu, List<IJavaElement> javaElements, TextChangeManager manager)
      throws CoreException {
    CompilationUnit cuNode = RefactoringASTParser.parseWithASTProvider(cu, false, null);
    CompilationUnitRewrite rewriter = new CompilationUnitRewrite(cu, cuNode);
    IJavaElement[] elements = javaElements.toArray(new IJavaElement[javaElements.size()]);
    ASTNodeDeleteUtil.markAsDeleted(elements, rewriter, null);
    return addTextEditFromRewrite(manager, cu, rewriter.getASTRewrite());
  }

  private static TextChange addTextEditFromRewrite(
      TextChangeManager manager, ICompilationUnit cu, ASTRewrite rewrite) throws CoreException {
    try {
      ITextFileBuffer buffer = RefactoringFileBuffers.acquire(cu);
      TextEdit resultingEdits =
          rewrite.rewriteAST(buffer.getDocument(), cu.getJavaProject().getOptions(true));
      TextChange textChange = manager.get(cu);
      if (textChange instanceof TextFileChange) {
        TextFileChange tfc = (TextFileChange) textChange;
        tfc.setSaveMode(TextFileChange.KEEP_SAVE_STATE);
      }
      String message = RefactoringCoreMessages.DeleteChangeCreator_1;
      TextChangeCompatibility.addTextEdit(textChange, message, resultingEdits);
      return textChange;
    } finally {
      RefactoringFileBuffers.release(cu);
    }
  }

  // List<IJavaElement>
  private static List<IJavaElement> getElementsSmallerThanCu(IJavaElement[] javaElements) {
    List<IJavaElement> result = new ArrayList<IJavaElement>();
    for (int i = 0; i < javaElements.length; i++) {
      IJavaElement element = javaElements[i];
      if (ReorgUtils.isInsideCompilationUnit(element)) result.add(element);
    }
    return result;
  }

  private static Change createDeleteChange(IJavaElement javaElement) throws JavaModelException {
    Assert.isTrue(!ReorgUtils.isInsideCompilationUnit(javaElement));

    switch (javaElement.getElementType()) {
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        return createPackageFragmentRootDeleteChange((IPackageFragmentRoot) javaElement);

      case IJavaElement.PACKAGE_FRAGMENT:
        return createSourceManipulationDeleteChange((IPackageFragment) javaElement);

      case IJavaElement.COMPILATION_UNIT:
        return createSourceManipulationDeleteChange((ICompilationUnit) javaElement);

      case IJavaElement.CLASS_FILE:
        // if this assert fails, it means that a precondition is missing
        Assert.isTrue(((IClassFile) javaElement).getResource() instanceof IFile);
        return createDeleteChange(((IClassFile) javaElement).getResource());

      case IJavaElement.JAVA_MODEL: // cannot be done
        Assert.isTrue(false);
        return null;

      case IJavaElement.JAVA_PROJECT: // handled differently
        Assert.isTrue(false);
        return null;

      case IJavaElement.TYPE:
      case IJavaElement.FIELD:
      case IJavaElement.METHOD:
      case IJavaElement.INITIALIZER:
      case IJavaElement.PACKAGE_DECLARATION:
      case IJavaElement.IMPORT_CONTAINER:
      case IJavaElement.IMPORT_DECLARATION:
        Assert.isTrue(false); // not done here
        return new NullChange();
      default:
        Assert.isTrue(false); // there's no more kinds
        return new NullChange();
    }
  }

  private static Change createSourceManipulationDeleteChange(ISourceManipulation element) {
    // XXX workaround for bug 31384, in case of linked ISourceManipulation delete the resource
    if (element instanceof ICompilationUnit || element instanceof IPackageFragment) {
      IResource resource;
      if (element instanceof ICompilationUnit)
        resource = ReorgUtils.getResource((ICompilationUnit) element);
      else resource = ((IPackageFragment) element).getResource();
      if (resource != null && resource.isLinked()) return createDeleteChange(resource);
    }
    return new DeleteSourceManipulationChange(element, true);
  }

  private static Change createPackageFragmentRootDeleteChange(IPackageFragmentRoot root)
      throws JavaModelException {
    IResource resource = root.getResource();
    if (resource != null && resource.isLinked()) {
      // XXX using this code is a workaround for jcore bug 31998
      // jcore cannot handle linked stuff
      // normally, we should always create DeletePackageFragmentRootChange
      CompositeChange composite =
          new DynamicValidationStateChange(
              RefactoringCoreMessages.DeleteRefactoring_delete_package_fragment_root);

      ClasspathChange change =
          ClasspathChange.removeEntryChange(root.getJavaProject(), root.getRawClasspathEntry());
      if (change != null) {
        composite.add(change);
      }
      Assert.isTrue(!Checks.isClasspathDelete(root)); // checked in preconditions
      composite.add(createDeleteChange(resource));

      return composite;
    } else {
      Assert.isTrue(!root.isExternal());
      // TODO remove the query argument
      return new DeletePackageFragmentRootChange(root, true, null);
    }
  }
}
