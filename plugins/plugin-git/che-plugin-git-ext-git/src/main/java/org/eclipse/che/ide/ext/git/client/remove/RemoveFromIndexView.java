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
package org.eclipse.che.ide.ext.git.client.remove;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link RemoveFromIndexPresenter}.
 *
 * @author Andrey Plotnikov
 */
public interface RemoveFromIndexView extends View<RemoveFromIndexView.ActionDelegate> {

  /** Is needed to delegate some function into CloneRepository view. */
  interface ActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the Remove button.
     */
    void onRemoveClicked();

    /**
     * Performs any actions appropriate in response to the user having pressed the Cancel button.
     */
    void onCancelClicked();
  }

  /**
   * Set content into message field.
   *
   * @param message content of message
   */
  void setMessage(@NotNull String message);

  /**
   * @return <code>true</code> if files need to remove only from index, and <code>false</code>
   *     otherwise
   */
  boolean isRemoved();

  /**
   * Set state for files.
   *
   * @param isRemoved <code>true</code> to remove file only from index, <code>false</code> to remove
   *     files
   */
  void setRemoved(boolean isRemoved);

  /** Close dialog. */
  void close();

  /** Show dialog. */
  void showDialog();
}
