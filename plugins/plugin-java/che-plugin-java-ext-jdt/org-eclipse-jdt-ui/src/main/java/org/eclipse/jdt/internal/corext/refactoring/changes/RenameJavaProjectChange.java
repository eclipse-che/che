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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.AbstractJavaElementRenameChange;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.ltk.core.refactoring.Change;

public final class RenameJavaProjectChange extends AbstractJavaElementRenameChange {

  private boolean fUpdateReferences;

  public RenameJavaProjectChange(IJavaProject project, String newName, boolean updateReferences) {
    this(
        project.getPath(),
        project.getElementName(),
        newName,
        IResource.NULL_STAMP,
        updateReferences);
    Assert.isTrue(!project.isReadOnly(), "should not be read only"); // $NON-NLS-1$
  }

  private RenameJavaProjectChange(
      IPath resourcePath,
      String oldName,
      String newName,
      long stampToRestore,
      boolean updateReferences) {
    super(resourcePath, oldName, newName, stampToRestore);
    fUpdateReferences = updateReferences;

    setValidationMethod(VALIDATE_NOT_DIRTY);
  }

  private IClasspathEntry createModifiedEntry(IClasspathEntry oldEntry) {
    return JavaCore.newProjectEntry(
        createNewPath(),
        oldEntry.getAccessRules(),
        oldEntry.combineAccessRules(),
        oldEntry.getExtraAttributes(),
        oldEntry.isExported());
  }

  @Override
  protected IPath createNewPath() {
    return getResourcePath().removeLastSegments(1).append(getNewName());
  }

  @Override
  protected Change createUndoChange(long stampToRestore) throws JavaModelException {
    return new RenameJavaProjectChange(
        createNewPath(), getNewName(), getOldName(), stampToRestore, fUpdateReferences);
  }

  @Override
  protected void doRename(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask(getName(), 2);
      if (fUpdateReferences) modifyClassPaths(new SubProgressMonitor(pm, 1));
      IProject project = getProject();
      if (project != null) {
        IProjectDescription description = project.getDescription();
        description.setName(getNewName());
        project.move(
            description, IResource.FORCE | IResource.SHALLOW, new SubProgressMonitor(pm, 1));
      }
    } finally {
      pm.done();
    }
  }

  private IJavaProject getJavaProject() {
    return (IJavaProject) getModifiedElement();
  }

  @Override
  public String getName() {
    String[] keys =
        new String[] {
          BasicElementLabels.getJavaElementName(getOldName()),
          BasicElementLabels.getJavaElementName(getNewName())
        };
    return Messages.format(RefactoringCoreMessages.RenameJavaProjectChange_rename, keys);
  }

  private IProject getProject() {
    IJavaProject jp = getJavaProject();
    if (jp == null) return null;
    return jp.getProject();
  }

  private boolean isOurEntry(IClasspathEntry cpe) {
    if (cpe.getEntryKind() != IClasspathEntry.CPE_PROJECT) return false;
    if (!cpe.getPath().equals(getResourcePath())) return false;
    return true;
  }

  private void modifyClassPath(IJavaProject referencingProject, IProgressMonitor pm)
      throws JavaModelException {
    pm.beginTask("", 1); // $NON-NLS-1$
    IClasspathEntry[] oldEntries = referencingProject.getRawClasspath();
    IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length];
    for (int i = 0; i < newEntries.length; i++) {
      if (isOurEntry(oldEntries[i])) newEntries[i] = createModifiedEntry(oldEntries[i]);
      else newEntries[i] = oldEntries[i];
    }
    referencingProject.setRawClasspath(newEntries, pm);
    pm.done();
  }

  private void modifyClassPaths(IProgressMonitor pm) throws JavaModelException {
    IProject[] referencing = getProject().getReferencingProjects();
    pm.beginTask(RefactoringCoreMessages.RenameJavaProjectChange_update, referencing.length);
    for (int i = 0; i < referencing.length; i++) {
      IJavaProject jp = JavaCore.create(referencing[i]);
      if (jp != null && jp.exists()) {
        modifyClassPath(jp, new SubProgressMonitor(pm, 1));
      } else {
        pm.worked(1);
      }
    }
    pm.done();
  }
}
