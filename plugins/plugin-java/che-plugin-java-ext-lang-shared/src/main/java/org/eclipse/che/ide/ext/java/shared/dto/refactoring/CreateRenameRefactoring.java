/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for creating rename refactoring.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface CreateRenameRefactoring {

  /**
   * type of rename refactoring: java file(compilation unit), package or java element (class, field,
   * method, local variable...)
   */
  RenameType getType();

  void setType(RenameType type);

  String getProjectPath();

  void setProjectPath(String path);

  /**
   * Workspace path for package or FQN for compilation unit
   *
   * @return path or FQN
   */
  String getPath();

  void setPath(String path);

  /** if true refactoring may be performed without UI(wizard) */
  boolean isRefactorLightweight();

  void setRefactorLightweight(boolean lightweight);

  /** if rename type is JAVA_ELEMENT this method must return position where refactoring is called */
  int getOffset();

  void setOffset(int offset);

  enum RenameType {
    COMPILATION_UNIT,
    PACKAGE,
    JAVA_ELEMENT
  }
}
