/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel;

/**
 * Answer for {@link CreateRenameRefactoring}
 *
 * @author Evgen Vidolob
 * @author Vleriy Svydenko
 */
@DTO
public interface RenameRefactoringSession extends RefactoringSession {

  /**
   * Rename refactoring wizard type
   *
   * @return the wizard type
   */
  RenameWizard getWizardType();

  void setWizardType(RenameWizard type);

  /** @return name of the renaming element */
  String getOldName();

  void setOldName(String name);

  /**
   * Linked edit model, not null if refactoring performed from editor and wizard is not necessary.
   */
  LinkedModeModel getLinkedModeModel();

  void setLinkedModeModel(LinkedModeModel model);

  enum RenameWizard {
    PACKAGE,
    COMPILATION_UNIT,
    TYPE,
    FIELD,
    ENUM_CONSTANT,
    TYPE_PARAMETER,
    METHOD,
    LOCAL_VARIABLE
  }
}
