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
package org.eclipse.che.plugin.java.plain.client.wizard;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * Describes the page of Project Wizard for configuring Plain java project.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(PlainJavaPageViewImpl.class)
interface PlainJavaPageView extends View<PlainJavaPageView.ActionDelegate> {
  /** Returns value of the source folder attribute. */
  String getSourceFolder();

  /** Sets value of the source folder attribute. */
  void setSourceFolder(String value);

  /** Returns value of the library folder attribute. */
  String getLibraryFolder();

  /** Sets value of the library folder attribute. */
  void setLibraryFolder(String value);

  /**
   * Sets whether Browse button is visible.
   *
   * @param isVisible <code>true</code> to show the object, <code>false</code> to hide it
   */
  void changeBrowseBtnVisibleState(boolean isVisible);

  /**
   * Sets whether Library folder panel is visible.
   *
   * @param isVisible <code>true</code> to show the object, <code>false</code> to hide it
   */
  void changeLibraryPanelVisibleState(boolean isVisible);

  /**
   * Sets whether source folder field is enabled.
   *
   * @param isEnable <code>true</code> to enable the widget, <code>false</code> to disable it
   */
  void changeSourceFolderFieldState(boolean isEnable);

  interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having changed on the fields. */
    void onCoordinatesChanged();

    /** Called when Browse button is clicked for choosing source folder. */
    void onBrowseSourceButtonClicked();

    /** Called when Browse button is clicked for choosing library folder. */
    void onBrowseLibraryButtonClicked();
  }
}
