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

import java.util.ArrayList;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.ltk.core.refactoring.Change;

public class MoveCompilationUnitChange extends CompilationUnitReorgChange {

  private boolean fUndoable;
  private long fStampToRestore;
  private final IPackageFragment[] fDeletePackages;

  public MoveCompilationUnitChange(ICompilationUnit cu, IPackageFragment newPackage) {
    super(cu, newPackage);
    fStampToRestore = IResource.NULL_STAMP;
    fDeletePackages = null;

    setValidationMethod(SAVE_IF_DIRTY | VALIDATE_NOT_READ_ONLY);
  }

  private MoveCompilationUnitChange(
      IPackageFragment oldPackage,
      String cuName,
      IPackageFragment newPackage,
      long stampToRestore,
      IPackageFragment[] deletePackages) {
    super(
        oldPackage.getHandleIdentifier(),
        newPackage.getHandleIdentifier(),
        oldPackage.getCompilationUnit(cuName).getHandleIdentifier());
    fStampToRestore = stampToRestore;
    fDeletePackages = deletePackages;

    setValidationMethod(SAVE_IF_DIRTY | VALIDATE_NOT_READ_ONLY);
  }

  @Override
  public String getName() {
    return Messages.format(
        RefactoringCoreMessages.MoveCompilationUnitChange_name,
        new String[] {
          BasicElementLabels.getFileName(getCu()), getPackageName(getDestinationPackage())
        });
  }

  @Override
  Change doPerformReorg(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    String name;
    String newName = getNewName();
    if (newName == null) name = getCu().getElementName();
    else name = newName;

    // get current modification stamp
    long currentStamp = IResource.NULL_STAMP;
    IResource resource = getCu().getResource();
    if (resource != null) {
      currentStamp = resource.getModificationStamp();
    }

    IPackageFragment destination = getDestinationPackage();
    fUndoable = !destination.exists() || !destination.getCompilationUnit(name).exists();

    IPackageFragment[] createdPackages = null;
    if (!destination.exists()) {
      IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) destination.getParent();
      createdPackages = createDestination(packageFragmentRoot, destination, pm);
    }

    // perform the move and restore modification stamp
    getCu().move(destination, null, newName, true, pm);
    if (fStampToRestore != IResource.NULL_STAMP) {
      ICompilationUnit moved = destination.getCompilationUnit(name);
      IResource movedResource = moved.getResource();
      if (movedResource != null) {
        movedResource.revertModificationStamp(fStampToRestore);
      }
    }

    if (fDeletePackages != null) {
      for (int i = fDeletePackages.length - 1; i >= 0; i--) {
        fDeletePackages[i].delete(true, pm);
      }
    }

    if (fUndoable) {
      return new MoveCompilationUnitChange(
          destination, getCu().getElementName(), getOldPackage(), currentStamp, createdPackages);
    } else {
      return null;
    }
  }

  private IPackageFragment[] createDestination(
      IPackageFragmentRoot root, IPackageFragment destination, IProgressMonitor pm)
      throws JavaModelException {
    String packageName = destination.getElementName();
    String[] split = packageName.split("\\."); // $NON-NLS-1$

    ArrayList<IPackageFragment> created = new ArrayList<IPackageFragment>();

    StringBuffer name = new StringBuffer();
    name.append(split[0]);
    for (int i = 0; i < split.length; i++) {
      IPackageFragment fragment = root.getPackageFragment(name.toString());
      if (!fragment.exists()) {
        created.add(fragment);
      }

      if (fragment.equals(destination)) {
        root.createPackageFragment(name.toString(), true, pm);
        return created.toArray(new IPackageFragment[created.size()]);
      }

      name.append("."); // $NON-NLS-1$
      name.append(split[i + 1]);
    }

    return null;
  }
}
