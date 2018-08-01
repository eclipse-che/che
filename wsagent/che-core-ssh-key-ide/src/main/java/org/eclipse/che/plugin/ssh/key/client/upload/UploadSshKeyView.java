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
package org.eclipse.che.plugin.ssh.key.client.upload;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link UploadSshKeyPresenter}.
 *
 * @author Andrey Plotnikov
 */
public interface UploadSshKeyView extends View<UploadSshKeyView.ActionDelegate> {
  /** Needs for delegate some function into UploadSshKey view. */
  interface ActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Upload button.
     */
    void onUploadClicked();

    /**
     * Performs any actions appropriate in response to submit operation is completed.
     *
     * @param result result of submit operation
     */
    void onSubmitComplete(@NotNull String result);

    /** Performs any actions appropriate in response to the user having changed file name field. */
    void onFileNameChanged();
  }

  /** @return host */
  @NotNull
  String getHost();

  /**
   * Set host into place on view.
   *
   * @param host
   */
  void setHost(@NotNull String host);

  /** @return file name */
  @NotNull
  String getFileName();

  /**
   * Change the enable state of the upload button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnabledUploadButton(boolean enabled);

  /**
   * Set error message
   *
   * @param message the message
   */
  void setMessage(@NotNull String message);

  /**
   * Sets the encoding used for submitting this form.
   *
   * @param encodingType the form's encoding
   */
  void setEncoding(@NotNull String encodingType);

  /**
   * Sets the 'action' associated with this form. This is the URL to which it will be submitted.
   *
   * @param url the form's action
   */
  void setAction(@NotNull String url);

  /** Submits the form. */
  void submit();

  /** Shows current dialog. */
  void showDialog();

  /** Close current dialog. */
  void close();
}
