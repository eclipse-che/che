/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.changes;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.corext.refactoring.AbstractJavaElementRenameChange;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public final class RenameSourceFolderChange extends AbstractJavaElementRenameChange {

  private static RefactoringStatus checkIfModifiable(IPackageFragmentRoot root)
      throws CoreException {
    RefactoringStatus result = new RefactoringStatus();
    if (root == null) {
      result.addFatalError(RefactoringCoreMessages.DynamicValidationStateChange_workspace_changed);
      return result;
    }
    if (!root.exists()) {
      result.addFatalError(
          Messages.format(RefactoringCoreMessages.Change_does_not_exist, getRootLabel(root)));
      return result;
    }

    if (result.hasFatalError()) return result;

    if (root.isArchive()) {
      result.addFatalError(
          Messages.format(
              RefactoringCoreMessages.RenameSourceFolderChange_rename_archive, getRootLabel(root)));
      return result;
    }

    if (root.isExternal()) {
      result.addFatalError(
          Messages.format(
              RefactoringCoreMessages.RenameSourceFolderChange_rename_external,
              getRootLabel(root)));
      return result;
    }

    IResource correspondingResource = root.getCorrespondingResource();
    if (correspondingResource == null || !correspondingResource.exists()) {
      result.addFatalError(
          Messages.format(
              RefactoringCoreMessages
                  .RenameSourceFolderChange_error_underlying_resource_not_existing,
              getRootLabel(root)));
      return result;
    }

    if (correspondingResource.isLinked()) {
      result.addFatalError(
          Messages.format(
              RefactoringCoreMessages.RenameSourceFolderChange_rename_linked, getRootLabel(root)));
      return result;
    }

    return result;
  }

  private static String getRootLabel(IPackageFragmentRoot root) {
    return JavaElementLabels.getElementLabel(root, JavaElementLabels.ALL_DEFAULT);
  }

  public RenameSourceFolderChange(IPackageFragmentRoot sourceFolder, String newName) {
    this(sourceFolder.getPath(), sourceFolder.getElementName(), newName, IResource.NULL_STAMP);
    Assert.isTrue(!sourceFolder.isReadOnly(), "should not be read only"); // $NON-NLS-1$
    Assert.isTrue(!sourceFolder.isArchive(), "should not be an archive"); // $NON-NLS-1$
    Assert.isTrue(!sourceFolder.isExternal(), "should not be an external folder"); // $NON-NLS-1$
    setValidationMethod(VALIDATE_NOT_DIRTY);
  }

  private RenameSourceFolderChange(
      IPath resourcePath, String oldName, String newName, long stampToRestore) {
    super(resourcePath, oldName, newName, stampToRestore);
  }

  @Override
  protected IPath createNewPath() {
    return getResourcePath().removeLastSegments(1).append(getNewName());
  }

  @Override
  protected Change createUndoChange(long stampToRestore) {
    return new RenameSourceFolderChange(
        createNewPath(), getNewName(), getOldName(), stampToRestore);
  }

  @Override
  protected void doRename(IProgressMonitor pm) throws CoreException {
    IPackageFragmentRoot sourceFolder = getSourceFolder();
    if (sourceFolder != null)
      sourceFolder.move(getNewPath(), getCoreMoveFlags(), getJavaModelUpdateFlags(), null, pm);
  }

  private int getCoreMoveFlags() {
    if (getResource().isLinked()) return IResource.SHALLOW;
    else return IResource.NONE;
  }

  private int getJavaModelUpdateFlags() {
    return IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH
        | IPackageFragmentRoot.ORIGINATING_PROJECT_CLASSPATH
        | IPackageFragmentRoot.OTHER_REFERRING_PROJECTS_CLASSPATH
        | IPackageFragmentRoot.REPLACE;
  }

  @Override
  public String getName() {
    String[] keys = {
      BasicElementLabels.getJavaElementName(getOldName()),
      BasicElementLabels.getJavaElementName(getNewName())
    };
    return Messages.format(RefactoringCoreMessages.RenameSourceFolderChange_rename, keys);
  }

  private IPath getNewPath() {
    return getResource().getFullPath().removeLastSegments(1).append(getNewName());
  }

  private IPackageFragmentRoot getSourceFolder() {
    return (IPackageFragmentRoot) getModifiedElement();
  }

  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
    RefactoringStatus result = super.isValid(pm);
    if (result.hasFatalError()) return result;

    IPackageFragmentRoot sourceFolder = getSourceFolder();
    result.merge(checkIfModifiable(sourceFolder));

    return result;
  }
}
