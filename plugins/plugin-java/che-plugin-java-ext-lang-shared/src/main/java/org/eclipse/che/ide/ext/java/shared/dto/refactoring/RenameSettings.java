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
 * Settings for all Rename refactorings.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface RenameSettings extends RefactoringSession {

  // IDelegateUpdating
  /**
   * If refactoring object is capable of creating appropriate delegates for the refactored elements.
   * This value used to set whether to create delegates.
   */
  boolean isDelegateUpdating();

  void setDelegateUpdating(boolean updating);

  /**
   * If refactoring object is capable of creating appropriate delegates for the refactored elements.
   * This value used to set whether to deprecate delegates.
   */
  boolean isDeprecateDelegates();

  void setDeprecateDelegates(boolean delegates);

  // IQualifiedNameUpdating

  /**
   * If this refactoring object is capable of updating qualified names in non Java files. then this
   * value is used to inform the refactoring object whether references in non Java files should be
   * updated.
   */
  boolean isUpdateQualifiedNames();

  void setUpdateQualifiedNames(boolean update);

  String getFilePatterns();

  void setFilePatterns(String patterns);

  // ISubpackagesUpdating

  /**
   * Informs the refactoring object whether subpackages should be updated. This value used to set
   * whether to updating packages.
   *
   * @return <code>true</code> if subpackages updating is enabled
   */
  boolean isUpdateSubpackages();

  void setUpdateSubpackages(boolean update);

  // IReferenceUpdating

  /**
   * Informs the refactoring object whether references should be updated. * @return <code>true
   * </code> iff reference updating is enabled
   */
  boolean isUpdateReferences();

  void setUpdateReferences(boolean update);

  // ISimilarDeclarationUpdating

  /**
   * If this refactoring object is capable of updating similar declarations of the renamed element,
   * then this value is used to inform the refactoring object whether similar declarations should be
   * updated.
   *
   * @return
   */
  boolean isUpdateSimilarDeclarations();

  void setUpdateSimilarDeclarations(boolean update);

  /** method is used to set the match strategy for determining similarly named elements. */
  int getMachStrategy();

  /**
   * @param strategy must be one of {@link
   *     org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings.MachStrategy} values.
   */
  void setMachStrategy(int strategy);

  // ITextUpdating

  /**
   * This method is used to inform the refactoring object whether references in regular (non
   * JavaDoc) comments and string literals should be updated.
   */
  boolean isUpdateTextualMatches();

  void setUpdateTextualMatches(boolean update);

  enum MachStrategy {
    EXACT(1),
    EMBEDDED(2),
    SUFFIX(3);
    private int value;

    MachStrategy(int i) {
      value = i;
    }

    public int getValue() {
      return value;
    }
  }
}
