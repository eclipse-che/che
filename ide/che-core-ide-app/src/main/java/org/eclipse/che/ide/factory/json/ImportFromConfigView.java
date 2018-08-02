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
package org.eclipse.che.ide.factory.json;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * The view of {@link ImportFromConfigPresenter}.
 *
 * @author Sergii Leschenko
 */
public interface ImportFromConfigView extends IsWidget {

  interface ActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Import button.
     */
    void onImportClicked();

    /** Performs any actions appropriate in response to error reading file. */
    void onErrorReadingFile(String errorMessage);
  }

  /** Show dialog. */
  void showDialog();

  /** Close dialog */
  void closeDialog();

  /** Sets the delegate to receive events from this view. */
  void setDelegate(ActionDelegate delegate);

  /** Enables or disables import button */
  void setEnabledImportButton(boolean enabled);

  /** Get content of selected file */
  String getFileContent();
}
