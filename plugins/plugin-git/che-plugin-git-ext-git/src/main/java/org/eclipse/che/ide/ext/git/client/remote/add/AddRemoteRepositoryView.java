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
package org.eclipse.che.ide.ext.git.client.remote.add;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link AddRemoteRepositoryPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public interface AddRemoteRepositoryView extends View<AddRemoteRepositoryView.ActionDelegate> {
  /** Needs for delegate some function into AddRemoteRepository view. */
  public interface ActionDelegate {
    /** Performs any actions appropriate in response to the user having pressed the Ok button. */
    void onOkClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();

    /** Performs any actions appropriate in response to the user having changed something. */
    void onValueChanged();
  }

  /** @return repository name */
  @NotNull
  String getName();

  /**
   * Set value of name field.
   *
   * @param name repository name
   */
  void setName(@NotNull String name);

  /** @return repository url */
  @NotNull
  String getUrl();

  /**
   * Set value of url field.
   *
   * @param url repository url
   */
  void setUrl(@NotNull String url);

  /**
   * Change the enable state of the ok button.
   *
   * @param enable <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void setEnableOkButton(boolean enable);

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
