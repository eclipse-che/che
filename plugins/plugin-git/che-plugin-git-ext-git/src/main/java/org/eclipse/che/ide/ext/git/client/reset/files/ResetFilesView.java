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
package org.eclipse.che.ide.ext.git.client.reset.files;

import org.eclipse.che.api.git.shared.IndexFile;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link org.eclipse.che.ide.ext.git.client.reset.commit.ResetToCommitPresenter}.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public interface ResetFilesView extends View<ResetFilesView.ActionDelegate> {
  String FILES = "Files for commit";

  /** Needs for delegate some function into ResetFiles view. */
  interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having pressed the Reset button. */
    void onResetClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();
  }

  /**
   * Set indexed files into table on view.
   *
   * @param indexedFiles indexed files
   */
  void setIndexedFiles(IndexFile[] indexedFiles);

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
