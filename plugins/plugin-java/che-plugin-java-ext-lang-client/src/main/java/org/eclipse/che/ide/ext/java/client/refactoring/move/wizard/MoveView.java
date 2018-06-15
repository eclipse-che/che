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
package org.eclipse.che.ide.ext.java.client.refactoring.move.wizard;

import com.google.inject.ImplementedBy;
import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaProjectStructure;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;

/**
 * The visual part of Move wizard that has an ability to show configuration of a refactoring
 * operation.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@ImplementedBy(MoveViewImpl.class)
interface MoveView extends View<MoveView.ActionDelegate> {

  /**
   * Show Move panel with the special information.
   *
   * @param refactorInfo information about the move operation
   */
  void show(RefactorInfo refactorInfo);

  /** Hide Move panel. */
  void close();

  /** Sets empty text into error label */
  void clearErrorLabel();

  /**
   * Sets tree of the packages structure of the project.
   *
   * @param projects list of projects from current workspace
   * @param refactorInfo information about refactoring operation
   */
  void setTreeOfDestinations(RefactorInfo refactorInfo, List<JavaProjectStructure> projects);

  /**
   * Show information message into bottom of view.
   *
   * @param status status of move operation
   */
  void showStatusMessage(RefactoringStatus status);

  /**
   * Show error message into bottom of view.
   *
   * @param status status of error move operation
   */
  void showErrorMessage(RefactoringStatus status);

  /**
   * Setts enable scope of the Preview button.
   *
   * @param isEnable enable state of scope property
   */
  void setEnablePreviewButton(boolean isEnable);

  /**
   * Setts enable scope of the Accept button.
   *
   * @param isEnable enable state of scope property
   */
  void setEnableAcceptButton(boolean isEnable);

  /** Clears a label with status message. */
  void clearStatusMessage();

  /** returns <code>true<code/> if update reference is checked, <code>false<code/> update reference is unchecked. */
  boolean isUpdateReferences();

  /** returns <code>true<code/> if a update qualified name is selected, <code>false<code/> update qualified name is unselected. */
  boolean isUpdateQualifiedNames();

  /** return value of the file patterns. */
  String getFilePatterns();

  interface ActionDelegate {
    /** Performs some actions in response to user's clicking on the 'Preview' button. */
    void onPreviewButtonClicked();

    /** Performs some actions in response to user's clicking on the 'Accept' button. */
    void onAcceptButtonClicked();

    /** Performs some actions in response to user's clicking on the 'Cancel' button. */
    void onCancelButtonClicked();

    /** Performs some actions in response to user's clicking on some destination. */
    void setMoveDestinationPath(String path, String projectPath);
  }
}
