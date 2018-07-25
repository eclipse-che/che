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

/**
 * DTO that represent move refactoring wizard settings.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface MoveSettings extends RefactoringSession {

  /** @return true if refactoring should update references in java classes, false otherwise */
  boolean isUpdateReferences();

  /** @param updateReferences */
  void setUpdateReferences(boolean updateReferences);

  /** Used to ask the refactoring object whether references in non Java files should be updated. */
  boolean isUpdateQualifiedNames();

  /**
   * Used to inform the refactoring object whether references in non Java files should be updated.
   *
   * @param update
   */
  void setUpdateQualifiedNames(boolean update);

  /**
   * if {@link #isUpdateQualifiedNames()} return true refactoring will use this file pattern to
   * search qualified names
   *
   * @return the file pattern
   */
  String getFilePatterns();

  /**
   * The file pattern. (like "*.php")
   *
   * @param patterns
   */
  void setFilePatterns(String patterns);
}
