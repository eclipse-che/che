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
package org.eclipse.che.ide.ext.git.client.compare;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link ComparePresenter}.
 *
 * @author Igor Vinokur
 */
@ImplementedBy(CompareViewImpl.class)
interface CompareView extends View<CompareView.ActionDelegate> {

  interface ActionDelegate {
    /** Performs some actions in response to user's closing the window. */
    void onClose();

    /** Performs save of editable panel in diff dialog. Does nothing if content isn't editable. */
    void onSaveChangesClicked();

    /** Shows next diff. */
    void onNextDiffClicked();

    /** Shows previous diff. */
    void onPreviousDiffClicked();
  }

  /** Returns content of editable part of the widget */
  String getEditableContent();

  /**
   * Set a title for the window.
   *
   * @param title text that will be as a title in the window
   */
  void setTitleCaption(String title);

  /**
   * Set left and right column titles.
   *
   * @param leftTitle title for the left column
   * @param rightTitle title for the right column
   */
  void setColumnTitles(String leftTitle, String rightTitle);

  /** Hide compare window. */
  void close();

  /** Shows whether widget is opened */
  boolean isVisible();

  /**
   * Show compare window with specified contents.
   *
   * @param oldContent content from specified revision or branch
   * @param newContent content of current file
   * @param file changed file name with its full path
   * @param readOnly read only state of the left column
   */
  void show(String oldContent, String newContent, String file, boolean readOnly);

  /** Change the enable state of the Save Changes button */
  void setEnableSaveChangesButton(boolean enabled);

  /** Change the enable state of the Next Diff button */
  void setEnableNextDiffButton(boolean enabled);

  /** Change the enable state of the Previous Diff button */
  void setEnablePreviousDiffButton(boolean enabled);
}
