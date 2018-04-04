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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.AbstractJavaElementRenameChange;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public final class RenamePackageChange extends AbstractJavaElementRenameChange {

  private static IPath createPath(String packageName) {
    return new Path(packageName.replace('.', IPath.SEPARATOR));
  }

  private Map<IResource, Long> fCompilationUnitStamps;

  private final boolean fRenameSubpackages;

  public RenamePackageChange(IPackageFragment pack, String newName, boolean renameSubpackages) {
    this(
        pack.getPath(),
        pack.getElementName(),
        newName,
        IResource.NULL_STAMP,
        null,
        renameSubpackages);
    Assert.isTrue(!pack.isReadOnly(), "package must not be read only"); // $NON-NLS-1$
  }

  private RenamePackageChange(
      IPath resourcePath,
      String oldName,
      String newName,
      long stampToRestore,
      Map<IResource, Long> compilationUnitStamps,
      boolean renameSubpackages) {
    super(resourcePath, oldName, newName, stampToRestore);
    fCompilationUnitStamps = compilationUnitStamps;
    fRenameSubpackages = renameSubpackages;

    setValidationMethod(VALIDATE_NOT_DIRTY);
  }

  private void addStamps(Map<IResource, Long> stamps, ICompilationUnit[] units) {
    for (int i = 0; i < units.length; i++) {
      IResource resource = units[i].getResource();
      long stamp = IResource.NULL_STAMP;
      if (resource != null && (stamp = resource.getModificationStamp()) != IResource.NULL_STAMP) {
        stamps.put(resource, new Long(stamp));
      }
    }
  }

  public Map<IResource, Long> getFCompilationUnitStamps() {
    return fCompilationUnitStamps;
  }

  @Override
  protected IPath createNewPath() {
    IPackageFragment oldPackage = getPackage();
    IPath oldPackageName = createPath(oldPackage.getElementName());
    IPath newPackageName = createPath(getNewName());
    return getResourcePath()
        .removeLastSegments(oldPackageName.segmentCount())
        .append(newPackageName);
  }

  protected IPath createNewPath(IPackageFragment oldPackage) {
    IPath oldPackagePath = createPath(oldPackage.getElementName());
    IPath newPackagePath = createPath(getNewName(oldPackage));
    return oldPackage
        .getPath()
        .removeLastSegments(oldPackagePath.segmentCount())
        .append(newPackagePath);
  }

  @Override
  protected Change createUndoChange(long stampToRestore) throws CoreException {
    IPackageFragment pack = getPackage();
    if (pack == null) return new NullChange();
    Map<IResource, Long> stamps = new HashMap<IResource, Long>();
    if (!fRenameSubpackages) {
      addStamps(stamps, pack.getCompilationUnits());
    } else {
      IPackageFragment[] allPackages = JavaElementUtil.getPackageAndSubpackages(pack);
      for (int i = 0; i < allPackages.length; i++) {
        IPackageFragment currentPackage = allPackages[i];
        addStamps(stamps, currentPackage.getCompilationUnits());
      }
    }
    return new RenamePackageChange(
        createNewPath(), getNewName(), getOldName(), stampToRestore, stamps, fRenameSubpackages);
    // Note: This reverse change only works if the renamePackage change did
    // not merge the source package into an existing target.
  }

  @Override
  protected void doRename(IProgressMonitor pm) throws CoreException {
    IPackageFragment pack = getPackage();
    if (pack == null) return;

    if (!fRenameSubpackages) {
      renamePackage(pack, pm, createNewPath(), getNewName());

    } else {
      IPackageFragment[] allPackages = JavaElementUtil.getPackageAndSubpackages(pack);
      Arrays.sort(
          allPackages,
          new Comparator<IPackageFragment>() {
            public int compare(IPackageFragment o1, IPackageFragment o2) {
              String p1 = o1.getElementName();
              String p2 = o2.getElementName();
              return p1.compareTo(p2);
            }
          });
      int count = allPackages.length;
      pm.beginTask("", count); // $NON-NLS-1$
      // When renaming to subpackage (a -> a.b), do it inside-out:
      boolean insideOut = getNewName().startsWith(getOldName());
      try {
        for (int i = 0; i < count; i++) {
          IPackageFragment currentPackage = allPackages[insideOut ? count - i - 1 : i];
          renamePackage(
              currentPackage,
              new SubProgressMonitor(pm, 1),
              createNewPath(currentPackage),
              getNewName(currentPackage));
        }
      } finally {
        pm.done();
      }
    }
  }

  @Override
  public String getName() {
    String msg =
        fRenameSubpackages
            ? RefactoringCoreMessages.RenamePackageChange_name_with_subpackages
            : RefactoringCoreMessages.RenamePackageChange_name;
    String[] keys = {
      BasicElementLabels.getJavaElementName(getOldName()),
      BasicElementLabels.getJavaElementName(getNewName())
    };
    return Messages.format(msg, keys);
  }

  private String getNewName(IPackageFragment subpackage) {
    return getNewName() + subpackage.getElementName().substring(getOldName().length());
  }

  private IPackageFragment getPackage() {
    return (IPackageFragment) getModifiedElement();
  }

  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
    pm.beginTask("", 2); // $NON-NLS-1$
    RefactoringStatus result;
    try {
      result = new RefactoringStatus();
      IJavaElement element = (IJavaElement) getModifiedElement();
      // don't check for read-only since we don't go through
      // validate edit.
      result.merge(super.isValid(new SubProgressMonitor(pm, 1)));
      if (result.hasFatalError()) return result;
      if (element != null && element.exists() && element instanceof IPackageFragment) {
        IPackageFragment pack = (IPackageFragment) element;
        if (fRenameSubpackages) {
          IPackageFragment[] allPackages = JavaElementUtil.getPackageAndSubpackages(pack);
          SubProgressMonitor subPm = new SubProgressMonitor(pm, 1);
          subPm.beginTask("", allPackages.length); // $NON-NLS-1$
          for (int i = 0; i < allPackages.length; i++) {
            // don't check for read-only since we don't go through
            // validate edit.
            checkIfModifiable(result, allPackages[i].getResource(), VALIDATE_NOT_DIRTY);
            if (result.hasFatalError()) return result;
            isValid(result, allPackages[i], new SubProgressMonitor(subPm, 1));
          }
        } else {
          isValid(result, pack, new SubProgressMonitor(pm, 1));
        }
      }
    } finally {
      pm.done();
    }
    return result;
  }

  private void isValid(RefactoringStatus result, IPackageFragment pack, IProgressMonitor pm)
      throws JavaModelException {
    ICompilationUnit[] units = pack.getCompilationUnits();
    pm.beginTask("", units.length); // $NON-NLS-1$
    for (int i = 0; i < units.length; i++) {
      pm.subTask(
          Messages.format(
              RefactoringCoreMessages.RenamePackageChange_checking_change,
              JavaElementLabels.getElementLabel(pack, JavaElementLabels.ALL_DEFAULT)));
      checkIfModifiable(
          result, units[i].getResource(), VALIDATE_NOT_READ_ONLY | VALIDATE_NOT_DIRTY);
      pm.worked(1);
    }
    pm.done();
  }

  private void renamePackage(
      IPackageFragment pack, IProgressMonitor pm, IPath newPath, String newName)
      throws JavaModelException, CoreException {
    if (!pack.exists()) return; // happens if empty parent with single subpackage is renamed, see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=199045
    pack.rename(newName, false, pm);
    if (fCompilationUnitStamps != null) {
      IPackageFragment newPack =
          (IPackageFragment)
              JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getFolder(newPath));
      if (newPack.exists()) {
        ICompilationUnit[] units = newPack.getCompilationUnits();
        for (int i = 0; i < units.length; i++) {
          IResource resource = units[i].getResource();
          if (resource != null) {
            Long stamp = fCompilationUnitStamps.get(resource);
            if (stamp != null) {
              resource.revertModificationStamp(stamp.longValue());
            }
          }
        }
      }
    }
  }
}
