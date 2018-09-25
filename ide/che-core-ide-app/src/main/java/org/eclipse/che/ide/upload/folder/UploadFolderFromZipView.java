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
package org.eclipse.che.ide.upload.folder;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;
import javax.validation.constraints.NotNull;

/**
 * The view of {@link UploadFolderFromZipPresenter}.
 *
 * @author Roman Nikitenko.
 */
@ImplementedBy(UploadFolderFromZipViewImpl.class)
public interface UploadFolderFromZipView extends IsWidget {

  public interface ActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /**
     * Performs any actions appropriate in response to submit operation is completed.
     *
     * @param result result of submit operation
     */
    void onSubmitComplete(String result);

    /**
     * Performs any actions appropriate in response to the user having pressed the Upload button.
     */
    void onUploadClicked();

    /** Performs any actions appropriate in response to the user having changed file name field. */
    void onFileNameChanged();
  }

  /** Show dialog. */
  void showDialog();

  /** Close dialog */
  void closeDialog();

  /**
   * Set the visibility state of the loader.
   *
   * @param isVisible <code>true</code> if visible.
   */
  void setLoaderVisibility(boolean isVisible);

  /** Sets the delegate to receive events from this view. */
  void setDelegate(ActionDelegate delegate);

  /**
   * Change the enable state of the upload button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnabledUploadButton(boolean enabled);

  /**
   * Sets the encoding used for submitting form.
   *
   * @param encodingType the form's encoding
   */
  void setEncoding(@NotNull String encodingType);

  /**
   * Sets the 'action' associated with form. This is the URL to which it will be submitted.
   *
   * @param url the form's action
   */
  void setAction(@NotNull String url);

  /** Submits the form. */
  void submit();

  /** @return file name */
  @NotNull
  String getFileName();

  /** Performs when user select 'overwrite if file exists'. */
  boolean isOverwriteFileSelected();

  /** Performs when user select 'Skip the root folder of the archive'. */
  boolean isSkipRootFolderSelected();
}
