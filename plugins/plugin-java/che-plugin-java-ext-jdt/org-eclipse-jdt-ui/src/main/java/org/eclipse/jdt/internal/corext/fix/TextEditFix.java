/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.ltk.core.refactoring.CategorizedTextEditGroup;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.text.edits.TextEdit;

public class TextEditFix implements ICleanUpFix {

  private final TextEdit fEdit;
  private final ICompilationUnit fUnit;
  private final String fChangeDescription;

  public TextEditFix(TextEdit edit, ICompilationUnit unit, String changeDescription) {
    fEdit = edit;
    fUnit = unit;
    fChangeDescription = changeDescription;
  }

  /** {@inheritDoc} */
  public CompilationUnitChange createChange(IProgressMonitor progressMonitor) throws CoreException {
    String label = fChangeDescription;
    CompilationUnitChange result = new CompilationUnitChange(label, fUnit);
    result.setEdit(fEdit);
    result.addTextEditGroup(
        new CategorizedTextEditGroup(
            label, new GroupCategorySet(new GroupCategory(label, label, label))));
    return result;
  }
}
