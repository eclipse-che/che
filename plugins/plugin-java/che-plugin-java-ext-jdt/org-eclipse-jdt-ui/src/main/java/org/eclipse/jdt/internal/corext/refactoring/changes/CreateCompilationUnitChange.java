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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.nls.changes.CreateTextFileChange;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;

public final class CreateCompilationUnitChange extends CreateTextFileChange {

  private final ICompilationUnit fUnit;

  public CreateCompilationUnitChange(ICompilationUnit unit, String source, String encoding) {
    super(unit.getResource().getFullPath(), source, encoding, "java"); // $NON-NLS-1$
    fUnit = unit;
  }

  @Override
  public String getName() {
    String cuName = BasicElementLabels.getFileName(fUnit);
    String cuContainerName = BasicElementLabels.getPathLabel(fUnit.getParent().getPath(), false);
    return Messages.format(
        RefactoringCoreMessages.CompilationUnitChange_label,
        new String[] {cuName, cuContainerName});
  }
}
