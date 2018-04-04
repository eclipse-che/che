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
package org.eclipse.che.ide.projectimport.zip;

import com.google.inject.ImplementedBy;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;

/** @author Roman Nikitenko */
@ImplementedBy(ZipImporterPageViewImpl.class)
public interface ZipImporterPageView extends View<ZipImporterPageView.ActionDelegate> {
  interface ActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having changed the project's name.
     */
    void projectNameChanged(@NotNull String name);

    /**
     * Performs any actions appropriate in response to the user having changed the project's URL.
     */
    void projectUrlChanged(@NotNull String url);

    /**
     * Performs any actions appropriate in response to the user having changed the project's
     * description.
     */
    void projectDescriptionChanged(@NotNull String projectDescriptionValue);

    /**
     * Performs any actions appropriate in response to the user having selected a skip first level.
     */
    void skipFirstLevelChanged(boolean isSkipFirstLevel);
  }

  /** Show the name error. */
  void showNameError();

  /** Hide the name error. */
  void hideNameError();

  /** Show URL error. */
  void showUrlError(@NotNull String message);

  /** Hide URL error. */
  void hideUrlError();

  /**
   * Set the project's URL.
   *
   * @param url the project's URL to set
   */
  void setProjectUrl(@NotNull String url);

  /**
   * Get the project's name value.
   *
   * @return {@link String} project's name
   */
  @NotNull
  String getProjectName();

  /**
   * Set the project's name value.
   *
   * @param projectName project's name to set
   */
  void setProjectName(@NotNull String projectName);

  void setProjectDescription(@NotNull String projectDescription);

  /** Give focus to project's URL input. */
  void focusInUrlInput();

  /**
   * Set the enable state of the inputs.
   *
   * @param isEnabled <code>true</code> if enabled, <code>false</code> if disabled
   */
  void setInputsEnableState(boolean isEnabled);

  /** Performs when user select skip first level. */
  boolean isSkipFirstLevelSelected();

  void setSkipFirstLevel(boolean skip);
}
