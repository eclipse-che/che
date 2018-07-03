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
package org.eclipse.che.ide.ext.java.client.refactoring.preview;

import com.google.inject.ImplementedBy;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;

/**
 * The visual part of Preview view that has an ability to show preview information about the
 * refactoring operation.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@ImplementedBy(PreviewViewImpl.class)
interface PreviewView extends View<PreviewView.ActionDelegate> {
  /**
   * Set a title of the window.
   *
   * @param title the name of the preview window
   */
  void setTitleCaption(String title);

  /**
   * Show error message into bottom of view.
   *
   * @param status status of error move operation
   */
  void showErrorMessage(RefactoringStatus status);

  /**
   * Show diffs of selected change.
   *
   * @param preview information about change
   */
  void showDiff(@Nullable ChangePreview preview);

  /** Hide Move panel. */
  void close();

  /** Show Preview panel with the special information. */
  void showDialog();

  /**
   * Sets tree of the changes.
   *
   * @param nodes changes from the refactoring operation
   */
  void setTreeOfChanges(Map<String, PreviewNode> nodes);

  interface ActionDelegate {
    /** Performs some actions in response to user's clicking on the 'Cancel' button. */
    void onCancelButtonClicked();

    /** Performs some actions in response to user's clicking on the 'Accept' button. */
    void onAcceptButtonClicked();

    /** Performs some actions in response to user's clicking on the 'Back' button. */
    void onBackButtonClicked();

    /** Performs some actions in response to user's choosing some change. */
    void onEnabledStateChanged(PreviewNode change);

    /** Performs some actions in response to user selected some change. */
    void onSelectionChanged(PreviewNode selectedNode);
  }
}
