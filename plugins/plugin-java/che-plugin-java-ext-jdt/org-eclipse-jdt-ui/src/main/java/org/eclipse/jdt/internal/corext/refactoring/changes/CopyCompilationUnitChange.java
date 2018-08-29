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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.reorg.INewNameQuery;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.ltk.core.refactoring.Change;

public class CopyCompilationUnitChange extends CompilationUnitReorgChange {

  public CopyCompilationUnitChange(
      ICompilationUnit cu, IPackageFragment dest, INewNameQuery newNameQuery) {
    super(cu, dest, newNameQuery);

    // Copy compilation unit change isn't undoable and isn't used
    // as a redo/undo change right now.
    setValidationMethod(SAVE_IF_DIRTY);
  }

  @Override
  Change doPerformReorg(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    getCu().copy(getDestinationPackage(), null, getNewName(), true, pm);
    return null;
  }

  @Override
  public String getName() {
    return Messages.format(
        RefactoringCoreMessages.CopyCompilationUnitChange_copy,
        new String[] {
          BasicElementLabels.getFileName(getCu()), getPackageName(getDestinationPackage())
        });
  }
}
