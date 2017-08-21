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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.reorg.INewNameQuery;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.Change;

public class CopyPackageChange extends PackageReorgChange {

  public CopyPackageChange(
      IPackageFragment pack, IPackageFragmentRoot dest, INewNameQuery nameQuery) {
    super(pack, dest, nameQuery);
  }

  @Override
  protected Change doPerformReorg(IProgressMonitor pm)
      throws JavaModelException, OperationCanceledException {
    getPackage().copy(getDestination(), null, getNewName(), true, pm);
    return null;
  }

  @Override
  public String getName() {
    String packageName =
        JavaElementLabels.getElementLabel(getPackage(), JavaElementLabels.ALL_DEFAULT);
    String destinationName =
        JavaElementLabels.getElementLabel(getDestination(), JavaElementLabels.ALL_DEFAULT);
    return Messages.format(
        RefactoringCoreMessages.CopyPackageChange_copy,
        new String[] {packageName, destinationName});
  }
}
