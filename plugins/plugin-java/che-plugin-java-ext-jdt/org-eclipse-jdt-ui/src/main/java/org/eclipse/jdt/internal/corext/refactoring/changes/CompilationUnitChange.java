/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.changes;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.TextEdit;

/**
 * A {@link TextFileChange} that operates on an {@link ICompilationUnit}.
 *
 * <p>DO NOT REMOVE, used in a product.
 *
 * @deprecated As of 3.5, replaced by {@link org.eclipse.jdt.core.refactoring.CompilationUnitChange}
 */
public class CompilationUnitChange extends org.eclipse.jdt.core.refactoring.CompilationUnitChange {

  /**
   * Creates a new <code>CompilationUnitChange</code>.
   *
   * @param name the change's name, mainly used to render the change in the UI
   * @param cunit the compilation unit this change works on
   */
  public CompilationUnitChange(String name, ICompilationUnit cunit) {
    super(name, cunit);
  }

  /**
   * @param change the change
   * @since 3.6
   */
  public CompilationUnitChange(org.eclipse.jdt.core.refactoring.CompilationUnitChange change) {
    super(change.getName(), change.getCompilationUnit());
    setDescriptor(change.getDescriptor());
    TextEdit edit = change.getEdit();
    if (edit != null) {
      setEdit(edit);
    }
    setEnabledShallow(change.isEnabled());
    setKeepPreviewEdits(change.getKeepPreviewEdits());
    setSaveMode(change.getSaveMode());
    setTextType(change.getTextType());
    TextEditBasedChangeGroup[] groups = change.getChangeGroups();
    for (int i = 0; i < groups.length; i++) addChangeGroup(groups[i]);
  }
}
