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
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;

/**
 * The visual part of Rename wizard that has an ability to show configuration of a refactoring
 * operation.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(RenameViewImpl.class)
interface RenameView extends View<RenameView.ActionDelegate> {

  /** Returns new name. */
  String getNewName();

  /** Show Rename panel with the special information. */
  void showDialog();

  /** Hide Rename panel. */
  void close();

  /**
   * Set title of wizard.
   *
   * @param title name of wizard
   */
  void setTitleCaption(String title);

  /**
   * Sets the renaming name.
   *
   * @param name old name
   */
  void setOldName(String name);

  /** Set empty text into error label */
  void clearErrorLabel();

  /**
   * Set visible states for the full qualified name panel.
   *
   * @param isVisible visible state of the panel
   */
  void setVisibleFullQualifiedNamePanel(boolean isVisible);

  /**
   * Set visible states for the patterns panel.
   *
   * @param isVisible visible state of the panel
   */
  void setVisiblePatternsPanel(boolean isVisible);

  /**
   * Set visible states for the Keep original name panel.
   *
   * @param isVisible visible state of the panel
   */
  void setVisibleKeepOriginalPanel(boolean isVisible);

  /**
   * Set visible states for the Rename subpackages panel.
   *
   * @param isVisible visible state of the panel
   */
  void setVisibleRenameSubpackagesPanel(boolean isVisible);

  /**
   * Set visible states for the Similarly variables panel.
   *
   * @param isVisible visible state of the panel
   */
  void setVisibleSimilarlyVariablesPanel(boolean isVisible);

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

  void setFocus();

  /**
   * Set enable scope of the Preview button.
   *
   * @param isEnable enable state of scope property
   */
  void setEnablePreviewButton(boolean isEnable);

  /**
   * Set enable scope of the Accept button.
   *
   * @param isEnable enable state of scope property
   */
  void setEnableAcceptButton(boolean isEnable);

  /**
   * Set the visibility state of the loader for 'OK' button.
   *
   * @param isVisible <code>true</code> if visible, <code>false</code> otherwise.
   */
  void setLoaderVisibility(boolean isVisible);

  /** returns <code>true<code/> if update reference is checked, <code>false<code/> update reference is unchecked. */
  boolean isUpdateReferences();

  /** returns <code>true<code/> if update delegate updating is checked, <code>false<code/> update delegate updating is unchecked. */
  boolean isUpdateDelegateUpdating();

  /** returns <code>true<code/> if update deprecate marker is checked, <code>false<code/> update deprecate marker is unchecked. */
  boolean isUpdateMarkDeprecated();

  /** returns <code>true<code/> if update subpackages is checked, <code>false<code/> update subpackages is unchecked. */
  boolean isUpdateSubpackages();

  /** returns <code>true<code/> if update textual occurrences is checked, <code>false<code/> update textual occurrences is unchecked. */
  boolean isUpdateTextualOccurrences();

  /** returns <code>true<code/> if a update qualified name is selected, <code>false<code/> update qualified name is unselected. */
  boolean isUpdateQualifiedNames();

  /** returns <code>true<code/> if a similarly variables is selected, <code>false<code/> similarly variables is unselected. */
  boolean isUpdateSimilarlyVariables();

  /** return value of the file patterns. */
  String getFilePatterns();

  interface ActionDelegate {
    /** Performs some actions in response to user's clicking on the 'Preview' button. */
    void onPreviewButtonClicked();

    /** Performs some actions in response to user's clicking on the 'Accept' button. */
    void onAcceptButtonClicked();

    /** Performs some actions in response to user's clicking on the 'Cancel' button. */
    void onCancelButtonClicked();

    /** Validates refactored name. */
    void validateName();
  }
}
