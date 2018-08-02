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
package org.eclipse.che.ide.ext.java.client.formatter.preferences;

import org.eclipse.che.ide.api.mvp.View;

/** The view of the code formatter importer page. */
public interface FormatterPreferencePageView
    extends View<FormatterPreferencePageView.ActionDelegate> {

  interface ActionDelegate {
    /** Calls when dirty state of the page is changed. */
    void onDirtyChanged();

    /** Calls when the import button is clicked. */
    void onImportButtonClicked();

    /**
     * Shows error message.
     *
     * @param error test of an error
     */
    void showErrorMessage(String error);
  }

  /** Shows view of the code importer page. */
  void showDialog();

  /** returns the content of the selected file. */
  String getFileContent();

  /**
   * returns {@code true} if for the formatter should be applied for the whole workspace otherwise
   * returns {@code false}.*
   */
  boolean isWorkspaceTarget();
}
