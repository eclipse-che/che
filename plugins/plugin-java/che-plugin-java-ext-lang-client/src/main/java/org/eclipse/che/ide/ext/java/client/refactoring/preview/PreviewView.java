/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.refactoring.preview;

import com.google.inject.ImplementedBy;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;

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
   * Sets tree of the changes.
   *
   * @param changes list of changes from the refactoring operation
   */
  void setTreeOfChanges(RefactoringPreview changes);

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

  interface ActionDelegate {
    /** Performs some actions in response to user's clicking on the 'Cancel' button. */
    void onCancelButtonClicked();

    /** Performs some actions in response to user's clicking on the 'Accept' button. */
    void onAcceptButtonClicked();

    /** Performs some actions in response to user's clicking on the 'Back' button. */
    void onBackButtonClicked();

    /** Performs some actions in response to user's choosing some change. */
    void onEnabledStateChanged(RefactoringPreview change);

    /** Performs some actions in response to user's selecting some change. */
    void onSelectionChanged(RefactoringPreview change);
  }
}
